package com.spoludo.valens.workout.pose

import com.spoludo.valens.workout.pose.skeleton.SkeletonPose

data class BodyPoseView(
    val angle: PoseViewAngle,
    val label: String,
    val pose: SkeletonPose,
    val prop: PoseProp = PoseProp.NONE,
)
