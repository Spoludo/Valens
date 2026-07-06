package com.spoludo.valens.domain.model

data class ExercisePack(
    val manifest: ExercisePackManifest,
    val movementPatterns: List<MovementPattern>,
    val muscles: List<Muscle>,
    val joints: List<Joint>,
    val exercises: List<Exercise>,
    val translations: Map<String, TranslationBundle>,
)
