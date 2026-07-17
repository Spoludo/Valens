package com.spoludo.valens.workout.pose

import androidx.compose.ui.geometry.Offset

enum class PoseProp { NONE, WALL, FLOOR }

data class BodyPose(
    val points: Map<BodyPoint, Offset>,
    val prop: PoseProp = PoseProp.NONE,
)

enum class PoseViewAngle { SIDE, FRONT, BACK }

data class BodyPoseView(
    val angle: PoseViewAngle,
    val label: String,
    val pose: BodyPose,
)
