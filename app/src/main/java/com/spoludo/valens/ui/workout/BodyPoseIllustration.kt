package com.spoludo.valens.ui.workout

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.spoludo.valens.workout.pose.BodyPoint
import com.spoludo.valens.workout.pose.BodyPose
import com.spoludo.valens.workout.pose.PoseProp
import com.spoludo.valens.workout.pose.RoutineExercisePoses
import com.spoludo.valens.workout.pose.interpolatePose

@Composable
fun BodyPoseIllustration(
    targetPose: BodyPose?,
    modifier: Modifier = Modifier,
    progressToTarget: Float = 1f,
) {
    val pose = if (targetPose == null) {
        RoutineExercisePoses.neutralStandingPose
    } else {
        interpolatePose(RoutineExercisePoses.neutralStandingPose, targetPose, progressToTarget)
    }
    Canvas(modifier = modifier) {
        drawProp(pose.prop)
        drawLimb(pose, BodyPoint.LeftShoulder, BodyPoint.LeftElbow, BodyPoint.LeftWrist)
        drawLimb(pose, BodyPoint.RightShoulder, BodyPoint.RightElbow, BodyPoint.RightWrist)
        drawLimb(pose, BodyPoint.LeftHip, BodyPoint.LeftKnee, BodyPoint.LeftAnkle)
        drawLimb(pose, BodyPoint.RightHip, BodyPoint.RightKnee, BodyPoint.RightAnkle)
        drawTorso(pose)
        drawJoints(pose)
        drawHead(pose)
    }
}

private fun DrawScope.scaledPoint(pose: BodyPose, bodyPoint: BodyPoint): Offset {
    val normalized = pose.points.getValue(bodyPoint)
    return Offset(normalized.x * size.width, normalized.y * size.height)
}

private fun DrawScope.drawProp(prop: PoseProp) {
    when (prop) {
        PoseProp.WALL -> drawLine(
            color = Color.LightGray,
            start = Offset(size.width * 0.9f, 0f),
            end = Offset(size.width * 0.9f, size.height),
            strokeWidth = 4f,
        )
        PoseProp.FLOOR -> drawLine(
            color = Color.LightGray,
            start = Offset(0f, size.height * 0.9f),
            end = Offset(size.width, size.height * 0.9f),
            strokeWidth = 4f,
        )
        PoseProp.NONE -> Unit
    }
}

private fun DrawScope.drawLimb(pose: BodyPose, vararg points: BodyPoint) {
    for (i in 0 until points.size - 1) {
        drawLine(
            color = Color.DarkGray,
            start = scaledPoint(pose, points[i]),
            end = scaledPoint(pose, points[i + 1]),
            strokeWidth = 6f,
        )
    }
}

private fun DrawScope.drawTorso(pose: BodyPose) {
    val neck = scaledPoint(pose, BodyPoint.Neck)
    val leftHip = scaledPoint(pose, BodyPoint.LeftHip)
    val rightHip = scaledPoint(pose, BodyPoint.RightHip)
    val hipCenter = Offset((leftHip.x + rightHip.x) / 2f, (leftHip.y + rightHip.y) / 2f)
    drawLine(color = Color.DarkGray, start = neck, end = hipCenter, strokeWidth = 6f)
}

private fun DrawScope.drawJoints(pose: BodyPose) {
    for (bodyPoint in BodyPoint.entries) {
        if (bodyPoint == BodyPoint.Head) continue
        drawCircle(color = Color.DarkGray, radius = 5f, center = scaledPoint(pose, bodyPoint))
    }
}

private fun DrawScope.drawHead(pose: BodyPose) {
    drawCircle(
        color = Color.DarkGray,
        radius = size.minDimension * 0.06f,
        center = scaledPoint(pose, BodyPoint.Head),
    )
}
