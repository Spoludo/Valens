# Practice-First MVP — Design

**Status:** Approved
**Scope:** The fastest path to "open the app, press Start, complete a guided 20-minute routine from the bundled isometric pack." Explicitly *not* architectural completeness — no Room, no statistics, no capacity model, no full movement-pattern planner, no asset pipeline, no audio cues, no feedback collection.

## 1. Purpose

Every prior commit built foundation (schema models, decoder, validator, repository abstraction) without anything runnable. This is the first commit series that makes the app do something: load the real bundled pack from Android assets, build one fixed beginner routine from it, and run it through a minimal but real workout engine surfaced in a Compose screen.

## 2. Asset packaging — no duplication

Rather than copying `exercise-packs/bundled/isometric-foundations/` into `app/src/main/assets/` (a second copy that can drift from the source of truth), add it as an *additional AGP assets source directory* in `app/build.gradle.kts`:

```kotlin
android {
    // ...existing config...
    sourceSets {
        getByName("main") {
            assets.srcDirs("../exercise-packs/bundled/isometric-foundations")
        }
    }
}
```

AGP merges every `srcDirs` entry into one assets tree at build time, using each file's path relative to its source root as the asset path — `pack.json` → asset `pack.json`, `exercises/wall_sit.json` → asset `exercises/wall_sit.json`, etc. Single source of truth, zero sync step, nothing to remember to re-copy.

## 3. `AndroidAssetExercisePackJsonSource`

New file, `data/json` (existing package — this is the first Android-*dependent* implementation of the existing `ExercisePackJsonSource` interface; `data` is expected to hold Android-specific I/O, only `domain` must stay pure):

```kotlin
package com.spoludo.valens.data.json

import android.content.res.AssetManager

class AndroidAssetExercisePackJsonSource(
    private val assetManager: AssetManager,
) : ExercisePackJsonSource {
    override fun manifest() = readAsset("pack.json")
    override fun movementPatterns() = readAsset("movement-patterns.json")
    override fun muscles() = readAsset("muscles.json")
    override fun joints() = readAsset("joints.json")
    override fun exercises() = readAssetDirectory("exercises")
    override fun translations() = readAssetDirectory("translations")

    private fun readAsset(path: String): String =
        assetManager.open(path).bufferedReader().use { it.readText() }

    private fun readAssetDirectory(directory: String): List<JsonTextFile> =
        assetManager.list(directory).orEmpty()
            .filter { it.endsWith(".json") }
            .map { fileName -> JsonTextFile("$directory/$fileName", readAsset("$directory/$fileName")) }
}
```

No sorting needed here — `ExercisePackJsonParser` already sorts `exercises()`/`translations()` by `relativePath` before decoding.

## 4. Wiring — manual DI, no Hilt

Per `docs/20_technology_stack.md` ("do not introduce Hilt before the project has enough complexity"). `ValensApplication` becomes the one composition point:

```kotlin
package com.spoludo.valens

import android.app.Application
import com.spoludo.valens.data.json.AndroidAssetExercisePackJsonSource
import com.spoludo.valens.data.json.ExercisePackJsonParser
import com.spoludo.valens.data.repository.DefaultExerciseRepository
import com.spoludo.valens.data.repository.ExerciseRepository
import com.spoludo.valens.domain.model.ExercisePackValidator

class ValensApplication : Application() {
    val exerciseRepository: ExerciseRepository by lazy {
        DefaultExerciseRepository(
            sourceProvider = { AndroidAssetExercisePackJsonSource(assets) },
            loader = ExercisePackJsonParser(),
            validator = ExercisePackValidator,
        )
    }
}
```

Consumers (the workout `ViewModel`) reach it via `(LocalContext.current.applicationContext as ValensApplication).exerciseRepository`.

## 5. Routine — deliberately not `domain/planner`

CLAUDE.md's core domain rule bars hardcoded exercise ids from planner logic. The fixed 7-exercise sequence is real, named ids by design (an MVP seed, not the planner), so it lives in `workout/engine`, not `domain/planner` — keeping that package free for the real movement-pattern-driven planner later.

```kotlin
package com.spoludo.valens.workout.engine

import com.spoludo.valens.domain.model.Exercise
import com.spoludo.valens.domain.model.ExerciseId
import com.spoludo.valens.domain.model.ExercisePack

private val FIXED_BEGINNER_ROUTINE_EXERCISE_IDS = listOf(
    "wall_push", "hollow_body_hold", "calf_raise_hold", "wall_sit",
    "single_leg_balance_hold", "reverse_table_hold", "plank_hold",
).map { ExerciseId(it) }

object FixedBeginnerRoutine {
    fun build(pack: ExercisePack): List<Exercise> {
        val byId = pack.exercises.associateBy { it.id }
        return FIXED_BEGINNER_ROUTINE_EXERCISE_IDS.mapNotNull { byId[it] }
    }
}
```

Pure function: unknown/missing ids are silently skipped rather than crashing (defensive against a future pack revision dropping one of these ids). `pike_pushup_hold` is excluded by omission. Timing comes straight from each exercise's own `defaultPrescription` — no progression-step selection logic is needed for an MVP (the pack's own default is already the sensible middle-of-the-road prescription, not the hardest progression step). Left/right-side doubling for `sideModel = left_right` exercises is *not* implemented — sets run exactly as prescribed, once. Rough total with these 7 exercises' current prescriptions: ~24 minutes — close enough to "20-minute routine" as a target, not tuned further.

## 6. `WorkoutEngine` — minimal, real, testable

CLAUDE.md requires workout-engine state-transition tests with a fake clock and forbids business logic in composables, so this is a real (if small) state machine, not timer logic embedded in a Composable.

**`workout/timer/WorkoutTicker.kt`** — the swappable clock:

```kotlin
package com.spoludo.valens.workout.timer

fun interface WorkoutTicker {
    suspend fun awaitNextSecond()
}
```

**`workout/timer/RealWorkoutTicker.kt`** — production impl (`delay(1000)`).

Tests use a channel-based manual fake (`ManualWorkoutTicker`, private to the test file) whose `awaitNextSecond()` suspends on a rendezvous channel until the test explicitly calls `advanceOneSecond()` — this gives full deterministic step-by-step control without busy-looping or relying on virtual time, satisfying "use a fake clock in tests" concretely.

**`workout/engine/WorkoutEngine.kt`**:

```kotlin
package com.spoludo.valens.workout.engine

import com.spoludo.valens.domain.model.Exercise
import com.spoludo.valens.domain.model.Prescription
import com.spoludo.valens.workout.timer.WorkoutTicker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class WorkoutPhase { COUNTDOWN, WORK, REST, COMPLETE }

const val PREP_COUNTDOWN_SECONDS = 3

data class WorkoutEngineState(
    val exerciseIndex: Int = 0,
    val setIndex: Int = 0,
    val phase: WorkoutPhase = WorkoutPhase.COUNTDOWN,
    val secondsRemaining: Int = PREP_COUNTDOWN_SECONDS,
    val isRunning: Boolean = false,
    val hasStarted: Boolean = false,
)

class WorkoutEngine(
    val routine: List<Exercise>,
    private val ticker: WorkoutTicker,
) {
    private val _state = MutableStateFlow(WorkoutEngineState())
    val state: StateFlow<WorkoutEngineState> = _state.asStateFlow()

    suspend fun run() {
        if (routine.isEmpty()) {
            _state.value = _state.value.copy(phase = WorkoutPhase.COMPLETE, isRunning = false)
            return
        }
        while (_state.value.phase != WorkoutPhase.COMPLETE) {
            ticker.awaitNextSecond()
            if (_state.value.isRunning) tick()
        }
    }

    fun pause() {
        _state.value = _state.value.copy(isRunning = false)
    }

    fun resume() {
        if (_state.value.phase != WorkoutPhase.COMPLETE) {
            _state.value = _state.value.copy(isRunning = true, hasStarted = true)
        }
    }

    fun skip() {
        _state.value = advancePastCurrentPhase(_state.value)
    }

    private fun tick() {
        val current = _state.value
        val remaining = current.secondsRemaining - 1
        _state.value = if (remaining > 0) current.copy(secondsRemaining = remaining)
        else advancePastCurrentPhase(current)
    }

    private fun advancePastCurrentPhase(current: WorkoutEngineState): WorkoutEngineState {
        if (current.phase == WorkoutPhase.COMPLETE) return current
        val exercise = routine.getOrNull(current.exerciseIndex)
            ?: return current.copy(phase = WorkoutPhase.COMPLETE, isRunning = false, secondsRemaining = 0)
        val prescription = exercise.defaultPrescription

        return when (current.phase) {
            WorkoutPhase.COUNTDOWN -> current.copy(
                phase = WorkoutPhase.WORK,
                secondsRemaining = workSeconds(prescription),
            )
            WorkoutPhase.WORK -> {
                val isLastSet = current.setIndex >= prescription.sets - 1
                val isLastExercise = current.exerciseIndex >= routine.lastIndex
                if (isLastSet && isLastExercise) {
                    current.copy(phase = WorkoutPhase.COMPLETE, isRunning = false, secondsRemaining = 0)
                } else {
                    current.copy(phase = WorkoutPhase.REST, secondsRemaining = prescription.restSeconds)
                }
            }
            WorkoutPhase.REST -> {
                val isLastSet = current.setIndex >= prescription.sets - 1
                if (isLastSet) {
                    current.copy(
                        exerciseIndex = current.exerciseIndex + 1, setIndex = 0,
                        phase = WorkoutPhase.COUNTDOWN, secondsRemaining = PREP_COUNTDOWN_SECONDS,
                    )
                } else {
                    current.copy(
                        setIndex = current.setIndex + 1,
                        phase = WorkoutPhase.COUNTDOWN, secondsRemaining = PREP_COUNTDOWN_SECONDS,
                    )
                }
            }
            WorkoutPhase.COMPLETE -> current
        }
    }

    private fun workSeconds(prescription: Prescription): Int =
        prescription.holdSeconds ?: prescription.durationSeconds ?: 0
}
```

Notes:
- `run()` does **not** auto-set `isRunning = true` — it launches an idle loop that only advances once something calls `resume()`. The caller (`WorkoutViewModel`) launches `run()` exactly once (in `init`); the user's "Start" button press is what actually calls `resume()`.
- Trailing rest is skipped: `WORK` transitions straight to `COMPLETE` when both the current set and current exercise are the last.
- `hasStarted` (set once, on the first `resume()`) lets the UI distinguish "Start" (never begun) from "Resume" (begun, currently paused) without extra engine API surface.
- No audio cues, no feedback collection — not in this MVP's UI requirements; deferred cleanly (the phase state machine doesn't preclude adding them later).
- Engine methods (`pause`/`resume`/`skip`) are plain synchronous calls, not `suspend` — intended to be called from the same dispatcher (`viewModelScope`, effectively `Dispatchers.Main`) as the `run()` loop. No `Mutex`, unlike `DefaultExerciseRepository` — that repository is called from arbitrary coroutines/dispatchers; this engine is a single-owner, UI-thread-confined object. Not designed for concurrent multi-threaded access.

## 7. `WorkoutViewModel` and `WorkoutScreen`

`WorkoutViewModel` (`ui/workout`) loads the pack via `ExerciseRepository.loadExercisePack()` in `init`, builds the routine, constructs a `WorkoutEngine(routine, RealWorkoutTicker())`, launches `run()`, and republishes engine state (mapped through the pack's `"en"` translation bundle for display names — `Exercise.nameKey` is a key like `"exercise.wall_sit.name"`, not display text) as:

```kotlin
sealed interface WorkoutUiState {
    data object Loading : WorkoutUiState
    data class Error(val message: String) : WorkoutUiState
    data class Running(
        val exerciseName: String,
        val phase: WorkoutPhase,
        val secondsRemaining: Int,
        val currentSet: Int,
        val totalSets: Int,
        val nextExerciseName: String?,
        val isRunning: Boolean,
        val isStarted: Boolean,
        val isComplete: Boolean,
    ) : WorkoutUiState
}
```

Exposes `onStartOrResume()`, `onPause()`, `onNext()` — thin passthroughs to `engine.resume()/pause()/skip()`. A `WorkoutViewModel.Factory(exerciseRepository)` supplies the constructor dependency (no Hilt).

`WorkoutScreen` renders `Loading` / `Error` / `Running` states; `Running` shows exercise name, a hardcoded English phase label (`"Get Ready"/"Work"/"Rest"/"Done"` — UI chrome strings, not pack content, so not routed through the translation bundle; not yet extracted to `strings.xml`, an accepted MVP shortcut), the countdown, `"Set X / Y"`, the next exercise's name, and Start-or-Resume/Pause/Next buttons. On `isComplete`, shows a "Workout complete!" state with a "Done" button that navigates back.

`HomeScreen` gains an `onStart: () -> Unit` parameter and a "Start" button. `MainActivity` adds a minimal two-route `NavHost` (`"home"`, `"workout"`) — Navigation Compose is already a declared dependency, unused until now; this is less code than hand-rolled screen-state toggling. Popping back to `"home"` clears the `WorkoutViewModel`'s `ViewModelStore` (tied to the nav back-stack entry), which cancels `viewModelScope` and the running `engine.run()` coroutine — no manual cleanup needed.

## 8. Test scope

Domain-layer tests only, matching "domain tests > UI tests":

- `FixedBeginnerRoutineTest` — building against the real bundled pack returns exactly the 7 exercises in the specified order; a pack missing one of the ids skips it rather than crashing.
- `WorkoutEngineTest` — using `ManualWorkoutTicker`, drives a small 2-exercise/1-set-each fixture through: countdown → work → rest → transition → countdown → work → complete (trailing rest skipped); pausing mid-tick freezes state until resumed; `skip()` jumps phases immediately without waiting out the remaining time.

No instrumented tests for `AndroidAssetExercisePackJsonSource`, `WorkoutViewModel`, or `WorkoutScreen` in this pass — verified by compiling, `./gradlew build`, and running the app manually. Explicit scope cut, consistent with "don't turn this into a long architecture phase."

## 9. Commits

1. `feat(data): load bundled exercise pack from Android assets` — Gradle assets sourceSet change, `AndroidAssetExercisePackJsonSource`, `ValensApplication` wiring.
2. `feat(workout): add simple routine builder` — `FixedBeginnerRoutine`, `WorkoutTicker`/`RealWorkoutTicker`, `WorkoutEngine`, both test files.
3. `feat(ui): add basic guided workout screen` — `WorkoutViewModel`, `WorkoutScreen`, `HomeScreen` Start button, `MainActivity` `NavHost`.

## 10. Out of scope (explicit, per this task)

Room persistence, statistics, capacity model, full movement-pattern planner intelligence, asset pipeline/SVG generation, muscle maps, feedback history, authentication/cloud/analytics, audio cues, left/right-side set doubling, progression-step selection, `strings.xml` extraction for workout-screen chrome, instrumented UI tests, process-death state restoration for an in-progress workout.
