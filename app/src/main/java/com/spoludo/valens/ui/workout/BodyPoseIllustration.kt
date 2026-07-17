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
import com.spoludo.valens.workout.pose.PoseProp
import com.spoludo.valens.workout.pose.PoseViewAngle
import com.spoludo.valens.workout.pose.RoutineExercisePoses
import com.spoludo.valens.workout.pose.skeleton.ProjectedPoint
import com.spoludo.valens.workout.pose.skeleton.SkeletonJoint
import com.spoludo.valens.workout.pose.skeleton.SkeletonPose
import com.spoludo.valens.workout.pose.skeleton.interpolatePose
import com.spoludo.valens.workout.pose.skeleton.project
import com.spoludo.valens.workout.pose.skeleton.resolve

@Composable
fun BodyPoseIllustration(
    pose: SkeletonPose?,
    angle: PoseViewAngle,
    modifier: Modifier = Modifier,
    progressToTarget: Float = 1f,
    prop: PoseProp = PoseProp.NONE,
    accessibilityDescription: String = "Body posture illustration",
) {
    val target = pose ?: RoutineExercisePoses.neutralStandingPose
    val interpolated = interpolatePose(RoutineExercisePoses.neutralStandingPose, target, progressToTarget)
    val projected = project(resolve(interpolated), angle)
    val figureColor = MaterialTheme.colorScheme.onSurface
    val propColor = MaterialTheme.colorScheme.outline
    Canvas(
        modifier = modifier.semantics { contentDescription = accessibilityDescription },
    ) {
        val fitted = fitToCanvas(projected, size.width, size.height)
        drawProp(prop, propColor)
        drawLimb(fitted, figureColor, SkeletonJoint.LeftShoulder, SkeletonJoint.LeftElbow, SkeletonJoint.LeftWrist, SkeletonJoint.LeftHand)
        drawLimb(fitted, figureColor, SkeletonJoint.RightShoulder, SkeletonJoint.RightElbow, SkeletonJoint.RightWrist, SkeletonJoint.RightHand)
        drawLimb(fitted, figureColor, SkeletonJoint.LeftHip, SkeletonJoint.LeftKnee, SkeletonJoint.LeftAnkle)
        drawLimb(fitted, figureColor, SkeletonJoint.RightHip, SkeletonJoint.RightKnee, SkeletonJoint.RightAnkle)
        drawFoot(fitted, figureColor, SkeletonJoint.LeftAnkle, SkeletonJoint.LeftHeel, SkeletonJoint.LeftToe)
        drawFoot(fitted, figureColor, SkeletonJoint.RightAnkle, SkeletonJoint.RightHeel, SkeletonJoint.RightToe)
        drawSpine(fitted, figureColor)
        drawShoulderLine(fitted, figureColor)
        drawPelvisLine(fitted, figureColor)
        drawJoints(fitted, figureColor)
        drawHead(fitted, figureColor)
    }
}

private fun fitToCanvas(
    projected: Map<SkeletonJoint, ProjectedPoint>,
    canvasWidth: Float,
    canvasHeight: Float,
): Map<SkeletonJoint, Offset> {
    val minX = projected.values.minOf { it.x }
    val maxX = projected.values.maxOf { it.x }
    val minY = projected.values.minOf { it.y }
    val maxY = projected.values.maxOf { it.y }
    val poseWidth = (maxX - minX).coerceAtLeast(0.01f)
    val poseHeight = (maxY - minY).coerceAtLeast(0.01f)
    val margin = 0.85f
    val scale = minOf(canvasWidth / poseWidth, canvasHeight / poseHeight) * margin
    val poseCenterX = (minX + maxX) / 2f
    val poseCenterY = (minY + maxY) / 2f
    val canvasCenterX = canvasWidth / 2f
    val canvasCenterY = canvasHeight / 2f
    return projected.mapValues { (_, point) ->
        Offset(
            canvasCenterX + (point.x - poseCenterX) * scale,
            canvasCenterY + (point.y - poseCenterY) * scale,
        )
    }
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

private fun DrawScope.drawLimb(fitted: Map<SkeletonJoint, Offset>, color: Color, vararg joints: SkeletonJoint) {
    for (i in 0 until joints.size - 1) {
        drawLine(
            color = color,
            start = fitted.getValue(joints[i]),
            end = fitted.getValue(joints[i + 1]),
            strokeWidth = 6f,
        )
    }
}

private fun DrawScope.drawFoot(fitted: Map<SkeletonJoint, Offset>, color: Color, ankle: SkeletonJoint, heel: SkeletonJoint, toe: SkeletonJoint) {
    val anklePoint = fitted.getValue(ankle)
    drawLine(color = color, start = anklePoint, end = fitted.getValue(heel), strokeWidth = 4f)
    drawLine(color = color, start = anklePoint, end = fitted.getValue(toe), strokeWidth = 4f)
}

private fun DrawScope.drawSpine(fitted: Map<SkeletonJoint, Offset>, color: Color) {
    drawLimb(fitted, color, SkeletonJoint.Pelvis, SkeletonJoint.SpineTop, SkeletonJoint.Neck, SkeletonJoint.Head)
}

private fun DrawScope.drawShoulderLine(fitted: Map<SkeletonJoint, Offset>, color: Color) {
    drawLine(color = color, start = fitted.getValue(SkeletonJoint.LeftShoulder), end = fitted.getValue(SkeletonJoint.RightShoulder), strokeWidth = 6f)
}

private fun DrawScope.drawPelvisLine(fitted: Map<SkeletonJoint, Offset>, color: Color) {
    drawLine(color = color, start = fitted.getValue(SkeletonJoint.LeftHip), end = fitted.getValue(SkeletonJoint.RightHip), strokeWidth = 6f)
}

private fun DrawScope.drawJoints(fitted: Map<SkeletonJoint, Offset>, color: Color) {
    for (joint in SkeletonJoint.entries) {
        if (joint == SkeletonJoint.Head) continue
        drawCircle(color = color, radius = 5f, center = fitted.getValue(joint))
    }
}

private fun DrawScope.drawHead(fitted: Map<SkeletonJoint, Offset>, color: Color) {
    drawCircle(
        color = color,
        radius = size.minDimension * 0.06f,
        center = fitted.getValue(SkeletonJoint.Head),
    )
}
