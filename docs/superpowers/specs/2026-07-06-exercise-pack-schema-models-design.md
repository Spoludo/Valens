# Exercise Pack Schema Models — Design

**Status:** Approved
**Scope:** Typed Kotlin models only. No file I/O, no bundled-pack loading, no cross-reference validation, no planner logic, no Room entities. `ExercisePackLoader` remains an empty placeholder interface for a future commit.

## 1. Purpose

Add `@Serializable` Kotlin data classes and enums that mirror the five canonical JSON Schema files (`schemas/exercise-pack.schema.json`, `exercise.schema.json`, `movement-pattern.schema.json`/`movement-patterns.schema.json`, `muscle.schema.json`/`muscles.schema.json`, `joint.schema.json`/`joints.schema.json`) and the side-aware joint-load model (`docs/06_exercise_model.md` §10, `docs/16_json_schema.md` §7). These become the typed shape the future `ExercisePackLoader` will decode JSON into.

## 2. Package and file layout

All new types live flat under `com.spoludo.valens.domain.model` (matches the package proposal in `docs/03_architecture.md` §9 — no new subpackages). One file per schema concept, all pure Kotlin + kotlinx.serialization (no Android dependency, satisfying "domain must not depend on Android UI").

| File | Contents |
|---|---|
| `Ids.kt` (extend existing) | `ExerciseId`, `MovementPatternId` (already exist), plus new `JointId`, `MuscleId`, `ExerciseFamilyId`, `CapacityId` |
| `LocalizationKey.kt` | `LocalizationKey` value class, for every `*Key` field |
| `JointLoadRole.kt` | `JointLoadRole` enum, plus `JointStressMap` / `JointFatigueMap` typealiases |
| `Joint.kt` | `Joint`, `JointRegion`, `JointSide`, `JointCollection` |
| `Muscle.kt` | `Muscle`, `MuscleRegion`, `MuscleSide`, `MuscleCollection` |
| `MovementPattern.kt` | `MovementPattern`, `MovementDomain`, `WeeklyFrequency`, `MovementPatternCollection` |
| `Exercise.kt` | `Exercise`, `ExerciseType`, `SideModel`, `EquipmentItem`, `ContraindicationFlag` |
| `ExercisePrescription.kt` | `Prescription`, `Tempo` |
| `ExerciseMuscleLoad.kt` | `MuscleLoad`, `Muscles` |
| `ExerciseFatigueCost.kt` | `FatigueCost` |
| `ExerciseProgressionStep.kt` | `ProgressionStep` |
| `ExerciseCues.kt` | `Cues` |
| `ExerciseAssets.kt` | `ExerciseAssets` |
| `ExercisePackManifest.kt` | `ExercisePackManifest`, `ExercisePackContents` |

## 3. ID and map-key strategy (as approved)

Value classes wrap scalar identifier fields only: `ExerciseId`, `MovementPatternId`, `JointId`, `MuscleId`, `ExerciseFamilyId`, `CapacityId`, `LocalizationKey`. These appear as ordinary properties or list elements (e.g. `regressions: List<ExerciseId>`, `typicalJoints: List<JointId>`).

Wherever a schema uses a raw id as a **JSON object key** — `jointStress`, `fatigueCost.joint`, and (by the same reasoning) `fatigueCost.local` — the model uses a plain `String` key, not a value class. Rationale (per user direction): these models mirror external JSON directly; JSON object keys are strings, and typed-ID conversion belongs in a later validation/domain-mapping step, not in the raw schema-decoding layer. Two typealiases capture the double-nested shape:

```kotlin
typealias JointStressMap = Map<String, Map<JointLoadRole, Double>>
typealias JointFatigueMap = Map<String, Map<JointLoadRole, Int>>
```

`fatigueCost.local` (muscle-id-keyed, single-level) is `Map<String, Int>` for the same reason — not called out explicitly in the approved list, but the identical rationale applies, so it's included here for consistency and flagged for visibility.

`ProgressionStep.id` stays `String`, not a new value class — it's a distinct id namespace (e.g. `"wall_sit_120deg_20s"`) but was not part of the approved value-class list, and progression steps aren't cross-referenced by other models in this schema.

## 4. Enum serialization

Every enum entry gets an explicit `@SerialName` matching the exact JSON string, rather than relying on Kotlin enum-name casing:

```kotlin
@Serializable
enum class JointLoadRole {
    @SerialName("bilateral") BILATERAL,
    @SerialName("midline") MIDLINE,
    @SerialName("workingSide") WORKING_SIDE,
    @SerialName("supportSide") SUPPORT_SIDE,
    @SerialName("oppositeSide") OPPOSITE_SIDE,
    @SerialName("left") LEFT,
    @SerialName("right") RIGHT,
}
```

Enums required: `JointLoadRole`, `JointRegion`, `JointSide`, `MuscleRegion`, `MuscleSide`, `MovementDomain`, `ExerciseType`, `SideModel`, `EquipmentItem`, `ContraindicationFlag` — one per closed `enum` in the JSON Schema `$defs`.

## 5. Full model definitions

### `Ids.kt`

```kotlin
package com.spoludo.valens.domain.model

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class ExerciseId(val value: String)

@Serializable
@JvmInline
value class MovementPatternId(val value: String)

@Serializable
@JvmInline
value class JointId(val value: String)

@Serializable
@JvmInline
value class MuscleId(val value: String)

@Serializable
@JvmInline
value class ExerciseFamilyId(val value: String)

@Serializable
@JvmInline
value class CapacityId(val value: String)
```

(`ExerciseId` and `MovementPatternId` already exist without `@Serializable` — this task adds the annotation and the four new value classes.)

### `LocalizationKey.kt`

```kotlin
package com.spoludo.valens.domain.model

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class LocalizationKey(val value: String)
```

### `JointLoadRole.kt`

```kotlin
package com.spoludo.valens.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class JointLoadRole {
    @SerialName("bilateral") BILATERAL,
    @SerialName("midline") MIDLINE,
    @SerialName("workingSide") WORKING_SIDE,
    @SerialName("supportSide") SUPPORT_SIDE,
    @SerialName("oppositeSide") OPPOSITE_SIDE,
    @SerialName("left") LEFT,
    @SerialName("right") RIGHT,
}

typealias JointStressMap = Map<String, Map<JointLoadRole, Double>>
typealias JointFatigueMap = Map<String, Map<JointLoadRole, Int>>
```

### `Joint.kt`

```kotlin
package com.spoludo.valens.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class JointRegion {
    @SerialName("neck") NECK,
    @SerialName("shoulder") SHOULDER,
    @SerialName("elbow") ELBOW,
    @SerialName("wrist") WRIST,
    @SerialName("spine") SPINE,
    @SerialName("hip") HIP,
    @SerialName("knee") KNEE,
    @SerialName("ankle") ANKLE,
    @SerialName("foot") FOOT,
}

@Serializable
enum class JointSide {
    @SerialName("left") LEFT,
    @SerialName("right") RIGHT,
    @SerialName("midline") MIDLINE,
}

@Serializable
data class Joint(
    val id: JointId,
    val nameKey: LocalizationKey,
    val region: JointRegion,
    val sides: List<JointSide>,
    val sensitiveByDefault: Boolean? = null,
)

@Serializable
data class JointCollection(
    val schemaVersion: String,
    val joints: List<Joint>,
)
```

### `Muscle.kt`

```kotlin
package com.spoludo.valens.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class MuscleRegion {
    @SerialName("neck") NECK,
    @SerialName("shoulder") SHOULDER,
    @SerialName("chest") CHEST,
    @SerialName("back") BACK,
    @SerialName("core") CORE,
    @SerialName("hip") HIP,
    @SerialName("thigh") THIGH,
    @SerialName("lower_leg") LOWER_LEG,
    @SerialName("arm") ARM,
    @SerialName("forearm") FOREARM,
    @SerialName("full_body") FULL_BODY,
}

@Serializable
enum class MuscleSide {
    @SerialName("left") LEFT,
    @SerialName("right") RIGHT,
    @SerialName("bilateral") BILATERAL,
    @SerialName("midline") MIDLINE,
}

@Serializable
data class Muscle(
    val id: MuscleId,
    val nameKey: LocalizationKey,
    val region: MuscleRegion,
    val side: MuscleSide? = null,
)

@Serializable
data class MuscleCollection(
    val schemaVersion: String,
    val muscles: List<Muscle>,
)
```

### `MovementPattern.kt`

```kotlin
package com.spoludo.valens.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class MovementDomain {
    @SerialName("strength") STRENGTH,
    @SerialName("mobility") MOBILITY,
    @SerialName("balance") BALANCE,
    @SerialName("endurance") ENDURANCE,
    @SerialName("recovery") RECOVERY,
    @SerialName("assessment") ASSESSMENT,
}

@Serializable
data class WeeklyFrequency(
    val min: Int,
    val target: Int,
    val max: Int,
)

@Serializable
data class MovementPattern(
    val id: MovementPatternId,
    val domain: MovementDomain,
    val category: String,
    val nameKey: LocalizationKey,
    val descriptionKey: LocalizationKey,
    val primaryCapacities: List<CapacityId>,
    val typicalMuscles: List<MuscleId>,
    val typicalJoints: List<JointId>,
    val functionalPurposeKey: LocalizationKey? = null,
    val recommendedWeeklyFrequency: WeeklyFrequency,
    val minimumEffectiveDoseSeconds: Int? = null,
    val recoveryCost: Int,
)

@Serializable
data class MovementPatternCollection(
    val schemaVersion: String,
    val patterns: List<MovementPattern>,
)
```

(`typicalMuscles` and `typicalJoints` are both in the schema's `required` list, so they have no default — they must be present, even if an empty array.)

### `Exercise.kt`

```kotlin
package com.spoludo.valens.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ExerciseType {
    @SerialName("isometric") ISOMETRIC,
    @SerialName("dynamic") DYNAMIC,
    @SerialName("mobility_dynamic") MOBILITY_DYNAMIC,
    @SerialName("mobility_isometric") MOBILITY_ISOMETRIC,
    @SerialName("balance") BALANCE,
    @SerialName("assessment") ASSESSMENT,
    @SerialName("breathing") BREATHING,
}

@Serializable
enum class SideModel {
    @SerialName("bilateral") BILATERAL,
    @SerialName("left_right") LEFT_RIGHT,
    @SerialName("single_side") SINGLE_SIDE,
    @SerialName("midline") MIDLINE,
}

@Serializable
enum class EquipmentItem {
    @SerialName("none") NONE,
    @SerialName("wall") WALL,
    @SerialName("floor") FLOOR,
    @SerialName("chair") CHAIR,
    @SerialName("mat") MAT,
    @SerialName("bar") BAR,
    @SerialName("band") BAND,
    @SerialName("dumbbell") DUMBBELL,
    @SerialName("backpack") BACKPACK,
    @SerialName("timer") TIMER,
}

@Serializable
enum class ContraindicationFlag {
    @SerialName("acute_knee_pain") ACUTE_KNEE_PAIN,
    @SerialName("acute_shoulder_pain") ACUTE_SHOULDER_PAIN,
    @SerialName("acute_wrist_pain") ACUTE_WRIST_PAIN,
    @SerialName("acute_low_back_pain") ACUTE_LOW_BACK_PAIN,
    @SerialName("dizziness") DIZZINESS,
    @SerialName("uncontrolled_hypertension") UNCONTROLLED_HYPERTENSION,
    @SerialName("requires_overhead_tolerance") REQUIRES_OVERHEAD_TOLERANCE,
    @SerialName("requires_floor_access") REQUIRES_FLOOR_ACCESS,
    @SerialName("requires_bar") REQUIRES_BAR,
}

@Serializable
data class Exercise(
    val id: ExerciseId,
    val schemaVersion: String,
    val nameKey: LocalizationKey,
    val descriptionKey: LocalizationKey,
    val type: ExerciseType,
    val movementPatternId: MovementPatternId,
    val exerciseFamilyId: ExerciseFamilyId,
    val difficulty: Int,
    val equipment: List<EquipmentItem>,
    val homeFriendly: Boolean,
    val sideModel: SideModel,
    val defaultPrescription: Prescription,
    val muscles: Muscles,
    val jointStress: JointStressMap,
    val fatigueCost: FatigueCost,
    val progression: List<ProgressionStep>,
    val regressions: List<ExerciseId>,
    val alternatives: List<ExerciseId>,
    val contraindications: List<ContraindicationFlag>,
    val cues: Cues,
    val assets: ExerciseAssets,
)
```

### `ExercisePrescription.kt`

```kotlin
package com.spoludo.valens.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Tempo(
    val eccentricSeconds: Double? = null,
    val bottomPauseSeconds: Double? = null,
    val concentricCue: String? = null,
    val topPauseSeconds: Double? = null,
)

@Serializable
data class Prescription(
    val sets: Int,
    val holdSeconds: Int? = null,
    val durationSeconds: Int? = null,
    val restSeconds: Int,
    val targetReps: Int? = null,
    val intensityTarget: Int? = null,
    val tempo: Tempo? = null,
)
```

### `ExerciseMuscleLoad.kt`

```kotlin
package com.spoludo.valens.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class MuscleLoad(
    val id: MuscleId,
    val load: Double,
)

@Serializable
data class Muscles(
    val primary: List<MuscleLoad>,
    val secondary: List<MuscleLoad>,
    val stabilizers: List<MuscleLoad>,
)
```

### `ExerciseFatigueCost.kt`

```kotlin
package com.spoludo.valens.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class FatigueCost(
    val global: Int,
    val local: Map<String, Int>,
    val joint: JointFatigueMap,
)
```

### `ExerciseProgressionStep.kt`

```kotlin
package com.spoludo.valens.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ProgressionStep(
    val id: String,
    val labelKey: LocalizationKey,
    val difficulty: Int,
    val holdSeconds: Int? = null,
    val sets: Int? = null,
    val restSeconds: Int? = null,
    val kneeAngleDegrees: Int? = null,
    val variationKey: LocalizationKey? = null,
)
```

### `ExerciseCues.kt`

```kotlin
package com.spoludo.valens.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Cues(
    val setup: List<LocalizationKey>,
    val during: List<LocalizationKey>,
    val stopIf: List<LocalizationKey>,
)
```

### `ExerciseAssets.kt`

```kotlin
package com.spoludo.valens.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ExerciseAssets(
    val illustration: String? = null,
    val illustrationStart: String? = null,
    val illustrationHold: String? = null,
    val illustrationMistake: String? = null,
    val muscleMapFront: String? = null,
    val muscleMapBack: String? = null,
    val jointMapFront: String? = null,
    val jointMapBack: String? = null,
)
```

### `ExercisePackManifest.kt`

```kotlin
package com.spoludo.valens.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ExercisePackContents(
    val movementPatterns: String,
    val muscles: String,
    val joints: String,
    val exercises: String,
    val translations: String,
)

@Serializable
data class ExercisePackManifest(
    val id: String,
    val schemaVersion: String,
    val version: String,
    val nameKey: LocalizationKey,
    val descriptionKey: LocalizationKey,
    val author: String,
    val license: String,
    val homepage: String? = null,
    val minAppVersion: String? = null,
    val tags: List<String> = emptyList(),
    val contents: ExercisePackContents,
)
```

## 6. Progression step `additionalProperties: true`

`exercise.schema.json`'s `progression` items allow `additionalProperties: true` — the schema is explicitly open to future custom fields. This commit models only the currently-known fields. Tests that decode `Exercise` snippets containing progression steps must configure `Json { ignoreUnknownKeys = true }` (a test-level `Json` instance concern, not a change to the model itself); full forward-compatible handling of unknown progression fields is deferred to the future loader work.

## 7. Testing plan

All tests decode inline JSON string literals (not files under `exercise-packs/`) using `kotlinx.serialization.json.Json`. Test package mirrors production package: `com.spoludo.valens.domain.model`, under `app/src/test/java/...`.

| Test file | Covers |
|---|---|
| `JointSerializationTest.kt` | Decode a `knee`-shaped joint JSON snippet with `"sides": ["left", "right"]`; assert `sides == listOf(JointSide.LEFT, JointSide.RIGHT)` and `region == JointRegion.KNEE`. |
| `MuscleSerializationTest.kt` | Decode a `quadriceps`-shaped muscle snippet with `"side": "bilateral"`; assert `side == MuscleSide.BILATERAL`. |
| `MovementPatternSerializationTest.kt` | Decode a `squat`-shaped movement-pattern snippet; assert `domain == MovementDomain.STRENGTH`, `recommendedWeeklyFrequency == WeeklyFrequency(1, 3, 5)`. |
| `ExercisePackManifestSerializationTest.kt` | Decode the `isometric-foundations`-shaped `pack.json` snippet; assert `id == "isometric-foundations"`, `contents.exercises == "exercises/"`. |
| `ExerciseSerializationTest.kt` | Two tests: (1) decode a `wall_sit`-shaped snippet (bilateral `sideModel`), assert `jointStress["knee"]?.get(JointLoadRole.BILATERAL) == 0.6`; (2) decode a `single_leg_glute_bridge_hold`-shaped snippet (`left_right` `sideModel`), assert `jointStress["hip"]?.get(JointLoadRole.WORKING_SIDE) == 0.4`. |

The `wall_sit` and `single_leg_glute_bridge_hold` test fixtures reuse the real bundled JSON content (`exercise-packs/bundled/isometric-foundations/exercises/wall_sit.json` and `single_leg_glute_bridge_hold.json`) as inline string literals, so the models are proven against real pack data even though file loading itself is out of scope.

## 8. Out of scope (explicit)

- File I/O / reading from `exercise-packs/` or Android assets at runtime
- Implementing `ExercisePackLoader`
- Cross-reference validation (e.g. does `movementPatternId` exist in the movement-pattern collection)
- Schema-version pattern validation (`^\d+\.\d+\.\d+$`)
- Planner logic
- Room entities
