package com.spoludo.valens.workout.pose

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class RoutineExercisePosesTest {
    private val knownExerciseIds = listOf(
        "wall_push", "hollow_body_hold", "calf_raise_hold", "wall_sit",
        "single_leg_balance_hold", "reverse_table_hold", "plank_hold",
    )

    @Test
    fun targetPoseFor_knownExerciseIds_returnsPoseWithAllFourteenPoints() {
        for (id in knownExerciseIds) {
            val pose = RoutineExercisePoses.targetPoseFor(id)
            assertNotNull("expected a pose for $id", pose)
            assertEquals(BodyPoint.entries.toSet(), pose!!.points.keys)
        }
    }

    @Test
    fun targetPoseFor_unknownExerciseId_returnsNull() {
        assertNull(RoutineExercisePoses.targetPoseFor("not_a_real_exercise"))
    }

    @Test
    fun neutralStandingPose_hasAllFourteenPoints() {
        assertEquals(BodyPoint.entries.toSet(), RoutineExercisePoses.neutralStandingPose.points.keys)
    }
}
