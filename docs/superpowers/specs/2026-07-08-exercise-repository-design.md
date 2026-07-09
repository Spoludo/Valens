# Exercise Repository Abstraction — Design

**Status:** Approved
**Scope:** An Android-independent `ExerciseRepository` abstraction that wraps `ExercisePackLoader` (decoding) and `ExercisePackValidator` (referential integrity) behind a single caching, coroutine-friendly API. No Android `AssetManager`, no copying `exercise-packs/` into `app/src/main/assets/`, no wiring into `MainActivity`/UI/DI, no Room entities, no planner logic, no workout engine logic.

## 1. Purpose

The previous three commits built, in order: typed schema models (`ExercisePack` and friends), a decoder (`ExercisePackJsonParser` + `ExercisePackJsonSource`), and a referential-integrity checker (`ExercisePackValidator`). Nothing yet combines them into one thing a future caller (a ViewModel, the planner) can call to get "the current exercise pack, or a reason it's unavailable." This commit adds that: `ExerciseRepository`, living in `data/repository` per `docs/03_architecture.md` §9-10 (which names `ExerciseRepository` explicitly and already has an empty `data/repository/.gitkeep` placeholder).

The repository does not introduce a new I/O mechanism — it is parameterized over the same `ExercisePackJsonSource` abstraction the parser already uses, so it stays Android-independent and JVM-testable, matching the pattern of every prior commit in this sequence. A production Android-asset-backed `ExercisePackJsonSource` is deliberately deferred to a future commit.

## 2. Files

| File | Layer | Role |
|---|---|---|
| `app/src/main/java/com/spoludo/valens/data/repository/ExerciseRepository.kt` | data (new) | `ExerciseRepository` interface + `ExerciseRepositoryResult` sealed interface. |
| `app/src/main/java/com/spoludo/valens/data/repository/DefaultExerciseRepository.kt` | data (new) | `DefaultExerciseRepository`, the mutex-guarded caching implementation. |
| `app/src/test/java/com/spoludo/valens/data/repository/DefaultExerciseRepositoryTest.kt` | test (new) | Fakes for most cases, one real-stack integration test. |

## 3. Interface and result type

```kotlin
package com.spoludo.valens.data.repository

import com.spoludo.valens.domain.model.ExercisePack
import com.spoludo.valens.domain.model.ExercisePackValidationResult

sealed interface ExerciseRepositoryResult {
    data class Success(val exercisePack: ExercisePack) : ExerciseRepositoryResult
    data class InvalidPack(val validationResult: ExercisePackValidationResult) : ExerciseRepositoryResult
    data class LoadFailure(val cause: Throwable) : ExerciseRepositoryResult
}

interface ExerciseRepository {
    suspend fun loadExercisePack(): ExerciseRepositoryResult
    suspend fun reloadExercisePack(): ExerciseRepositoryResult
    suspend fun clearCache()
}
```

`Success` and `InvalidPack` are both "the JSON decoded fine" outcomes, distinguished from `LoadFailure` ("the JSON itself didn't decode" — a raw decode exception, e.g. `kotlinx.serialization.SerializationException`, or any exception thrown while obtaining/reading the source). `InvalidPack` carries the full `ExercisePackValidationResult` (not just its `errors` list) so a future caller can also see `warnings` once that type gains real cases.

All three functions are `suspend` from the start, even though today's only source (`ExercisePackJsonSource` backed by already-in-memory strings, or a filesystem read in tests) does no real asynchronous I/O. This avoids an API change later when a real Android asset source, an imported community pack, or any other file-backed source is added — callers written against this interface today don't need to change.

## 4. `DefaultExerciseRepository`

```kotlin
package com.spoludo.valens.data.repository

import com.spoludo.valens.data.json.ExercisePackJsonSource
import com.spoludo.valens.data.json.ExercisePackLoader
import com.spoludo.valens.domain.model.ExercisePack
import com.spoludo.valens.domain.model.ExercisePackValidator
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class DefaultExerciseRepository(
    private val sourceProvider: () -> ExercisePackJsonSource,
    private val loader: ExercisePackLoader,
    private val validator: ExercisePackValidator,
) : ExerciseRepository {
    private val mutex = Mutex()
    private var cached: ExercisePack? = null

    override suspend fun loadExercisePack(): ExerciseRepositoryResult {
        cached?.let { return ExerciseRepositoryResult.Success(it) }
        return mutex.withLock {
            cached?.let { return@withLock ExerciseRepositoryResult.Success(it) }
            loadAndValidate()
        }
    }

    override suspend fun reloadExercisePack(): ExerciseRepositoryResult =
        mutex.withLock {
            cached = null
            loadAndValidate()
        }

    override suspend fun clearCache() {
        mutex.withLock { cached = null }
    }

    private fun loadAndValidate(): ExerciseRepositoryResult {
        val pack = try {
            loader.load(sourceProvider())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return ExerciseRepositoryResult.LoadFailure(e)
        }

        val validationResult = validator.validate(pack)
        if (!validationResult.isValid) {
            return ExerciseRepositoryResult.InvalidPack(validationResult)
        }

        cached = pack
        return ExerciseRepositoryResult.Success(pack)
    }
}
```

Notes:

- **Caching:** double-checked locking. The fast path (`cached?.let { ... }` before acquiring the mutex) avoids lock contention once a pack is cached — the common case after the first successful load. Only `Success` results populate `cached`; `InvalidPack` and `LoadFailure` never do, so a subsequent `loadExercisePack()` call retries the full load rather than repeating a stale failure.
- **`reloadExercisePack()`** unconditionally clears the cache and re-runs `loadAndValidate()` under the lock, which calls `sourceProvider()` again — this is the one place a fresh source is guaranteed, per the constructor's provider-lambda design (a plain `ExercisePackJsonSource` instance would risk `reload()` re-decoding stale, already-read text).
- **`clearCache()`** just nulls the cache under the lock; the next `loadExercisePack()` call does the actual work. Both `reloadExercisePack()` and `clearCache()` are `suspend` for a consistent, uniformly-lockable API, even though `clearCache()`'s own body does no suspending work beyond acquiring the mutex.
- **Cancellation:** `CancellationException` is re-thrown, not wrapped into `LoadFailure` — swallowing it would break structured concurrency (a cancelled parent coroutine must see the cancellation propagate, not an unrelated `LoadFailure` result). Only `Exception` (not `Throwable`) is caught, so `Error`s (e.g. `OutOfMemoryError`) are not wrapped either.
- **`validator: ExercisePackValidator`** is typed as the concrete `object` (not an interface) — `ExercisePackValidator.validate()` is pure, deterministic domain logic proven by its own unit tests, so there is no second implementation to substitute in tests. It's still an explicit constructor parameter (not a hardcoded reference inside `loadAndValidate()`) for consistency with `loader`, which *is* swappable.

## 5. Testing plan

`DefaultExerciseRepositoryTest` (`app/src/test/java/com/spoludo/valens/data/repository/`), using `kotlinx.coroutines.test.runTest`. Most cases use a small in-file `FakeExercisePackLoader` (implements `ExercisePackLoader`, returns a canned `ExercisePack` or throws a canned exception, ignoring the `ExercisePackJsonSource` it's given) paired with a no-op `ExercisePackJsonSource` fake — this keeps each test a single isolated behavior instead of requiring hand-written JSON fixtures for every case. The real decode+validate stack is proven once, end to end, against the real bundled pack.

| Test | Setup | Assertion |
|---|---|---|
| `loadExercisePack_validPack_returnsSuccessAndCaches` | Fake loader returns a valid minimal pack | First call: `Success`. Second call: `Success` with the same pack, and the fake loader/source-provider were invoked only once total (proves caching). |
| `loadExercisePack_invalidPack_returnsInvalidPackAndDoesNotCache` | Fake loader returns a pack with a broken movement-pattern reference | Result is `InvalidPack` with a non-empty `validationResult.errors`. A second call re-invokes the loader (proves it wasn't cached). |
| `loadExercisePack_loaderThrows_returnsLoadFailure` | Fake loader throws `SerializationException` | Result is `LoadFailure(cause)` with the same exception. Not cached (second call re-invokes the loader). |
| `loadExercisePack_sourceProviderThrows_returnsLoadFailure` | `sourceProvider` lambda itself throws | Result is `LoadFailure(cause)`. |
| `loadExercisePack_cancellation_propagatesInsteadOfLoadFailure` | Fake loader throws `CancellationException` | The exception propagates out of `loadExercisePack()` (assert via `assertThrows`/try-catch), not wrapped as `LoadFailure`. |
| `reloadExercisePack_forcesFreshSourceAndReplacesCache` | Load once (caches pack A via a mutable fake loader), change the fake to return pack B, call `reloadExercisePack()` | Result is `Success(packB)`, `sourceProvider` was invoked again, and a subsequent `loadExercisePack()` returns the now-cached pack B without a further invocation. |
| `clearCache_makesNextLoadReinvokeLoader` | Load once (caches), call `clearCache()`, load again | Loader/source-provider invoked twice total (once before clear, once after). |
| `loadExercisePack_realBundledPack_returnsSuccessWith11Exercises` | Real `RealBundledExercisePackJsonSource` (reused from the `data.json` test package) + real `ExercisePackJsonParser` + real `ExercisePackValidator` | Result is `Success`, `exercisePack.exercises.size == 11`. |

The fake pack fixtures needed for the `InvalidPack` case reuse the same shape of minimal `Exercise`/`MovementPattern`/`Muscle`/`Joint` builders already established in `ExercisePackValidatorTest` (small, self-contained, private to this test file — no shared test-fixture module is introduced, consistent with that file's own approach).

## 6. Out of scope (explicit, per this task)

- Android `AssetManager` or any on-device runtime loading path
- Copying/symlinking `exercise-packs/` into `app/src/main/assets/`
- Wiring `ExerciseRepository` into `MainActivity`, any ViewModel, DI, or UI
- Room entities
- Planner logic, workout engine logic
- Concurrency stress-testing beyond the functional cache-correctness cases in §5 (no multi-threaded race tests)
