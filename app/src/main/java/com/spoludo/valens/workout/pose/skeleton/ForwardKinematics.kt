package com.spoludo.valens.workout.pose.skeleton

import kotlin.math.cos
import kotlin.math.sin

private fun sagittalRotate(v: Vec3, degrees: Float): Vec3 {
    val t = Math.toRadians(degrees.toDouble())
    val cosT = cos(t).toFloat()
    val sinT = sin(t).toFloat()
    return Vec3(v.x, v.y * cosT + v.z * sinT, v.z * cosT - v.y * sinT)
}

private fun frontalRotate(v: Vec3, degrees: Float): Vec3 {
    val t = Math.toRadians(degrees.toDouble())
    val cosT = cos(t).toFloat()
    val sinT = sin(t).toFloat()
    return Vec3(v.x * cosT - v.y * sinT, v.x * sinT + v.y * cosT, v.z)
}

private fun limbDirection(rotation: JointRotation, sideSign: Float): Vec3 {
    val flexed = sagittalRotate(Vec3(0f, -1f, 0f), rotation.flexionDegrees)
    return frontalRotate(flexed, rotation.abductionDegrees * sideSign)
}

private fun toeDirection(footPitchDegrees: Float): Vec3 =
    sagittalRotate(Vec3(0f, 0f, 1f), -footPitchDegrees)

fun resolve(pose: SkeletonPose): Map<SkeletonJoint, Vec3> {
    val pelvis = Vec3(0f, 0f, 0f)
    val spineDirection = sagittalRotate(Vec3(0f, 1f, 0f), -pose.rootLeanDegrees)
    val spineTop = pelvis + spineDirection * SkeletonProportions.SPINE_LENGTH
    val neck = spineTop + spineDirection * SkeletonProportions.NECK_LENGTH
    val head = neck + spineDirection * SkeletonProportions.HEAD_RADIUS

    val leftShoulder = spineTop + Vec3(-SkeletonProportions.SHOULDER_HALF_WIDTH, 0f, 0f)
    val rightShoulder = spineTop + Vec3(SkeletonProportions.SHOULDER_HALF_WIDTH, 0f, 0f)
    val leftHip = pelvis + Vec3(-SkeletonProportions.PELVIS_HALF_WIDTH, 0f, 0f)
    val rightHip = pelvis + Vec3(SkeletonProportions.PELVIS_HALF_WIDTH, 0f, 0f)

    val leftUpperArmDir = limbDirection(pose.leftShoulder, sideSign = -1f)
    val leftElbow = leftShoulder + leftUpperArmDir * SkeletonProportions.UPPER_ARM_LENGTH
    val leftForearmDir = sagittalRotate(leftUpperArmDir, pose.leftElbowDegrees)
    val leftWrist = leftElbow + leftForearmDir * SkeletonProportions.FOREARM_LENGTH
    val leftHand = leftWrist + leftForearmDir * SkeletonProportions.HAND_LENGTH

    val rightUpperArmDir = limbDirection(pose.rightShoulder, sideSign = 1f)
    val rightElbow = rightShoulder + rightUpperArmDir * SkeletonProportions.UPPER_ARM_LENGTH
    val rightForearmDir = sagittalRotate(rightUpperArmDir, pose.rightElbowDegrees)
    val rightWrist = rightElbow + rightForearmDir * SkeletonProportions.FOREARM_LENGTH
    val rightHand = rightWrist + rightForearmDir * SkeletonProportions.HAND_LENGTH

    val leftThighDir = limbDirection(pose.leftHip, sideSign = -1f)
    val leftKnee = leftHip + leftThighDir * SkeletonProportions.THIGH_LENGTH
    val leftShinDir = sagittalRotate(leftThighDir, pose.leftKneeDegrees)
    val leftAnkle = leftKnee + leftShinDir * SkeletonProportions.SHIN_LENGTH
    val leftToeDir = toeDirection(pose.leftFootPitchDegrees)
    val leftToe = leftAnkle + leftToeDir * SkeletonProportions.TOE_LENGTH
    val leftHeel = leftAnkle - leftToeDir * SkeletonProportions.HEEL_LENGTH

    val rightThighDir = limbDirection(pose.rightHip, sideSign = 1f)
    val rightKnee = rightHip + rightThighDir * SkeletonProportions.THIGH_LENGTH
    val rightShinDir = sagittalRotate(rightThighDir, pose.rightKneeDegrees)
    val rightAnkle = rightKnee + rightShinDir * SkeletonProportions.SHIN_LENGTH
    val rightToeDir = toeDirection(pose.rightFootPitchDegrees)
    val rightToe = rightAnkle + rightToeDir * SkeletonProportions.TOE_LENGTH
    val rightHeel = rightAnkle - rightToeDir * SkeletonProportions.HEEL_LENGTH

    return mapOf(
        SkeletonJoint.Head to head,
        SkeletonJoint.Neck to neck,
        SkeletonJoint.SpineTop to spineTop,
        SkeletonJoint.Pelvis to pelvis,
        SkeletonJoint.LeftShoulder to leftShoulder,
        SkeletonJoint.RightShoulder to rightShoulder,
        SkeletonJoint.LeftElbow to leftElbow,
        SkeletonJoint.RightElbow to rightElbow,
        SkeletonJoint.LeftWrist to leftWrist,
        SkeletonJoint.RightWrist to rightWrist,
        SkeletonJoint.LeftHand to leftHand,
        SkeletonJoint.RightHand to rightHand,
        SkeletonJoint.LeftHip to leftHip,
        SkeletonJoint.RightHip to rightHip,
        SkeletonJoint.LeftKnee to leftKnee,
        SkeletonJoint.RightKnee to rightKnee,
        SkeletonJoint.LeftAnkle to leftAnkle,
        SkeletonJoint.RightAnkle to rightAnkle,
        SkeletonJoint.LeftHeel to leftHeel,
        SkeletonJoint.RightHeel to rightHeel,
        SkeletonJoint.LeftToe to leftToe,
        SkeletonJoint.RightToe to rightToe,
    )
}
