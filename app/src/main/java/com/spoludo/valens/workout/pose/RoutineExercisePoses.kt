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
            BodyPoint.LeftHip to Offset(0.5f, 0.55f),
            BodyPoint.RightHip to Offset(0.5f, 0.55f),
            BodyPoint.LeftKnee to Offset(0.5f, 0.75f),
            BodyPoint.RightKnee to Offset(0.5f, 0.75f),
            BodyPoint.LeftAnkle to Offset(0.5f, 0.95f),
            BodyPoint.RightAnkle to Offset(0.5f, 0.95f),
        ),
    )

    private val wallPush = BodyPose(
        points = mapOf(
            BodyPoint.Head to Offset(0.55f, 0.15f),
            BodyPoint.Neck to Offset(0.58f, 0.22f),
            BodyPoint.LeftShoulder to Offset(0.60f, 0.28f),
            BodyPoint.RightShoulder to Offset(0.60f, 0.28f),
            BodyPoint.LeftElbow to Offset(0.72f, 0.32f),
            BodyPoint.RightElbow to Offset(0.72f, 0.32f),
            BodyPoint.LeftWrist to Offset(0.85f, 0.30f),
            BodyPoint.RightWrist to Offset(0.85f, 0.30f),
            BodyPoint.LeftHip to Offset(0.45f, 0.55f),
            BodyPoint.RightHip to Offset(0.45f, 0.55f),
            BodyPoint.LeftKnee to Offset(0.40f, 0.75f),
            BodyPoint.RightKnee to Offset(0.40f, 0.75f),
            BodyPoint.LeftAnkle to Offset(0.35f, 0.95f),
            BodyPoint.RightAnkle to Offset(0.35f, 0.95f),
        ),
        prop = PoseProp.WALL,
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
            BodyPoint.LeftHip to Offset(0.50f, 0.82f),
            BodyPoint.RightHip to Offset(0.50f, 0.82f),
            BodyPoint.LeftKnee to Offset(0.72f, 0.78f),
            BodyPoint.RightKnee to Offset(0.72f, 0.78f),
            BodyPoint.LeftAnkle to Offset(0.90f, 0.72f),
            BodyPoint.RightAnkle to Offset(0.90f, 0.72f),
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
            BodyPoint.LeftHip to Offset(0.5f, 0.54f),
            BodyPoint.RightHip to Offset(0.5f, 0.54f),
            BodyPoint.LeftKnee to Offset(0.5f, 0.74f),
            BodyPoint.RightKnee to Offset(0.5f, 0.74f),
            BodyPoint.LeftAnkle to Offset(0.5f, 0.93f),
            BodyPoint.RightAnkle to Offset(0.5f, 0.93f),
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
            BodyPoint.LeftHip to Offset(0.82f, 0.55f),
            BodyPoint.RightHip to Offset(0.82f, 0.55f),
            BodyPoint.LeftKnee to Offset(0.55f, 0.55f),
            BodyPoint.RightKnee to Offset(0.55f, 0.55f),
            BodyPoint.LeftAnkle to Offset(0.55f, 0.90f),
            BodyPoint.RightAnkle to Offset(0.55f, 0.90f),
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
            BodyPoint.LeftHip to Offset(0.5f, 0.55f),
            BodyPoint.RightHip to Offset(0.5f, 0.55f),
            BodyPoint.LeftKnee to Offset(0.62f, 0.62f),
            BodyPoint.RightKnee to Offset(0.5f, 0.75f),
            BodyPoint.LeftAnkle to Offset(0.68f, 0.70f),
            BodyPoint.RightAnkle to Offset(0.5f, 0.95f),
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
            BodyPoint.LeftHip to Offset(0.42f, 0.45f),
            BodyPoint.RightHip to Offset(0.42f, 0.45f),
            BodyPoint.LeftKnee to Offset(0.25f, 0.55f),
            BodyPoint.RightKnee to Offset(0.25f, 0.55f),
            BodyPoint.LeftAnkle to Offset(0.20f, 0.82f),
            BodyPoint.RightAnkle to Offset(0.20f, 0.82f),
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
            BodyPoint.LeftHip to Offset(0.42f, 0.42f),
            BodyPoint.RightHip to Offset(0.42f, 0.42f),
            BodyPoint.LeftKnee to Offset(0.20f, 0.44f),
            BodyPoint.RightKnee to Offset(0.20f, 0.44f),
            BodyPoint.LeftAnkle to Offset(0.05f, 0.46f),
            BodyPoint.RightAnkle to Offset(0.05f, 0.46f),
        ),
        prop = PoseProp.FLOOR,
    )

    fun targetPoseFor(exerciseId: String): BodyPose? = when (exerciseId) {
        "wall_push" -> wallPush
        "hollow_body_hold" -> hollowBodyHold
        "calf_raise_hold" -> calfRaiseHold
        "wall_sit" -> wallSit
        "single_leg_balance_hold" -> singleLegBalanceHold
        "reverse_table_hold" -> reverseTableHold
        "plank_hold" -> plankHold
        else -> null
    }
}
