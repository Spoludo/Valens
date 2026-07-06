package com.spoludo.valens.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class TranslationBundle(
    val schemaVersion: String,
    val locale: String,
    val strings: Map<String, String>,
)
