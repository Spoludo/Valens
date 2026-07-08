# Exercise Pack Reference Validation — Design

**Status:** Approved
**Scope:** Core + exercise-id references. A pure Kotlin validator that checks cross-references and duplicate ids within an already-decoded `ExercisePack`. No translation-key completeness, no asset-existence checks, no medical/planner semantics, no Room, no Android runtime, no UI.

## 1. Purpose

`ExercisePackJsonParser` (previous commit) decodes JSON into a typed `ExercisePack` but performs no referential-integrity checking — a typo in `movementPatternId` or a dangling `alternatives` entry decodes successfully and silently produces a broken aggregate. This commit adds `ExercisePackValidator`, a pure function over an already-loaded `ExercisePack` that reports every broken reference and duplicate id as a structured error, matching `docs/16_json_schema.md` §10 ("movement pattern references", "muscle references", "anatomical joint references", "regression and alternative references where practical") and `docs/03_architecture.md`'s Exercise Library Engine responsibility ("validates references").

The existing `scripts/validate-exercise-pack.py` already performs an equivalent check for CI/local use outside the JVM. This commit does not replace it; it adds the same guarantee inside the Kotlin domain layer, callable from tests and (in a future commit) from the loader itself.

## 2. Files

| File | Layer | Role |
|---|---|---|
| `app/src/main/java/com/spoludo/valens/domain/model/ExercisePackValidation.kt` | domain (new) | `ExercisePackReferenceField` enum, `DuplicateIdKind` enum, `ExercisePackValidationError` sealed interface, `ExercisePackValidationWarning` marker interface, `ExercisePackValidationResult` data class. |
| `app/src/main/java/com/spoludo/valens/domain/model/ExercisePackValidator.kt` | domain (new) | `ExercisePackValidator` object with `fun validate(pack: ExercisePack): ExercisePackValidationResult`. Pure, no I/O, no Android dependency. |
| `app/src/test/java/com/spoludo/valens/domain/model/ExercisePackValidatorTest.kt` | test (new) | Synthetic-fixture unit tests, one per validation rule, plus the real-bundled-pack success case. |
| `exercise-packs/bundled/isometric-foundations/exercises/*.json` (10 of 11 files) | data (modified) | Correct dangling `regressions`/`alternatives` references so the real pack validates cleanly (see §5). |

Placed in `domain/model` (flat, no new subpackage) to match the existing convention in this module — `ExercisePack.kt`, `TranslationBundle.kt`, etc. all live there, and this validator operates purely on those types with zero I/O, so it belongs beside them rather than in `data/json` (which owns decoding, not referential checks) or a new top-level package (unjustified for one class + one types file).

## 3. Result and error shape

```kotlin
package com.spoludo.valens.domain.model

enum class ExercisePackReferenceField {
    MOVEMENT_PATTERN_ID,
    MUSCLE_PRIMARY,
    MUSCLE_SECONDARY,
    MUSCLE_STABILIZER,
    JOINT_STRESS,
    FATIGUE_COST_JOINT,
    REGRESSION,
    ALTERNATIVE,
}

enum class DuplicateIdKind {
    EXERCISE,
    MOVEMENT_PATTERN,
    MUSCLE,
    JOINT,
}

sealed interface ExercisePackValidationError {
    data class UnknownReference(
        val exerciseId: ExerciseId,
        val field: ExercisePackReferenceField,
        val referencedId: String,
    ) : ExercisePackValidationError

    data class DuplicateId(
        val kind: DuplicateIdKind,
        val id: String,
    ) : ExercisePackValidationError

    data class DuplicateProgressionStepId(
        val exerciseId: ExerciseId,
        val stepId: String,
    ) : ExercisePackValidationError
}

interface ExercisePackValidationWarning

data class ExercisePackValidationResult(
    val errors: List<ExercisePackValidationError>,
    val warnings: List<ExercisePackValidationWarning>,
) {
    val isValid: Boolean get() = errors.isEmpty()
}
```

`UnknownReference` covers rules 1–4, 6, 7 (one shape for every "field on exercise X points at unknown id Y" case — exactly the `exerciseId` / `field` / `referencedId` shape suggested in the task). `DuplicateId` covers rules 9–12 (pack-level duplicates: exercise, movement pattern, muscle, joint). `DuplicateProgressionStepId` is separate because it is scoped to a single exercise (rule 8) rather than the whole pack, so it carries `exerciseId` instead of a `kind`.

`ExercisePackValidationWarning` has no implementations yet — `warnings` is always `emptyList()`. No case in this commit's scope (translation completeness, asset existence, biomechanical plausibility) is in scope for a warning either, per the task's explicit exclusion list, so inventing one would be speculative. Left as a plain (non-sealed) `interface` so a future commit can add a warning type without editing this file.

Rule 5 (side-role keys in `jointStress` / `fatigueCost.joint` are valid `JointLoadRole` values) is enforced by the type system already: both maps decode as `Map<String, Map<JointLoadRole, Double/Int>>`, so an invalid role string fails `kotlinx.serialization` decoding before an `ExercisePack` even exists. There is nothing left for this validator to check, and no error case is defined for it. One test proves this via inline JSON decoding rather than through the validator.

## 4. Validation logic

```kotlin
package com.spoludo.valens.domain.model

object ExercisePackValidator {
    fun validate(pack: ExercisePack): ExercisePackValidationResult {
        val errors = mutableListOf<ExercisePackValidationError>()

        errors += duplicates(pack.movementPatterns.map { it.id.value }, DuplicateIdKind.MOVEMENT_PATTERN)
        errors += duplicates(pack.muscles.map { it.id.value }, DuplicateIdKind.MUSCLE)
        errors += duplicates(pack.joints.map { it.id.value }, DuplicateIdKind.JOINT)
        errors += duplicates(pack.exercises.map { it.id.value }, DuplicateIdKind.EXERCISE)

        val movementPatternIds = pack.movementPatterns.map { it.id }.toSet()
        val muscleIds = pack.muscles.map { it.id }.toSet()
        val jointIds = pack.joints.map { it.id.value }.toSet()
        val exerciseIds = pack.exercises.map { it.id }.toSet()
        val progressionStepIds = pack.exercises.flatMap { it.progression.map { step -> step.id } }.toSet()

        for (exercise in pack.exercises) {
            if (exercise.movementPatternId !in movementPatternIds) {
                errors += ExercisePackValidationError.UnknownReference(
                    exercise.id, ExercisePackReferenceField.MOVEMENT_PATTERN_ID, exercise.movementPatternId.value,
                )
            }

            errors += unknownMuscles(exercise, exercise.muscles.primary, ExercisePackReferenceField.MUSCLE_PRIMARY, muscleIds)
            errors += unknownMuscles(exercise, exercise.muscles.secondary, ExercisePackReferenceField.MUSCLE_SECONDARY, muscleIds)
            errors += unknownMuscles(exercise, exercise.muscles.stabilizers, ExercisePackReferenceField.MUSCLE_STABILIZER, muscleIds)

            errors += unknownJoints(exercise, exercise.jointStress.keys, ExercisePackReferenceField.JOINT_STRESS, jointIds)
            errors += unknownJoints(exercise, exercise.fatigueCost.joint.keys, ExercisePackReferenceField.FATIGUE_COST_JOINT, jointIds)

            for (regression in exercise.regressions) {
                if (regression !in exerciseIds && regression.value !in progressionStepIds) {
                    errors += ExercisePackValidationError.UnknownReference(
                        exercise.id, ExercisePackReferenceField.REGRESSION, regression.value,
                    )
                }
            }

            for (alternative in exercise.alternatives) {
                if (alternative !in exerciseIds) {
                    errors += ExercisePackValidationError.UnknownReference(
                        exercise.id, ExercisePackReferenceField.ALTERNATIVE, alternative.value,
                    )
                }
            }

            val seenStepIds = mutableSetOf<String>()
            for (step in exercise.progression) {
                if (!seenStepIds.add(step.id)) {
                    errors += ExercisePackValidationError.DuplicateProgressionStepId(exercise.id, step.id)
                }
            }
        }

        return ExercisePackValidationResult(errors = errors, warnings = emptyList())
    }

    private fun duplicates(ids: List<String>, kind: DuplicateIdKind): List<ExercisePackValidationError> =
        ids.groupingBy { it }.eachCount()
            .filterValues { it > 1 }
            .keys
            .map { ExercisePackValidationError.DuplicateId(kind, it) }

    private fun unknownMuscles(
        exercise: Exercise,
        loads: List<MuscleLoad>,
        field: ExercisePackReferenceField,
        knownMuscleIds: Set<MuscleId>,
    ): List<ExercisePackValidationError> =
        loads.filter { it.id !in knownMuscleIds }
            .map { ExercisePackValidationError.UnknownReference(exercise.id, field, it.id.value) }

    private fun unknownJoints(
        exercise: Exercise,
        jointKeys: Set<String>,
        field: ExercisePackReferenceField,
        knownJointIds: Set<String>,
    ): List<ExercisePackValidationError> =
        jointKeys.filter { it !in knownJointIds }
            .map { ExercisePackValidationError.UnknownReference(exercise.id, field, it) }
}
```

Regression ids are checked against the **pack-wide** set of progression step ids, not just the same exercise's own steps — rule 6 says "known progression step ids" without scoping it to the referencing exercise, and the real data relies on this (see §5: `horse_stance` references `wall_sit`'s own progression step id `wall_sit_120deg_20s`).

Progression step duplicate checking is scoped per exercise (rule 8: "unique within each exercise") — the same step id (e.g. `two_leg_20s`) legitimately appears in more than one exercise's progression list today and must not be flagged.

## 5. Real bundled pack data fix

Cross-checking the real pack surfaced that 10 of 11 exercises' `regressions` (and 2 exercises' `alternatives`) reference ids that are neither real exercise ids nor real progression-step ids anywhere in the pack — e.g. `wall_sit.json` had `"regressions": ["wall_sit_120deg", "chair_sit_hold"]`, but the real progression step id is `wall_sit_120deg_20s` (missing suffix) and no `chair_sit_hold` exercise exists. This matches `docs/06_exercise_model.md` §16's example verbatim, so it reads as placeholder/aspirational content rather than a one-off typo.

Decision (confirmed with the user): fix the data, not the rule. `regressions`/`alternatives` stay hard errors exactly as specified; the bundled pack is corrected so every reference resolves to a real exercise id or a real progression-step id. Fix strategy, applied per exercise:

- If the invalid id is a near-exact match for one of the exercise's own progression step ids (a missing/altered suffix), correct the typo in place (`wall_sit_120deg` → `wall_sit_120deg_20s`, used both in `wall_sit.json` itself and in `horse_stance.json`'s regressions, which references it cross-exercise).
- Otherwise, replace the invalid id with the exercise's own next-easiest unused progression step id (progression arrays are already ordered ascending by `difficulty`, confirmed for all 11 files) — this is always a legitimate regression target and requires inventing no new exercise concepts.
- For `alternatives`, which must resolve to a real *exercise* id (not a progression step), drop `supported_split_squat_hold` from `horse_stance.json` and `wall_sit.json` — no matching exercise exists in this pack and alternatives has no `minItems` constraint, so each file keeps its one remaining valid entry (`wall_sit` / `horse_stance` respectively) rather than fabricating a new exercise reference.

Per-file changes:

| File | `regressions` before → after | `alternatives` before → after |
|---|---|---|
| `bear_crawl_hold.json` | `["tabletop_hold"]` → `["knees_low_10s"]` | unchanged |
| `calf_raise_hold.json` | `["supported_calf_raise_hold"]` → `["two_leg_20s"]` | unchanged |
| `hollow_body_hold.json` | `["dead_bug_hold", "tucked_hollow_hold"]` → `["tucked_15s", "tucked_25s"]` | unchanged |
| `horse_stance.json` | `["high_horse_stance", "wall_sit_120deg"]` → `["high_stance_15s", "wall_sit_120deg_20s"]` | `["wall_sit", "supported_split_squat_hold"]` → `["wall_sit"]` |
| `pike_pushup_hold.json` | `["downward_dog_hold", "wall_push"]` → `["high_pike_10s", "wall_push"]` | unchanged |
| `plank_hold.json` | `["kneeling_plank_hold"]` → `["knees_20s"]` | unchanged |
| `reverse_table_hold.json` | `["glute_bridge_hold"]` → `["low_bridge_15s"]` | unchanged |
| `single_leg_balance_hold.json` | `["supported_single_leg_balance"]` → `["supported_15s"]` | unchanged |
| `single_leg_glute_bridge_hold.json` | `["two_leg_glute_bridge_hold"]` → `["two_leg_20s"]` | unchanged |
| `wall_push.json` | `["gentle_wall_push"]` → `["easy_6x8s"]` | unchanged |
| `wall_sit.json` | `["wall_sit_120deg", "chair_sit_hold"]` → `["wall_sit_120deg_20s", "wall_sit_110deg_30s"]` | `["horse_stance", "supported_split_squat_hold"]` → `["horse_stance"]` |

`schemas/exercise.schema.json` already allows `regressions`/`alternatives` arrays of any length ≥ 0 with `uniqueItems: true` — no schema change needed, and `python3 scripts/validate-exercise-pack.py` (which doesn't check regression/alternative references at all) is unaffected by this data change.

## 6. Testing plan

`ExercisePackValidatorTest` (`app/src/test/java/com/spoludo/valens/domain/model/`), synthetic fixtures built by direct Kotlin construction (`Exercise(...)`, `MovementPattern(...)`, etc. — no raw JSON), plus one real-pack integration case:

| Test | Fixture | Assertion |
|---|---|---|
| `validate_realBundledPack_hasNoErrors` | `ExercisePackJsonParser().load(RealBundledExercisePackJsonSource(...))` (reused from `data.json` test package) | `result.isValid`, `result.errors.isEmpty()` |
| `validate_minimalValidPack_hasNoErrors` | One minimal valid exercise + matching pattern/muscle/joint | `result.isValid` |
| `validate_unknownMovementPatternId_reportsError` | Exercise with `movementPatternId = "unknown_pattern"` | Contains `UnknownReference(exerciseId, MOVEMENT_PATTERN_ID, "unknown_pattern")` |
| `validate_unknownMuscleId_reportsError` | Exercise with `muscles.primary` referencing `"unknown_muscle"` | Contains `UnknownReference(exerciseId, MUSCLE_PRIMARY, "unknown_muscle")` |
| `validate_unknownJointInJointStress_reportsError` | Exercise with `jointStress = {"unknown_joint": {...}}` | Contains `UnknownReference(exerciseId, JOINT_STRESS, "unknown_joint")` |
| `validate_unknownJointInFatigueCost_reportsError` | Exercise with `fatigueCost.joint = {"unknown_joint": {...}}` | Contains `UnknownReference(exerciseId, FATIGUE_COST_JOINT, "unknown_joint")` |
| `validate_invalidAlternativeId_reportsError` | Exercise with `alternatives = ["nonexistent"]` | Contains `UnknownReference(exerciseId, ALTERNATIVE, "nonexistent")` |
| `validate_invalidRegressionId_reportsError` | Exercise with `regressions = ["nonexistent"]` | Contains `UnknownReference(exerciseId, REGRESSION, "nonexistent")` |
| `validate_regressionReferencingKnownProgressionStepId_isValid` | Exercise A's `regressions` references exercise B's real progression step id | No `REGRESSION` error (proves the pack-wide lookup from §4) |
| `validate_duplicateExerciseId_reportsError` | Two exercises sharing `id = "dup"` | Contains `DuplicateId(EXERCISE, "dup")` |
| `validate_duplicateMovementPatternId_reportsError` | Two movement patterns sharing `id = "squat"` | Contains `DuplicateId(MOVEMENT_PATTERN, "squat")` |
| `validate_duplicateMuscleId_reportsError` | Two muscles sharing `id = "quadriceps"` | Contains `DuplicateId(MUSCLE, "quadriceps")` |
| `validate_duplicateJointId_reportsError` | Two joints sharing `id = "knee"` | Contains `DuplicateId(JOINT, "knee")` |
| `validate_duplicateProgressionStepId_reportsError` | One exercise with two progression steps sharing `id = "step_a"` | Contains `DuplicateProgressionStepId(exerciseId, "step_a")` |

Invalid `JointLoadRole` side-role keys are not tested through the validator (structurally impossible post-decode, per §3) — no raw-JSON fixture test is added for it since the task marks this optional and impractical.

## 7. Out of scope (explicit, per task)

- Translation key completeness
- Asset file existence (illustrations, muscle/joint map SVGs)
- Medical safety semantics, planner suitability
- Whether alternatives are biomechanically ideal or regressions are strictly easier
- Room persistence, Android runtime asset packaging, UI rendering
- Planner logic, workout engine logic
- Wiring `ExercisePackValidator` into `ExercisePackJsonParser`/`ExercisePackLoader` (stays a standalone, separately-callable validator this commit; integration into the load path is a future decision)
