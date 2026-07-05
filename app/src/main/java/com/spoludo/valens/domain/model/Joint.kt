package com.spoludo.valens.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class JointRegion {
    @SerialName("neck") NECK,
    @SerialName("shoulder") SHOULDER,
    @SerialName("elbow") ELBOW,
    @SerialName("wrist") WRIST,
    @SerialName("spine") SPINE,
    @SerialName("hip") HIP,
    @SerialName("knee") KNEE,
    @SerialName("ankle") ANKLE,
    @SerialName("foot") FOOT,
}

@Serializable
enum class JointSide {
    @SerialName("left") LEFT,
    @SerialName("right") RIGHT,
    @SerialName("midline") MIDLINE,
}

@Serializable
data class Joint(
    val id: JointId,
    val nameKey: LocalizationKey,
    val region: JointRegion,
    val sides: List<JointSide>,
    val sensitiveByDefault: Boolean? = null,
)

@Serializable
data class JointCollection(
    val schemaVersion: String,
    val joints: List<Joint>,
)
