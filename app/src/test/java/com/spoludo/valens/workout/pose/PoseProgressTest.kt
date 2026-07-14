package com.spoludo.valens.workout.pose

import com.spoludo.valens.workout.engine.WorkoutPhase
import org.junit.Assert.assertEquals
import org.junit.Test

class PoseProgressTest {
    @Test
    fun poseProgressFor_countdownAtFullDuration_isZero() {
        assertEquals(0f, poseProgressFor(WorkoutPhase.COUNTDOWN, secondsRemaining = 3), 0.001f)
    }

    @Test
    fun poseProgressFor_countdownPartway_isFractional() {
        assertEquals(1f / 3f, poseProgressFor(WorkoutPhase.COUNTDOWN, secondsRemaining = 2), 0.001f)
        assertEquals(2f / 3f, poseProgressFor(WorkoutPhase.COUNTDOWN, secondsRemaining = 1), 0.001f)
    }

    @Test
    fun poseProgressFor_work_isAlwaysOne() {
        assertEquals(1f, poseProgressFor(WorkoutPhase.WORK, secondsRemaining = 30), 0.001f)
        assertEquals(1f, poseProgressFor(WorkoutPhase.WORK, secondsRemaining = 1), 0.001f)
    }

    @Test
    fun poseProgressFor_complete_isOne() {
        assertEquals(1f, poseProgressFor(WorkoutPhase.COMPLETE, secondsRemaining = 0), 0.001f)
    }

    @Test
    fun poseProgressFor_rest_isZero() {
        assertEquals(0f, poseProgressFor(WorkoutPhase.REST, secondsRemaining = 5), 0.001f)
    }
}
