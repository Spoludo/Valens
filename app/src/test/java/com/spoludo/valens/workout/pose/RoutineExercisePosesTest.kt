package com.spoludo.valens.workout.pose

import com.spoludo.valens.workout.pose.skeleton.SkeletonPose
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RoutineExercisePosesTest {
    private val knownExerciseIds = listOf(
        "wall_push", "hollow_body_hold", "calf_raise_hold", "wall_sit",
        "single_leg_balance_hold", "reverse_table_hold", "plank_hold",
    )

    @Test
    fun targetPoseViewsFor_knownExerciseIds_returnNonEmptyLists() {
        for (id in knownExerciseIds) {
            assertTrue("expected at least one view for $id", RoutineExercisePoses.targetPoseViewsFor(id).isNotEmpty())
        }
    }

    @Test
    fun targetPoseViewsFor_wallPush_returnsSideAndFrontViews() {
        val views = RoutineExercisePoses.targetPoseViewsFor("wall_push")
        assertEquals(listOf(PoseViewAngle.SIDE, PoseViewAngle.FRONT), views.map { it.angle })
        assertEquals(listOf("Side", "Front"), views.map { it.label })
    }

    @Test
    fun targetPoseViewsFor_wallPush_sideHasWallProp_frontHasNoProp() {
        val views = RoutineExercisePoses.targetPoseViewsFor("wall_push")
        assertEquals(PoseProp.WALL, views.first { it.angle == PoseViewAngle.SIDE }.prop)
        assertEquals(PoseProp.NONE, views.first { it.angle == PoseViewAngle.FRONT }.prop)
    }

    @Test
    fun targetPoseViewsFor_otherKnownExerciseIds_returnExactlyOneView() {
        val singleViewIds = knownExerciseIds - "wall_push"
        for (id in singleViewIds) {
            assertEquals("expected exactly one view for $id", 1, RoutineExercisePoses.targetPoseViewsFor(id).size)
        }
    }

    @Test
    fun targetPoseViewsFor_unknownExerciseId_returnsEmptyList() {
        assertEquals(emptyList<BodyPoseView>(), RoutineExercisePoses.targetPoseViewsFor("not_a_real_exercise"))
    }

    @Test
    fun targetPoseViewsFor_calfRaiseHold_hasNonZeroFootPitch() {
        val pose = RoutineExercisePoses.targetPoseViewsFor("calf_raise_hold").first().pose
        assertNotEquals(0f, pose.leftFootPitchDegrees)
        assertNotEquals(0f, pose.rightFootPitchDegrees)
    }

    @Test
    fun neutralStandingPose_isAllZeroAngles() {
        assertEquals(SkeletonPose(), RoutineExercisePoses.neutralStandingPose)
    }
}
