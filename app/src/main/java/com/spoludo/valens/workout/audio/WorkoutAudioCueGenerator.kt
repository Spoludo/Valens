package com.spoludo.valens.workout.audio

import com.spoludo.valens.domain.model.Exercise
import com.spoludo.valens.workout.engine.WorkoutEngineState
import com.spoludo.valens.workout.engine.WorkoutPhase
import com.spoludo.valens.workout.engine.totalWorkSeconds

private val ENCOURAGEMENTS = listOf(
    "Good. Keep breathing.",
    "Stay relaxed.",
    "Move with control.",
    "Almost there.",
    "Nice work.",
)

class WorkoutAudioCueGenerator(
    private val routine: List<Exercise>,
    private val displayName: (Exercise) -> String,
) {
    fun cueFor(previous: WorkoutEngineState?, current: WorkoutEngineState): String? {
        val exercise = routine.getOrNull(current.exerciseIndex) ?: return null
        val previousHasStarted = previous?.hasStarted ?: false

        return when {
            !previousHasStarted && current.hasStarted ->
                "Let's begin. Prepare for ${displayName(exercise)}."

            previous?.phase == WorkoutPhase.COUNTDOWN && current.phase == WorkoutPhase.WORK ->
                "Start."

            current.phase == WorkoutPhase.WORK &&
                current.secondsRemaining == 10 &&
                previous?.secondsRemaining != 10 &&
                totalWorkSeconds(exercise) > 10 ->
                "Ten seconds left."

            previous?.phase == WorkoutPhase.WORK && current.phase == WorkoutPhase.REST &&
                current.setIndex >= exercise.defaultPrescription.sets - 1 ->
                ENCOURAGEMENTS[current.exerciseIndex % ENCOURAGEMENTS.size]

            previous?.phase == WorkoutPhase.REST && current.phase == WorkoutPhase.COUNTDOWN &&
                current.exerciseIndex != previous?.exerciseIndex ->
                "Next: ${displayName(exercise)}. Prepare."

            previous?.phase == WorkoutPhase.WORK && current.phase == WorkoutPhase.COMPLETE ->
                "Workout complete."

            else -> null
        }
    }
}
