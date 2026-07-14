package com.spoludo.valens.workout.pose

import com.spoludo.valens.workout.engine.PREP_COUNTDOWN_SECONDS
import com.spoludo.valens.workout.engine.WorkoutPhase

fun poseProgressFor(phase: WorkoutPhase, secondsRemaining: Int): Float = when (phase) {
    WorkoutPhase.COUNTDOWN -> (1f - secondsRemaining / PREP_COUNTDOWN_SECONDS.toFloat()).coerceIn(0f, 1f)
    WorkoutPhase.WORK, WorkoutPhase.COMPLETE -> 1f
    WorkoutPhase.REST -> 0f
}
