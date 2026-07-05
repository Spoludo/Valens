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
