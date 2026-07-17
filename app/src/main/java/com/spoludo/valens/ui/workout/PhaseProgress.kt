package com.spoludo.valens.ui.workout

import com.spoludo.valens.workout.engine.WorkoutPhase

fun phaseProgressFor(phase: WorkoutPhase, secondsRemaining: Int, totalPhaseSeconds: Int): Float {
    if (phase == WorkoutPhase.COMPLETE) return 1f
    if (totalPhaseSeconds <= 0) return 0f
    return (1f - secondsRemaining / totalPhaseSeconds.toFloat()).coerceIn(0f, 1f)
}
