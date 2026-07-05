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
