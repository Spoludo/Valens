package com.spoludo.valens.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Tempo(
    val eccentricSeconds: Double? = null,
    val bottomPauseSeconds: Double? = null,
    val concentricCue: String? = null,
    val topPauseSeconds: Double? = null,
)

@Serializable
data class Prescription(
    val sets: Int,
    val holdSeconds: Int? = null,
    val durationSeconds: Int? = null,
    val restSeconds: Int,
    val targetReps: Int? = null,
    val intensityTarget: Int? = null,
    val tempo: Tempo? = null,
)
