package com.spoludo.valens.workout.pose

import com.spoludo.valens.workout.pose.skeleton.JointRotation
import com.spoludo.valens.workout.pose.skeleton.SkeletonPose

object RoutineExercisePoses {
    val neutralStandingPose = SkeletonPose()

    private val wallPushPose = SkeletonPose(
        rootLeanDegrees = 28f,
        leftShoulder = JointRotation(flexionDegrees = 78f, abductionDegrees = 10f),
        rightShoulder = JointRotation(flexionDegrees = 78f, abductionDegrees = 10f),
        leftElbowDegrees = 15f,
        rightElbowDegrees = 15f,
    )

    private val hollowBodyHoldPose = SkeletonPose(
        rootLeanDegrees = 90f,
        leftShoulder = JointRotation(flexionDegrees = 95f),
        rightShoulder = JointRotation(flexionDegrees = 95f),
        leftHip = JointRotation(flexionDegrees = 80f),
        rightHip = JointRotation(flexionDegrees = 80f),
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
        rootLeanDegrees = -60f,
        leftShoulder = JointRotation(flexionDegrees = -70f),
        rightShoulder = JointRotation(flexionDegrees = -70f),
        leftHip = JointRotation(flexionDegrees = 70f),
        rightHip = JointRotation(flexionDegrees = 70f),
        leftKneeDegrees = -90f,
        rightKneeDegrees = -90f,
    )

    private val plankHoldPose = SkeletonPose(
        rootLeanDegrees = 90f,
        leftShoulder = JointRotation(flexionDegrees = 85f),
        rightShoulder = JointRotation(flexionDegrees = 85f),
        leftElbowDegrees = -90f,
        rightElbowDegrees = -90f,
        leftFootPitchDegrees = 20f,
        rightFootPitchDegrees = 20f,
    )

    fun targetPoseViewsFor(exerciseId: String): List<BodyPoseView> = when (exerciseId) {
        "wall_push" -> listOf(
            BodyPoseView(PoseViewAngle.SIDE, "Side", wallPushPose, prop = PoseProp.WALL),
            BodyPoseView(PoseViewAngle.FRONT, "Front", wallPushPose),
        )
        "hollow_body_hold" -> listOf(BodyPoseView(PoseViewAngle.SIDE, "Side", hollowBodyHoldPose, prop = PoseProp.FLOOR))
        "calf_raise_hold" -> listOf(BodyPoseView(PoseViewAngle.SIDE, "Side", calfRaiseHoldPose))
        "wall_sit" -> listOf(BodyPoseView(PoseViewAngle.SIDE, "Side", wallSitPose, prop = PoseProp.WALL))
        "single_leg_balance_hold" -> listOf(BodyPoseView(PoseViewAngle.FRONT, "Front", singleLegBalanceHoldPose))
        "reverse_table_hold" -> listOf(BodyPoseView(PoseViewAngle.SIDE, "Side", reverseTableHoldPose, prop = PoseProp.FLOOR))
        "plank_hold" -> listOf(BodyPoseView(PoseViewAngle.SIDE, "Side", plankHoldPose, prop = PoseProp.FLOOR))
        else -> emptyList()
    }
}
