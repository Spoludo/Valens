package com.spoludo.valens.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class FatigueCost(
    val global: Int,
    val local: Map<String, Int>,
    val joint: JointFatigueMap,
)
