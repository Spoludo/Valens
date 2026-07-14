package com.spoludo.valens.workout.pose

import androidx.compose.ui.geometry.Offset
import org.junit.Assert.assertEquals
import org.junit.Test

class PoseInterpolationTest {
    private val from = BodyPose(points = BodyPoint.entries.associateWith { Offset(0f, 0f) })
    private val to = BodyPose(
        points = BodyPoint.entries.associateWith { Offset(1f, 1f) },
        prop = PoseProp.WALL,
    )

    @Test
    fun interpolatePose_atProgressZero_returnsFromPoints() {
        val result = interpolatePose(from, to, 0f)
        assertEquals(Offset(0f, 0f), result.points.getValue(BodyPoint.Head))
    }

    @Test
    fun interpolatePose_atProgressOne_returnsToPoints() {
        val result = interpolatePose(from, to, 1f)
        assertEquals(Offset(1f, 1f), result.points.getValue(BodyPoint.Head))
    }

    @Test
    fun interpolatePose_atProgressHalf_returnsMidpoint() {
        val result = interpolatePose(from, to, 0.5f)
        assertEquals(Offset(0.5f, 0.5f), result.points.getValue(BodyPoint.Head))
    }

    @Test
    fun interpolatePose_usesTargetPoseProp_regardlessOfProgress() {
        assertEquals(PoseProp.WALL, interpolatePose(from, to, 0f).prop)
        assertEquals(PoseProp.WALL, interpolatePose(from, to, 1f).prop)
    }

    @Test
    fun interpolatePose_clampsProgressAboveOne() {
        val result = interpolatePose(from, to, 1.5f)
        assertEquals(Offset(1f, 1f), result.points.getValue(BodyPoint.Head))
    }

    @Test
    fun interpolatePose_clampsProgressBelowZero() {
        val result = interpolatePose(from, to, -0.5f)
        assertEquals(Offset(0f, 0f), result.points.getValue(BodyPoint.Head))
    }
}
