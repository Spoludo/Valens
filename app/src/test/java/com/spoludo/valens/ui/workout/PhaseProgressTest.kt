package com.spoludo.valens.ui.workout

import com.spoludo.valens.workout.engine.WorkoutPhase
import org.junit.Assert.assertEquals
import org.junit.Test

class PhaseProgressTest {
    @Test
    fun phaseProgressFor_countdownAtStart_isZero() {
        assertEquals(0f, phaseProgressFor(WorkoutPhase.COUNTDOWN, secondsRemaining = 3, totalPhaseSeconds = 3), 0.001f)
    }

    @Test
    fun phaseProgressFor_countdownNearEnd_isCloseToOne() {
        assertEquals(2f / 3f, phaseProgressFor(WorkoutPhase.COUNTDOWN, secondsRemaining = 1, totalPhaseSeconds = 3), 0.001f)
    }

    @Test
    fun phaseProgressFor_workHalfway_isHalf() {
        assertEquals(0.5f, phaseProgressFor(WorkoutPhase.WORK, secondsRemaining = 5, totalPhaseSeconds = 10), 0.001f)
    }

    @Test
    fun phaseProgressFor_restNearEnd_isCloseToOne() {
        assertEquals(0.8f, phaseProgressFor(WorkoutPhase.REST, secondsRemaining = 1, totalPhaseSeconds = 5), 0.001f)
    }

    @Test
    fun phaseProgressFor_complete_isAlwaysOne() {
        assertEquals(1f, phaseProgressFor(WorkoutPhase.COMPLETE, secondsRemaining = 0, totalPhaseSeconds = 0), 0.001f)
        assertEquals(1f, phaseProgressFor(WorkoutPhase.COMPLETE, secondsRemaining = 5, totalPhaseSeconds = 10), 0.001f)
    }

    @Test
    fun phaseProgressFor_zeroTotalPhaseSeconds_safelyReturnsZeroInsteadOfCrashing() {
        assertEquals(0f, phaseProgressFor(WorkoutPhase.WORK, secondsRemaining = 0, totalPhaseSeconds = 0), 0.001f)
    }

    @Test
    fun phaseProgressFor_negativeTotalPhaseSeconds_safelyReturnsZero() {
        assertEquals(0f, phaseProgressFor(WorkoutPhase.WORK, secondsRemaining = 0, totalPhaseSeconds = -1), 0.001f)
    }
}
