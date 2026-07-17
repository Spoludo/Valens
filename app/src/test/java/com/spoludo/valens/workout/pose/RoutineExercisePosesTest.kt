package com.spoludo.valens.workout.pose

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RoutineExercisePosesTest {
    private val knownExerciseIds = listOf(
        "wall_push", "hollow_body_hold", "calf_raise_hold", "wall_sit",
        "single_leg_balance_hold", "reverse_table_hold", "plank_hold",
    )

    @Test
    fun targetPoseViewsFor_knownExerciseIds_returnNonEmptyViewsWithAllTwentyPoints() {
        for (id in knownExerciseIds) {
            val views = RoutineExercisePoses.targetPoseViewsFor(id)
            assertTrue("expected at least one view for $id", views.isNotEmpty())
            for (view in views) {
                assertEquals("point set mismatch for $id/${view.angle}", BodyPoint.entries.toSet(), view.pose.points.keys)
            }
        }
    }

    @Test
    fun targetPoseViewsFor_wallPush_returnsSideAndFrontViews() {
        val views = RoutineExercisePoses.targetPoseViewsFor("wall_push")
        assertEquals(listOf(PoseViewAngle.SIDE, PoseViewAngle.FRONT), views.map { it.angle })
        assertEquals(listOf("Side", "Front"), views.map { it.label })
    }

    @Test
    fun targetPoseViewsFor_otherKnownExerciseIds_returnExactlyOneView() {
        val singleViewIds = knownExerciseIds - "wall_push"
        for (id in singleViewIds) {
            val views = RoutineExercisePoses.targetPoseViewsFor(id)
            assertEquals("expected exactly one view for $id", 1, views.size)
        }
    }

    @Test
    fun targetPoseViewsFor_unknownExerciseId_returnsEmptyList() {
        assertEquals(emptyList<BodyPoseView>(), RoutineExercisePoses.targetPoseViewsFor("not_a_real_exercise"))
    }

    @Test
    fun neutralStandingPose_hasAllTwentyPoints() {
        assertEquals(BodyPoint.entries.toSet(), RoutineExercisePoses.neutralStandingPose.points.keys)
    }
}
