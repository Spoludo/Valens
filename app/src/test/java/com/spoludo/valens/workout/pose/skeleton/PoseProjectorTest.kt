package com.spoludo.valens.workout.pose.skeleton

import com.spoludo.valens.workout.pose.PoseViewAngle
import org.junit.Assert.assertEquals
import org.junit.Test

class PoseProjectorTest {
    private val joints = mapOf(SkeletonJoint.Head to Vec3(x = 2f, y = 3f, z = 4f))

    @Test
    fun project_side_dropsXAndUsesZAsScreenX() {
        val projected = project(joints, PoseViewAngle.SIDE).getValue(SkeletonJoint.Head)
        assertEquals(4f, projected.x, 0.001f)
        assertEquals(-3f, projected.y, 0.001f)
    }

    @Test
    fun project_front_dropsZAndUsesXAsScreenX() {
        val projected = project(joints, PoseViewAngle.FRONT).getValue(SkeletonJoint.Head)
        assertEquals(2f, projected.x, 0.001f)
        assertEquals(-3f, projected.y, 0.001f)
    }

    @Test
    fun project_back_dropsZAndMirrorsX() {
        val projected = project(joints, PoseViewAngle.BACK).getValue(SkeletonJoint.Head)
        assertEquals(-2f, projected.x, 0.001f)
        assertEquals(-3f, projected.y, 0.001f)
    }
}
