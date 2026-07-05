package com.spoludo.valens.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class JointLoadRole {
    @SerialName("bilateral") BILATERAL,
    @SerialName("midline") MIDLINE,
    @SerialName("workingSide") WORKING_SIDE,
    @SerialName("supportSide") SUPPORT_SIDE,
    @SerialName("oppositeSide") OPPOSITE_SIDE,
    @SerialName("left") LEFT,
    @SerialName("right") RIGHT,
}

typealias JointStressMap = Map<String, Map<JointLoadRole, Double>>
typealias JointFatigueMap = Map<String, Map<JointLoadRole, Int>>
