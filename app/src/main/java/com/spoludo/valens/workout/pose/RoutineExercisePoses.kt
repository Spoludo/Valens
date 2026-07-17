package com.spoludo.valens.workout.pose

import androidx.compose.ui.geometry.Offset

object RoutineExercisePoses {
    val neutralStandingPose = BodyPose(
        points = mapOf(
            BodyPoint.Head to Offset(0.5f, 0.08f),
            BodyPoint.Neck to Offset(0.5f, 0.16f),
            BodyPoint.LeftShoulder to Offset(0.5f, 0.20f),
            BodyPoint.RightShoulder to Offset(0.5f, 0.20f),
            BodyPoint.LeftElbow to Offset(0.5f, 0.38f),
            BodyPoint.RightElbow to Offset(0.5f, 0.38f),
            BodyPoint.LeftWrist to Offset(0.5f, 0.52f),
            BodyPoint.RightWrist to Offset(0.5f, 0.52f),
            BodyPoint.LeftHand to Offset(0.5f, 0.56f),
            BodyPoint.RightHand to Offset(0.5f, 0.56f),
            BodyPoint.LeftHip to Offset(0.5f, 0.55f),
            BodyPoint.RightHip to Offset(0.5f, 0.55f),
            BodyPoint.LeftKnee to Offset(0.5f, 0.75f),
            BodyPoint.RightKnee to Offset(0.5f, 0.75f),
            BodyPoint.LeftAnkle to Offset(0.5f, 0.95f),
            BodyPoint.RightAnkle to Offset(0.5f, 0.95f),
            BodyPoint.LeftHeel to Offset(0.5f, 0.95f),
            BodyPoint.RightHeel to Offset(0.5f, 0.95f),
            BodyPoint.LeftToe to Offset(0.5f, 0.98f),
            BodyPoint.RightToe to Offset(0.5f, 0.98f),
        ),
    )

    private val wallPushSide = BodyPose(
        points = mapOf(
            BodyPoint.Head to Offset(0.55f, 0.15f),
            BodyPoint.Neck to Offset(0.58f, 0.22f),
            BodyPoint.LeftShoulder to Offset(0.60f, 0.28f),
            BodyPoint.RightShoulder to Offset(0.60f, 0.28f),
            BodyPoint.LeftElbow to Offset(0.72f, 0.32f),
            BodyPoint.RightElbow to Offset(0.72f, 0.32f),
            BodyPoint.LeftWrist to Offset(0.85f, 0.30f),
            BodyPoint.RightWrist to Offset(0.85f, 0.30f),
            BodyPoint.LeftHand to Offset(0.90f, 0.29f),
            BodyPoint.RightHand to Offset(0.90f, 0.29f),
            BodyPoint.LeftHip to Offset(0.45f, 0.55f),
            BodyPoint.RightHip to Offset(0.45f, 0.55f),
            BodyPoint.LeftKnee to Offset(0.40f, 0.75f),
            BodyPoint.RightKnee to Offset(0.40f, 0.75f),
            BodyPoint.LeftAnkle to Offset(0.35f, 0.95f),
            BodyPoint.RightAnkle to Offset(0.35f, 0.95f),
            BodyPoint.LeftHeel to Offset(0.30f, 0.97f),
            BodyPoint.RightHeel to Offset(0.30f, 0.97f),
            BodyPoint.LeftToe to Offset(0.38f, 0.98f),
            BodyPoint.RightToe to Offset(0.38f, 0.98f),
        ),
        prop = PoseProp.WALL,
    )

    private val wallPushFront = BodyPose(
        points = mapOf(
            BodyPoint.Head to Offset(0.5f, 0.12f),
            BodyPoint.Neck to Offset(0.5f, 0.20f),
            BodyPoint.LeftShoulder to Offset(0.38f, 0.24f),
            BodyPoint.RightShoulder to Offset(0.62f, 0.24f),
            BodyPoint.LeftElbow to Offset(0.30f, 0.35f),
            BodyPoint.RightElbow to Offset(0.70f, 0.35f),
            BodyPoint.LeftWrist to Offset(0.28f, 0.46f),
            BodyPoint.RightWrist to Offset(0.72f, 0.46f),
            BodyPoint.LeftHand to Offset(0.27f, 0.50f),
            BodyPoint.RightHand to Offset(0.73f, 0.50f),
            BodyPoint.LeftHip to Offset(0.42f, 0.55f),
            BodyPoint.RightHip to Offset(0.58f, 0.55f),
            BodyPoint.LeftKnee to Offset(0.40f, 0.75f),
            BodyPoint.RightKnee to Offset(0.60f, 0.75f),
            BodyPoint.LeftAnkle to Offset(0.38f, 0.93f),
            BodyPoint.RightAnkle to Offset(0.62f, 0.93f),
            BodyPoint.LeftHeel to Offset(0.37f, 0.95f),
            BodyPoint.RightHeel to Offset(0.63f, 0.95f),
            BodyPoint.LeftToe to Offset(0.38f, 0.98f),
            BodyPoint.RightToe to Offset(0.62f, 0.98f),
        ),
    )

    private val hollowBodyHold = BodyPose(
        points = mapOf(
            BodyPoint.Head to Offset(0.15f, 0.75f),
            BodyPoint.Neck to Offset(0.22f, 0.78f),
            BodyPoint.LeftShoulder to Offset(0.28f, 0.80f),
            BodyPoint.RightShoulder to Offset(0.28f, 0.80f),
            BodyPoint.LeftElbow to Offset(0.10f, 0.72f),
            BodyPoint.RightElbow to Offset(0.10f, 0.72f),
            BodyPoint.LeftWrist to Offset(0.02f, 0.70f),
            BodyPoint.RightWrist to Offset(0.02f, 0.70f),
            BodyPoint.LeftHand to Offset(0.00f, 0.69f),
            BodyPoint.RightHand to Offset(0.00f, 0.69f),
            BodyPoint.LeftHip to Offset(0.50f, 0.82f),
            BodyPoint.RightHip to Offset(0.50f, 0.82f),
            BodyPoint.LeftKnee to Offset(0.72f, 0.78f),
            BodyPoint.RightKnee to Offset(0.72f, 0.78f),
            BodyPoint.LeftAnkle to Offset(0.90f, 0.72f),
            BodyPoint.RightAnkle to Offset(0.90f, 0.72f),
            BodyPoint.LeftHeel to Offset(0.94f, 0.71f),
            BodyPoint.RightHeel to Offset(0.94f, 0.71f),
            BodyPoint.LeftToe to Offset(0.98f, 0.70f),
            BodyPoint.RightToe to Offset(0.98f, 0.70f),
        ),
        prop = PoseProp.FLOOR,
    )

    private val calfRaiseHold = BodyPose(
        points = mapOf(
            BodyPoint.Head to Offset(0.5f, 0.06f),
            BodyPoint.Neck to Offset(0.5f, 0.15f),
            BodyPoint.LeftShoulder to Offset(0.5f, 0.19f),
            BodyPoint.RightShoulder to Offset(0.5f, 0.19f),
            BodyPoint.LeftElbow to Offset(0.5f, 0.37f),
            BodyPoint.RightElbow to Offset(0.5f, 0.37f),
            BodyPoint.LeftWrist to Offset(0.5f, 0.51f),
            BodyPoint.RightWrist to Offset(0.5f, 0.51f),
            BodyPoint.LeftHand to Offset(0.5f, 0.55f),
            BodyPoint.RightHand to Offset(0.5f, 0.55f),
            BodyPoint.LeftHip to Offset(0.5f, 0.54f),
            BodyPoint.RightHip to Offset(0.5f, 0.54f),
            BodyPoint.LeftKnee to Offset(0.5f, 0.74f),
            BodyPoint.RightKnee to Offset(0.5f, 0.74f),
            BodyPoint.LeftAnkle to Offset(0.5f, 0.93f),
            BodyPoint.RightAnkle to Offset(0.5f, 0.93f),
            BodyPoint.LeftHeel to Offset(0.5f, 0.90f),
            BodyPoint.RightHeel to Offset(0.5f, 0.90f),
            BodyPoint.LeftToe to Offset(0.5f, 0.97f),
            BodyPoint.RightToe to Offset(0.5f, 0.97f),
        ),
    )

    private val wallSit = BodyPose(
        points = mapOf(
            BodyPoint.Head to Offset(0.82f, 0.20f),
            BodyPoint.Neck to Offset(0.82f, 0.28f),
            BodyPoint.LeftShoulder to Offset(0.82f, 0.32f),
            BodyPoint.RightShoulder to Offset(0.82f, 0.32f),
            BodyPoint.LeftElbow to Offset(0.75f, 0.45f),
            BodyPoint.RightElbow to Offset(0.75f, 0.45f),
            BodyPoint.LeftWrist to Offset(0.70f, 0.55f),
            BodyPoint.RightWrist to Offset(0.70f, 0.55f),
            BodyPoint.LeftHand to Offset(0.65f, 0.56f),
            BodyPoint.RightHand to Offset(0.65f, 0.56f),
            BodyPoint.LeftHip to Offset(0.82f, 0.55f),
            BodyPoint.RightHip to Offset(0.82f, 0.55f),
            BodyPoint.LeftKnee to Offset(0.55f, 0.55f),
            BodyPoint.RightKnee to Offset(0.55f, 0.55f),
            BodyPoint.LeftAnkle to Offset(0.55f, 0.90f),
            BodyPoint.RightAnkle to Offset(0.55f, 0.90f),
            BodyPoint.LeftHeel to Offset(0.53f, 0.92f),
            BodyPoint.RightHeel to Offset(0.53f, 0.92f),
            BodyPoint.LeftToe to Offset(0.48f, 0.93f),
            BodyPoint.RightToe to Offset(0.48f, 0.93f),
        ),
        prop = PoseProp.WALL,
    )

    private val singleLegBalanceHold = BodyPose(
        points = mapOf(
            BodyPoint.Head to Offset(0.5f, 0.10f),
            BodyPoint.Neck to Offset(0.5f, 0.18f),
            BodyPoint.LeftShoulder to Offset(0.5f, 0.22f),
            BodyPoint.RightShoulder to Offset(0.5f, 0.22f),
            BodyPoint.LeftElbow to Offset(0.35f, 0.30f),
            BodyPoint.RightElbow to Offset(0.65f, 0.30f),
            BodyPoint.LeftWrist to Offset(0.28f, 0.28f),
            BodyPoint.RightWrist to Offset(0.72f, 0.28f),
            BodyPoint.LeftHand to Offset(0.25f, 0.27f),
            BodyPoint.RightHand to Offset(0.75f, 0.27f),
            BodyPoint.LeftHip to Offset(0.5f, 0.55f),
            BodyPoint.RightHip to Offset(0.5f, 0.55f),
            BodyPoint.LeftKnee to Offset(0.62f, 0.62f),
            BodyPoint.RightKnee to Offset(0.5f, 0.75f),
            BodyPoint.LeftAnkle to Offset(0.68f, 0.70f),
            BodyPoint.RightAnkle to Offset(0.5f, 0.95f),
            BodyPoint.LeftHeel to Offset(0.66f, 0.72f),
            BodyPoint.RightHeel to Offset(0.5f, 0.96f),
            BodyPoint.LeftToe to Offset(0.70f, 0.68f),
            BodyPoint.RightToe to Offset(0.5f, 0.99f),
        ),
    )

    private val reverseTableHold = BodyPose(
        points = mapOf(
            BodyPoint.Head to Offset(0.75f, 0.35f),
            BodyPoint.Neck to Offset(0.68f, 0.38f),
            BodyPoint.LeftShoulder to Offset(0.60f, 0.42f),
            BodyPoint.RightShoulder to Offset(0.60f, 0.42f),
            BodyPoint.LeftElbow to Offset(0.55f, 0.60f),
            BodyPoint.RightElbow to Offset(0.55f, 0.60f),
            BodyPoint.LeftWrist to Offset(0.50f, 0.80f),
            BodyPoint.RightWrist to Offset(0.50f, 0.80f),
            BodyPoint.LeftHand to Offset(0.52f, 0.83f),
            BodyPoint.RightHand to Offset(0.52f, 0.83f),
            BodyPoint.LeftHip to Offset(0.42f, 0.45f),
            BodyPoint.RightHip to Offset(0.42f, 0.45f),
            BodyPoint.LeftKnee to Offset(0.25f, 0.55f),
            BodyPoint.RightKnee to Offset(0.25f, 0.55f),
            BodyPoint.LeftAnkle to Offset(0.20f, 0.82f),
            BodyPoint.RightAnkle to Offset(0.20f, 0.82f),
            BodyPoint.LeftHeel to Offset(0.17f, 0.85f),
            BodyPoint.RightHeel to Offset(0.17f, 0.85f),
            BodyPoint.LeftToe to Offset(0.24f, 0.83f),
            BodyPoint.RightToe to Offset(0.24f, 0.83f),
        ),
        prop = PoseProp.FLOOR,
    )

    private val plankHold = BodyPose(
        points = mapOf(
            BodyPoint.Head to Offset(0.85f, 0.35f),
            BodyPoint.Neck to Offset(0.75f, 0.38f),
            BodyPoint.LeftShoulder to Offset(0.68f, 0.40f),
            BodyPoint.RightShoulder to Offset(0.68f, 0.40f),
            BodyPoint.LeftElbow to Offset(0.68f, 0.60f),
            BodyPoint.RightElbow to Offset(0.68f, 0.60f),
            BodyPoint.LeftWrist to Offset(0.68f, 0.78f),
            BodyPoint.RightWrist to Offset(0.68f, 0.78f),
            BodyPoint.LeftHand to Offset(0.70f, 0.80f),
            BodyPoint.RightHand to Offset(0.70f, 0.80f),
            BodyPoint.LeftHip to Offset(0.42f, 0.42f),
            BodyPoint.RightHip to Offset(0.42f, 0.42f),
            BodyPoint.LeftKnee to Offset(0.20f, 0.44f),
            BodyPoint.RightKnee to Offset(0.20f, 0.44f),
            BodyPoint.LeftAnkle to Offset(0.05f, 0.46f),
            BodyPoint.RightAnkle to Offset(0.05f, 0.46f),
            BodyPoint.LeftHeel to Offset(0.03f, 0.42f),
            BodyPoint.RightHeel to Offset(0.03f, 0.42f),
            BodyPoint.LeftToe to Offset(0.02f, 0.50f),
            BodyPoint.RightToe to Offset(0.02f, 0.50f),
        ),
        prop = PoseProp.FLOOR,
    )

    fun targetPoseViewsFor(exerciseId: String): List<BodyPoseView> = when (exerciseId) {
        "wall_push" -> listOf(
            BodyPoseView(PoseViewAngle.SIDE, "Side", wallPushSide),
            BodyPoseView(PoseViewAngle.FRONT, "Front", wallPushFront),
        )
        "hollow_body_hold" -> listOf(BodyPoseView(PoseViewAngle.SIDE, "Side", hollowBodyHold))
        "calf_raise_hold" -> listOf(BodyPoseView(PoseViewAngle.SIDE, "Side", calfRaiseHold))
        "wall_sit" -> listOf(BodyPoseView(PoseViewAngle.SIDE, "Side", wallSit))
        "single_leg_balance_hold" -> listOf(BodyPoseView(PoseViewAngle.FRONT, "Front", singleLegBalanceHold))
        "reverse_table_hold" -> listOf(BodyPoseView(PoseViewAngle.SIDE, "Side", reverseTableHold))
        "plank_hold" -> listOf(BodyPoseView(PoseViewAngle.SIDE, "Side", plankHold))
        else -> emptyList()
    }
}
