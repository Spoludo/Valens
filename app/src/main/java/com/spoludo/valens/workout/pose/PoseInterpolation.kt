package com.spoludo.valens.workout.pose

import androidx.compose.ui.geometry.lerp

fun interpolatePose(from: BodyPose, to: BodyPose, progress: Float): BodyPose {
    val clamped = progress.coerceIn(0f, 1f)
    return BodyPose(
        points = BodyPoint.entries.associateWith { point ->
            lerp(from.points.getValue(point), to.points.getValue(point), clamped)
        },
        prop = to.prop,
    )
}
