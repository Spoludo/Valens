package com.spoludo.valens.workout.pose.skeleton

import org.junit.Assert.assertEquals
import org.junit.Test

class PoseInterpolationTest {
    private val from = SkeletonPose(
        rootLeanDegrees = 0f,
        rightShoulder = JointRotation(flexionDegrees = 0f, abductionDegrees = 0f),
        rightFootPitchDegrees = 0f,
        rightHandPitchDegrees = 0f,
    )
    private val to = SkeletonPose(
        rootLeanDegrees = 30f,
        rightShoulder = JointRotation(flexionDegrees = 90f, abductionDegrees = 20f),
        rightFootPitchDegrees = 40f,
        rightHandPitchDegrees = 80f,
    )

    @Test
    fun interpolatePose_atProgressZero_returnsFromAngles() {
        val result = interpolatePose(from, to, 0f)
        assertEquals(0f, result.rootLeanDegrees, 0.001f)
        assertEquals(0f, result.rightShoulder.flexionDegrees, 0.001f)
        assertEquals(0f, result.rightFootPitchDegrees, 0.001f)
        assertEquals(0f, result.rightHandPitchDegrees, 0.001f)
    }

    @Test
    fun interpolatePose_atProgressOne_returnsToAngles() {
        val result = interpolatePose(from, to, 1f)
        assertEquals(30f, result.rootLeanDegrees, 0.001f)
        assertEquals(90f, result.rightShoulder.flexionDegrees, 0.001f)
        assertEquals(40f, result.rightFootPitchDegrees, 0.001f)
        assertEquals(80f, result.rightHandPitchDegrees, 0.001f)
    }

    @Test
    fun interpolatePose_atProgressHalf_returnsMidpoint() {
        val result = interpolatePose(from, to, 0.5f)
        assertEquals(15f, result.rootLeanDegrees, 0.001f)
        assertEquals(45f, result.rightShoulder.flexionDegrees, 0.001f)
        assertEquals(10f, result.rightShoulder.abductionDegrees, 0.001f)
        assertEquals(20f, result.rightFootPitchDegrees, 0.001f)
        assertEquals(40f, result.rightHandPitchDegrees, 0.001f)
    }

    @Test
    fun interpolatePose_clampsProgressAboveOne() {
        val result = interpolatePose(from, to, 1.5f)
        assertEquals(30f, result.rootLeanDegrees, 0.001f)
    }

    @Test
    fun interpolatePose_clampsProgressBelowZero() {
        val result = interpolatePose(from, to, -0.5f)
        assertEquals(0f, result.rootLeanDegrees, 0.001f)
    }
}
