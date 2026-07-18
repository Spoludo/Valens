package com.spoludo.valens.workout.pose.skeleton

data class SkeletonPose(
    val rootLeanDegrees: Float = 0f,
    val leftShoulder: JointRotation = JointRotation(),
    val rightShoulder: JointRotation = JointRotation(),
    val leftElbowDegrees: Float = 0f,
    val rightElbowDegrees: Float = 0f,
    val leftHandPitchDegrees: Float = 0f,
    val rightHandPitchDegrees: Float = 0f,
    val leftHip: JointRotation = JointRotation(),
    val rightHip: JointRotation = JointRotation(),
    val leftKneeDegrees: Float = 0f,
    val rightKneeDegrees: Float = 0f,
    val leftFootPitchDegrees: Float = 0f,
    val rightFootPitchDegrees: Float = 0f,
)

enum class SkeletonJoint {
    Head, Neck, SpineTop, Pelvis,
    LeftShoulder, RightShoulder, LeftElbow, RightElbow, LeftWrist, RightWrist, LeftHand, RightHand,
    LeftHip, RightHip, LeftKnee, RightKnee, LeftAnkle, RightAnkle,
    LeftHeel, RightHeel, LeftToe, RightToe,
}
