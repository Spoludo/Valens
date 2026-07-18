package com.spoludo.valens.workout.pose.skeleton

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ForwardKinematicsTest {

    @Test
    fun resolve_neutralPose_hasAllTwentyTwoJoints() {
        val resolved = resolve(SkeletonPose())
        assertEquals(SkeletonJoint.entries.toSet(), resolved.keys)
    }

    @Test
    fun resolve_neutralPose_shouldersAtHalfWidthFromCenter() {
        val resolved = resolve(SkeletonPose())
        val leftShoulder = resolved.getValue(SkeletonJoint.LeftShoulder)
        val rightShoulder = resolved.getValue(SkeletonJoint.RightShoulder)
        assertEquals(-SkeletonProportions.SHOULDER_HALF_WIDTH, leftShoulder.x, 0.001f)
        assertEquals(SkeletonProportions.SHOULDER_HALF_WIDTH, rightShoulder.x, 0.001f)
    }

    @Test
    fun resolve_neutralPose_elbowDirectlyBelowShoulder() {
        val resolved = resolve(SkeletonPose())
        val shoulder = resolved.getValue(SkeletonJoint.RightShoulder)
        val elbow = resolved.getValue(SkeletonJoint.RightElbow)
        assertEquals(shoulder.x, elbow.x, 0.001f)
        assertEquals(shoulder.z, elbow.z, 0.001f)
        assertEquals(shoulder.y - SkeletonProportions.UPPER_ARM_LENGTH, elbow.y, 0.001f)
    }

    @Test
    fun resolve_neutralPose_totalHeightIsElevenPointFiveCranialUnits() {
        val resolved = resolve(SkeletonPose())
        val topOfHead = resolved.getValue(SkeletonJoint.Head).y + SkeletonProportions.HEAD_RADIUS
        val bottomOfFoot = resolved.getValue(SkeletonJoint.RightAnkle).y
        assertEquals(11.5f, topOfHead - bottomOfFoot, 0.001f)
    }

    @Test
    fun resolve_shoulderFlexion90_movesWristAndHandForwardInZ() {
        val pose = SkeletonPose(rightShoulder = JointRotation(flexionDegrees = 90f))
        val resolved = resolve(pose)
        val shoulder = resolved.getValue(SkeletonJoint.RightShoulder)
        assertTrue(resolved.getValue(SkeletonJoint.RightWrist).z > shoulder.z)
        assertTrue(resolved.getValue(SkeletonJoint.RightHand).z > shoulder.z)
    }

    @Test
    fun resolve_hipFlexion90_movesKneeAndAnkleForwardInZ() {
        val pose = SkeletonPose(rightHip = JointRotation(flexionDegrees = 90f))
        val resolved = resolve(pose)
        val hip = resolved.getValue(SkeletonJoint.RightHip)
        assertTrue(resolved.getValue(SkeletonJoint.RightKnee).z > hip.z)
        assertTrue(resolved.getValue(SkeletonJoint.RightAnkle).z > hip.z)
    }

    @Test
    fun resolve_rootLean90_movesSpineTopForwardInZ() {
        val resolved = resolve(SkeletonPose(rootLeanDegrees = 90f))
        assertTrue(resolved.getValue(SkeletonJoint.SpineTop).z > resolved.getValue(SkeletonJoint.Pelvis).z)
    }

    @Test
    fun resolve_rootLean90_doesNotMoveHips() {
        val resolved = resolve(SkeletonPose(rootLeanDegrees = 90f))
        val leftHip = resolved.getValue(SkeletonJoint.LeftHip)
        assertEquals(0f, leftHip.z, 0.001f)
        assertEquals(-SkeletonProportions.PELVIS_HALF_WIDTH, leftHip.x, 0.001f)
    }

    @Test
    fun resolve_abduction90_movesLimbsOutwardOnEachSide() {
        val pose = SkeletonPose(
            leftShoulder = JointRotation(abductionDegrees = 90f),
            rightShoulder = JointRotation(abductionDegrees = 90f),
        )
        val resolved = resolve(pose)
        val leftShoulder = resolved.getValue(SkeletonJoint.LeftShoulder)
        val leftElbow = resolved.getValue(SkeletonJoint.LeftElbow)
        val rightShoulder = resolved.getValue(SkeletonJoint.RightShoulder)
        val rightElbow = resolved.getValue(SkeletonJoint.RightElbow)
        assertTrue("left elbow should move outward (smaller x) than left shoulder", leftElbow.x < leftShoulder.x)
        assertTrue("right elbow should move outward (larger x) than right shoulder", rightElbow.x > rightShoulder.x)
    }

    @Test
    fun resolve_footPitch45_liftsHeelAboveToe() {
        val resolved = resolve(SkeletonPose(rightFootPitchDegrees = 45f))
        val heel = resolved.getValue(SkeletonJoint.RightHeel)
        val toe = resolved.getValue(SkeletonJoint.RightToe)
        assertTrue(heel.y > toe.y)
    }

    @Test
    fun resolve_footPitchZero_isFlatFoot() {
        val resolved = resolve(SkeletonPose())
        val ankle = resolved.getValue(SkeletonJoint.RightAnkle)
        assertEquals(ankle.y, resolved.getValue(SkeletonJoint.RightHeel).y, 0.001f)
        assertEquals(ankle.y, resolved.getValue(SkeletonJoint.RightToe).y, 0.001f)
    }

    @Test
    fun resolve_handPitchZero_handContinuesForearmDirection() {
        val pose = SkeletonPose(rightShoulder = JointRotation(flexionDegrees = 90f))
        val resolved = resolve(pose)
        val wrist = resolved.getValue(SkeletonJoint.RightWrist)
        val hand = resolved.getValue(SkeletonJoint.RightHand)
        // forearm points straight forward (+z) here, so the hand should simply continue that line
        assertEquals(wrist.z + SkeletonProportions.HAND_LENGTH, hand.z, 0.001f)
        assertEquals(wrist.y, hand.y, 0.001f)
    }

    @Test
    fun resolve_handPitch90_movesHandRelativeToWrist() {
        val pose = SkeletonPose(rightShoulder = JointRotation(flexionDegrees = 90f), rightHandPitchDegrees = 90f)
        val resolved = resolve(pose)
        val wrist = resolved.getValue(SkeletonJoint.RightWrist)
        val hand = resolved.getValue(SkeletonJoint.RightHand)
        // pitched 90 degrees off a forward-pointing forearm swings the hand upward instead of continuing forward
        assertEquals(wrist.z, hand.z, 0.001f)
        assertEquals(wrist.y + SkeletonProportions.HAND_LENGTH, hand.y, 0.001f)
    }

    @Test
    fun resolve_handPitch_wallContactScenario_tipsHandUpwardRelativeToWrist() {
        // matches wall_push's actual catalog angles: a forward-and-down-reaching forearm
        val pose = SkeletonPose(
            rightShoulder = JointRotation(flexionDegrees = 45f, abductionDegrees = 10f),
            rightElbowDegrees = 60f,
            rightHandPitchDegrees = 85f,
        )
        val resolved = resolve(pose)
        val wrist = resolved.getValue(SkeletonJoint.RightWrist)
        val hand = resolved.getValue(SkeletonJoint.RightHand)
        assertTrue("hand should tip upward relative to the wrist for a wall-contact palm", hand.y > wrist.y)
    }

    @Test
    fun resolve_handPitch_floorContactScenario_flattensHandBehindWrist() {
        // matches reverse_table_hold's actual catalog angles: a straight-down forearm
        val pose = SkeletonPose(rightHandPitchDegrees = -90f)
        val resolved = resolve(pose)
        val wrist = resolved.getValue(SkeletonJoint.RightWrist)
        val hand = resolved.getValue(SkeletonJoint.RightHand)
        assertEquals(wrist.y, hand.y, 0.001f)
        assertTrue("hand should point back toward the body for a floor-contact palm", hand.z < wrist.z)
    }
}
