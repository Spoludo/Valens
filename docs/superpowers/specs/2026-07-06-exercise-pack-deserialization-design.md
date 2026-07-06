# Exercise Pack Deserialization — Design

**Status:** Approved
**Scope:** Decode + aggregate the bundled `isometric-foundations` pack's JSON files into one typed `ExercisePack`, using the schema models added in the previous commit (`feat(domain): add exercise pack schema models`). No file I/O in production code, no Android assets, no `AssetManager`, no on-device runtime loading, no cross-reference validation, no planner logic, no Room entities, no UI changes.

## 1. Purpose

The previous commit added `@Serializable` models for each individual schema concept (`Exercise`, `MovementPattern`, `Muscle`, `Joint`, `ExercisePackManifest`). This commit adds the logic that decodes a full pack's worth of these files and assembles them into one aggregate, plus a translations model, proven against the real bundled pack data. `ExercisePackLoader` — an empty placeholder interface since the skeleton commit — gets its first real method here.

## 2. Files

| File | Layer | Role |
|---|---|---|
| `app/src/main/java/com/spoludo/valens/data/json/ExercisePackJsonSource.kt` | data (interface, zero I/O) | `JsonTextFile` data class + `ExercisePackJsonSource` interface — contract for "give me raw JSON text, by role." No file system, no Android — a future Android-asset-backed implementation plugs in here without touching the parser. |
| `app/src/main/java/com/spoludo/valens/data/json/ExercisePackLoader.kt` | data (interface, modified) | Gains its first real method: `fun load(source: ExercisePackJsonSource): ExercisePack`. |
| `app/src/main/java/com/spoludo/valens/data/json/ExercisePackJsonParser.kt` | data (concrete class) | Implements `ExercisePackLoader`. Does all `kotlinx.serialization` decoding. Depends only on `ExercisePackJsonSource` — never touches disk itself. |
| `app/src/main/java/com/spoludo/valens/domain/model/ExercisePack.kt` | domain | New aggregate type (not `@Serializable` — assembled from several already-decoded pieces, not decoded from one JSON blob). |
| `app/src/main/java/com/spoludo/valens/domain/model/TranslationBundle.kt` | domain | `@Serializable` mirror of `translations/en.json`'s exact current shape. No schema file exists for translations yet, and this commit doesn't add one. |
| `app/src/test/java/com/spoludo/valens/data/json/RealBundledExercisePackJsonSource.kt` | test-only | Walks upward from `System.getProperty("user.dir")` to find `exercise-packs/bundled/isometric-foundations/pack.json`, then implements `ExercisePackJsonSource` by reading the real files from disk. Lives in the test source set only — this is the one place file I/O happens, and it's test tooling, not production code. |
| `app/src/test/java/com/spoludo/valens/data/json/ExercisePackJsonParserTest.kt` | test | Decodes the real bundled pack via `ExercisePackJsonParser` + `RealBundledExercisePackJsonSource`, asserts against known real data. |

## 3. `ExercisePackJsonSource`

```kotlin
package com.spoludo.valens.data.json

data class JsonTextFile(
    val relativePath: String,
    val content: String,
)

interface ExercisePackJsonSource {
    fun manifest(): String
    fun movementPatterns(): String
    fun muscles(): String
    fun joints(): String
    fun exercises(): List<JsonTextFile>
    fun translations(): List<JsonTextFile>
}
```

`exercises()` and `translations()` return `JsonTextFile` (not bare `String`) because those map to directories of files (11 exercise files today, 1 translation file). `relativePath` exists so the parser can sort deterministically before decoding — directory listing order is filesystem-dependent and must not leak into the decoded aggregate's ordering.

## 4. `ExercisePackLoader` / `ExercisePackJsonParser`

```kotlin
package com.spoludo.valens.data.json

interface ExercisePackLoader {
    fun load(source: ExercisePackJsonSource): ExercisePack
}
```

```kotlin
package com.spoludo.valens.data.json

import com.spoludo.valens.domain.model.ExercisePack
import com.spoludo.valens.domain.model.TranslationBundle
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import com.spoludo.valens.domain.model.Exercise
import com.spoludo.valens.domain.model.ExercisePackManifest
import com.spoludo.valens.domain.model.JointCollection
import com.spoludo.valens.domain.model.MovementPatternCollection
import com.spoludo.valens.domain.model.MuscleCollection

class ExercisePackJsonParser : ExercisePackLoader {
    private val json = Json { ignoreUnknownKeys = true }

    override fun load(source: ExercisePackJsonSource): ExercisePack {
        val manifest = json.decodeFromString<ExercisePackManifest>(source.manifest())
        val movementPatterns = json.decodeFromString<MovementPatternCollection>(source.movementPatterns()).patterns
        val muscles = json.decodeFromString<MuscleCollection>(source.muscles()).muscles
        val joints = json.decodeFromString<JointCollection>(source.joints()).joints
        val exercises = source.exercises()
            .sortedBy { it.relativePath }
            .map { json.decodeFromString<Exercise>(it.content) }
        val translations = source.translations()
            .sortedBy { it.relativePath }
            .map { json.decodeFromString<TranslationBundle>(it.content) }
            .associateBy { it.locale }

        return ExercisePack(
            manifest = manifest,
            movementPatterns = movementPatterns,
            muscles = muscles,
            joints = joints,
            exercises = exercises,
            translations = translations,
        )
    }
}
```

Decode failures propagate as raw `kotlinx.serialization.SerializationException` — no `Result` wrapper or custom error type yet. "JSON must decode successfully" is the entire correctness bar for this commit; structured error/validation handling belongs to a future validation commit.

## 5. Domain types

```kotlin
package com.spoludo.valens.domain.model

data class ExercisePack(
    val manifest: ExercisePackManifest,
    val movementPatterns: List<MovementPattern>,
    val muscles: List<Muscle>,
    val joints: List<Joint>,
    val exercises: List<Exercise>,
    val translations: Map<String, TranslationBundle>,
)
```

```kotlin
package com.spoludo.valens.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class TranslationBundle(
    val schemaVersion: String,
    val locale: String,
    val strings: Map<String, String>,
)
```

`translations` is keyed by locale (taken from each decoded bundle's own `locale` field, e.g. `"en"`), not by filename — today there's exactly one bundle (`en`), but the shape supports more without changing the interface.

## 6. Test strategy

One test class, `ExercisePackJsonParserTest`, JVM-only (`app/src/test/...`), no Android imports anywhere in its call chain — satisfied automatically by `ExercisePackJsonParser`/`ExercisePackJsonSource` having no Android dependency.

`RealBundledExercisePackJsonSource` (test-only) locates the pack directory robustly instead of assuming a fixed `../` hop:

```kotlin
package com.spoludo.valens.data.json

import java.io.File

fun findBundledIsometricFoundationsPack(): File {
    var dir: File? = File(System.getProperty("user.dir")).canonicalFile
    while (dir != null) {
        val candidate = File(dir, "exercise-packs/bundled/isometric-foundations/pack.json")
        if (candidate.exists()) {
            return candidate.parentFile
        }
        dir = dir.parentFile
    }
    error("Could not locate exercise-packs/bundled/isometric-foundations from ${System.getProperty("user.dir")}")
}

class RealBundledExercisePackJsonSource(private val root: File) : ExercisePackJsonSource {
    override fun manifest() = File(root, "pack.json").readText()
    override fun movementPatterns() = File(root, "movement-patterns.json").readText()
    override fun muscles() = File(root, "muscles.json").readText()
    override fun joints() = File(root, "joints.json").readText()

    override fun exercises(): List<JsonTextFile> =
        File(root, "exercises").listFiles { file -> file.extension == "json" }
            .orEmpty()
            .map { JsonTextFile(relativePath = "exercises/${it.name}", content = it.readText()) }

    override fun translations(): List<JsonTextFile> =
        File(root, "translations").listFiles { file -> file.extension == "json" }
            .orEmpty()
            .map { JsonTextFile(relativePath = "translations/${it.name}", content = it.readText()) }
}
```

`ExercisePackJsonParserTest` covers exactly:

1. 11 exercises loaded (`pack.exercises.size == 11`)
2. 8 movement patterns loaded (`pack.movementPatterns.size == 8`)
3. `wall_sit` has `jointStress.knee.bilateral == 0.6`
4. `single_leg_glute_bridge_hold` has `jointStress.hip.workingSide == 0.4`
5. English translations load into a map (`pack.translations["en"]?.strings` is non-empty and contains a known key, e.g. `"pack.isometric_foundations.name"`)

No separate synthetic-fixture unit test class is added — the real-file test already exercises the full decode-and-aggregate path end to end, and none was requested.

## 7. Out of scope (explicit)

- Copying/symlinking `exercise-packs/` into `app/src/main/assets/`
- Android `AssetManager` or any on-device runtime loading path
- Cross-reference validation (e.g. does an exercise's `movementPatternId` exist in the decoded movement-pattern list)
- `schemas/translations.schema.json`
- Planner logic, Room entities, UI changes
