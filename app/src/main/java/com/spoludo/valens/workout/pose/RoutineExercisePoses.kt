package com.spoludo.valens.workout.pose

import com.spoludo.valens.workout.pose.skeleton.JointRotation
import com.spoludo.valens.workout.pose.skeleton.SkeletonPose

object RoutineExercisePoses {
    val neutralStandingPose = SkeletonPose()

    private val wallPushPose = SkeletonPose(
        rootLeanDegrees = 28f,
        leftShoulder = JointRotation(flexionDegrees = 45f, abductionDegrees = 10f),
        rightShoulder = JointRotation(flexionDegrees = 45f, abductionDegrees = 10f),
        leftElbowDegrees = 60f,
        rightElbowDegrees = 60f,
        leftHandPitchDegrees = 85f,
        rightHandPitchDegrees = 85f,
        leftHip = JointRotation(flexionDegrees = -28f),
        rightHip = JointRotation(flexionDegrees = -28f),
        leftFootPitchDegrees = 35f,
        rightFootPitchDegrees = 35f,
    )

    private val hollowBodyHoldPose = SkeletonPose(
        rootLeanDegrees = -78f,
        leftShoulder = JointRotation(flexionDegrees = -105f),
        rightShoulder = JointRotation(flexionDegrees = -105f),
        leftHip = JointRotation(flexionDegrees = 100f),
        rightHip = JointRotation(flexionDegrees = 100f),
    )

    private val calfRaiseHoldPose = SkeletonPose(
        leftFootPitchDegrees = 40f,
        rightFootPitchDegrees = 40f,
    )

    private val wallSitPose = SkeletonPose(
        leftHip = JointRotation(flexionDegrees = 90f),
        rightHip = JointRotation(flexionDegrees = 90f),
        leftKneeDegrees = -90f,
        rightKneeDegrees = -90f,
    )

    private val singleLegBalanceHoldPose = SkeletonPose(
        leftHip = JointRotation(flexionDegrees = 45f, abductionDegrees = 10f),
        leftKneeDegrees = -60f,
    )

    private val reverseTableHoldPose = SkeletonPose(
        rootLeanDegrees = -82f,
        leftHip = JointRotation(flexionDegrees = 82f),
        rightHip = JointRotation(flexionDegrees = 82f),
        leftKneeDegrees = -82f,
        rightKneeDegrees = -82f,
        leftHandPitchDegrees = 90f,
        rightHandPitchDegrees = 90f,
    )

    private val plankHoldPose = SkeletonPose(
        rootLeanDegrees = 84f,
        leftHip = JointRotation(flexionDegrees = -84f),
        rightHip = JointRotation(flexionDegrees = -84f),
        leftElbowDegrees = 90f,
        rightElbowDegrees = 90f,
        leftFootPitchDegrees = 88f,
        rightFootPitchDegrees = 88f,
    )

    fun targetPoseViewsFor(exerciseId: String): List<BodyPoseView> = when (exerciseId) {
        "wall_push" -> listOf(
            BodyPoseView(PoseViewAngle.SIDE, "Side", wallPushPose, prop = PoseProp.WALL),
            BodyPoseView(PoseViewAngle.FRONT, "Front", wallPushPose),
        )
        "hollow_body_hold" -> listOf(BodyPoseView(PoseViewAngle.SIDE, "Side", hollowBodyHoldPose, prop = PoseProp.FLOOR))
        "calf_raise_hold" -> listOf(BodyPoseView(PoseViewAngle.SIDE, "Side", calfRaiseHoldPose))
        "wall_sit" -> listOf(BodyPoseView(PoseViewAngle.SIDE, "Side", wallSitPose, prop = PoseProp.WALL, propNearEdge = true))
        "single_leg_balance_hold" -> listOf(BodyPoseView(PoseViewAngle.FRONT, "Front", singleLegBalanceHoldPose))
        "reverse_table_hold" -> listOf(BodyPoseView(PoseViewAngle.SIDE, "Side", reverseTableHoldPose, prop = PoseProp.FLOOR))
        "plank_hold" -> listOf(BodyPoseView(PoseViewAngle.SIDE, "Side", plankHoldPose, prop = PoseProp.FLOOR))
        else -> emptyList()
    }
}
