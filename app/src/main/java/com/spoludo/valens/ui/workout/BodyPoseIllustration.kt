package com.spoludo.valens.ui.workout

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
    val figureColor = MaterialTheme.colorScheme.onSurface
    val propColor = MaterialTheme.colorScheme.outline
    Canvas(
        modifier = modifier.semantics { contentDescription = "Body posture illustration" },
    ) {
        drawProp(pose.prop, propColor)
        drawLimb(pose, figureColor, BodyPoint.LeftShoulder, BodyPoint.LeftElbow, BodyPoint.LeftWrist)
        drawLimb(pose, figureColor, BodyPoint.RightShoulder, BodyPoint.RightElbow, BodyPoint.RightWrist)
        drawLimb(pose, figureColor, BodyPoint.LeftHip, BodyPoint.LeftKnee, BodyPoint.LeftAnkle)
        drawLimb(pose, figureColor, BodyPoint.RightHip, BodyPoint.RightKnee, BodyPoint.RightAnkle)
        drawTorso(pose, figureColor)
        drawJoints(pose, figureColor)
        drawHead(pose, figureColor)
    }
}

private fun DrawScope.scaledPoint(pose: BodyPose, bodyPoint: BodyPoint): Offset {
    val normalized = pose.points.getValue(bodyPoint)
    return Offset(normalized.x * size.width, normalized.y * size.height)
}

private fun DrawScope.drawProp(prop: PoseProp, color: Color) {
    when (prop) {
        PoseProp.WALL -> drawLine(
            color = color,
            start = Offset(size.width * 0.9f, 0f),
            end = Offset(size.width * 0.9f, size.height),
            strokeWidth = 4f,
        )
        PoseProp.FLOOR -> drawLine(
            color = color,
            start = Offset(0f, size.height * 0.9f),
            end = Offset(size.width, size.height * 0.9f),
            strokeWidth = 4f,
        )
        PoseProp.NONE -> Unit
    }
}

private fun DrawScope.drawLimb(pose: BodyPose, color: Color, vararg points: BodyPoint) {
    for (i in 0 until points.size - 1) {
        drawLine(
            color = color,
            start = scaledPoint(pose, points[i]),
            end = scaledPoint(pose, points[i + 1]),
            strokeWidth = 6f,
        )
    }
}

private fun DrawScope.drawTorso(pose: BodyPose, color: Color) {
    val neck = scaledPoint(pose, BodyPoint.Neck)
    val leftHip = scaledPoint(pose, BodyPoint.LeftHip)
    val rightHip = scaledPoint(pose, BodyPoint.RightHip)
    val hipCenter = Offset((leftHip.x + rightHip.x) / 2f, (leftHip.y + rightHip.y) / 2f)
    drawLine(color = color, start = neck, end = hipCenter, strokeWidth = 6f)
}

private fun DrawScope.drawJoints(pose: BodyPose, color: Color) {
    for (bodyPoint in BodyPoint.entries) {
        if (bodyPoint == BodyPoint.Head) continue
        drawCircle(color = color, radius = 5f, center = scaledPoint(pose, bodyPoint))
    }
}

private fun DrawScope.drawHead(pose: BodyPose, color: Color) {
    drawCircle(
        color = color,
        radius = size.minDimension * 0.06f,
        center = scaledPoint(pose, BodyPoint.Head),
    )
}
