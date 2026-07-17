package com.spoludo.valens.workout.pose.skeleton

private fun lerp(from: Float, to: Float, progress: Float): Float = from + (to - from) * progress

private fun lerp(from: JointRotation, to: JointRotation, progress: Float): JointRotation =
    JointRotation(
        flexionDegrees = lerp(from.flexionDegrees, to.flexionDegrees, progress),
        abductionDegrees = lerp(from.abductionDegrees, to.abductionDegrees, progress),
    )

fun interpolatePose(from: SkeletonPose, to: SkeletonPose, progress: Float): SkeletonPose {
    val clamped = progress.coerceIn(0f, 1f)
    return SkeletonPose(
        rootLeanDegrees = lerp(from.rootLeanDegrees, to.rootLeanDegrees, clamped),
        leftShoulder = lerp(from.leftShoulder, to.leftShoulder, clamped),
        rightShoulder = lerp(from.rightShoulder, to.rightShoulder, clamped),
        leftElbowDegrees = lerp(from.leftElbowDegrees, to.leftElbowDegrees, clamped),
        rightElbowDegrees = lerp(from.rightElbowDegrees, to.rightElbowDegrees, clamped),
        leftHip = lerp(from.leftHip, to.leftHip, clamped),
        rightHip = lerp(from.rightHip, to.rightHip, clamped),
        leftKneeDegrees = lerp(from.leftKneeDegrees, to.leftKneeDegrees, clamped),
        rightKneeDegrees = lerp(from.rightKneeDegrees, to.rightKneeDegrees, clamped),
        leftFootPitchDegrees = lerp(from.leftFootPitchDegrees, to.leftFootPitchDegrees, clamped),
        rightFootPitchDegrees = lerp(from.rightFootPitchDegrees, to.rightFootPitchDegrees, clamped),
    )
}
