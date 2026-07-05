package com.spoludo.valens.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Cues(
    val setup: List<LocalizationKey>,
    val during: List<LocalizationKey>,
    val stopIf: List<LocalizationKey>,
)
