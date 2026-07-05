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
