package com.spoludo.valens.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class MuscleRegion {
    @SerialName("neck") NECK,
    @SerialName("shoulder") SHOULDER,
    @SerialName("chest") CHEST,
    @SerialName("back") BACK,
    @SerialName("core") CORE,
    @SerialName("hip") HIP,
    @SerialName("thigh") THIGH,
    @SerialName("lower_leg") LOWER_LEG,
    @SerialName("arm") ARM,
    @SerialName("forearm") FOREARM,
    @SerialName("full_body") FULL_BODY,
}

@Serializable
enum class MuscleSide {
    @SerialName("left") LEFT,
    @SerialName("right") RIGHT,
    @SerialName("bilateral") BILATERAL,
    @SerialName("midline") MIDLINE,
}

@Serializable
data class Muscle(
    val id: MuscleId,
    val nameKey: LocalizationKey,
    val region: MuscleRegion,
    val side: MuscleSide? = null,
)

@Serializable
data class MuscleCollection(
    val schemaVersion: String,
    val muscles: List<Muscle>,
)
