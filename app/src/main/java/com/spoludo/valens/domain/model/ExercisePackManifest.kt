package com.spoludo.valens.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ExercisePackContents(
    val movementPatterns: String,
    val muscles: String,
    val joints: String,
    val exercises: String,
    val translations: String,
)

@Serializable
data class ExercisePackManifest(
    val id: String,
    val schemaVersion: String,
    val version: String,
    val nameKey: LocalizationKey,
    val descriptionKey: LocalizationKey,
    val author: String,
    val license: String,
    val homepage: String? = null,
    val minAppVersion: String? = null,
    val tags: List<String> = emptyList(),
    val contents: ExercisePackContents,
)
