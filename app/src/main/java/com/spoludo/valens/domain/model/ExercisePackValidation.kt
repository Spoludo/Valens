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
