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
                    exercise.id,
                    ExercisePackReferenceField.MOVEMENT_PATTERN_ID,
                    exercise.movementPatternId.value,
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
                        exercise.id,
                        ExercisePackReferenceField.REGRESSION,
                        regression.value,
                    )
                }
            }

            for (alternative in exercise.alternatives) {
                if (alternative !in exerciseIds) {
                    errors += ExercisePackValidationError.UnknownReference(
                        exercise.id,
                        ExercisePackReferenceField.ALTERNATIVE,
                        alternative.value,
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
