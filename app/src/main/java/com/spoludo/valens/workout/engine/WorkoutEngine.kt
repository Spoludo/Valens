package com.spoludo.valens.workout.engine

import com.spoludo.valens.domain.model.Exercise
import com.spoludo.valens.workout.timer.WorkoutTicker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class WorkoutPhase { COUNTDOWN, WORK, REST, COMPLETE }

const val PREP_COUNTDOWN_SECONDS = 3

fun totalWorkSeconds(exercise: Exercise): Int =
    exercise.defaultPrescription.holdSeconds ?: exercise.defaultPrescription.durationSeconds ?: 0

data class WorkoutEngineState(
    val exerciseIndex: Int = 0,
    val setIndex: Int = 0,
    val phase: WorkoutPhase = WorkoutPhase.COUNTDOWN,
    val secondsRemaining: Int = PREP_COUNTDOWN_SECONDS,
    val isRunning: Boolean = false,
    val hasStarted: Boolean = false,
)

class WorkoutEngine(
    val routine: List<Exercise>,
    private val ticker: WorkoutTicker,
) {
    private val _state = MutableStateFlow(WorkoutEngineState())
    val state: StateFlow<WorkoutEngineState> = _state.asStateFlow()

    suspend fun run() {
        if (routine.isEmpty()) {
            _state.value = _state.value.copy(phase = WorkoutPhase.COMPLETE, isRunning = false)
            return
        }
        while (_state.value.phase != WorkoutPhase.COMPLETE) {
            ticker.awaitNextSecond()
            if (_state.value.isRunning) tick()
        }
    }

    fun pause() {
        _state.value = _state.value.copy(isRunning = false)
    }

    fun resume() {
        if (_state.value.phase != WorkoutPhase.COMPLETE) {
            _state.value = _state.value.copy(isRunning = true, hasStarted = true)
        }
    }

    fun skip() {
        _state.value = advancePastCurrentPhase(_state.value)
    }

    private fun tick() {
        val current = _state.value
        val remaining = current.secondsRemaining - 1
        _state.value = if (remaining > 0) current.copy(secondsRemaining = remaining)
        else advancePastCurrentPhase(current)
    }

    private fun advancePastCurrentPhase(current: WorkoutEngineState): WorkoutEngineState {
        if (current.phase == WorkoutPhase.COMPLETE) return current
        val exercise = routine.getOrNull(current.exerciseIndex)
            ?: return current.copy(phase = WorkoutPhase.COMPLETE, isRunning = false, secondsRemaining = 0)
        val prescription = exercise.defaultPrescription

        return when (current.phase) {
            WorkoutPhase.COUNTDOWN -> current.copy(
                phase = WorkoutPhase.WORK,
                secondsRemaining = totalWorkSeconds(exercise),
            )
            WorkoutPhase.WORK -> {
                val isLastSet = current.setIndex >= prescription.sets - 1
                val isLastExercise = current.exerciseIndex >= routine.lastIndex
                if (isLastSet && isLastExercise) {
                    current.copy(phase = WorkoutPhase.COMPLETE, isRunning = false, secondsRemaining = 0)
                } else {
                    current.copy(phase = WorkoutPhase.REST, secondsRemaining = prescription.restSeconds)
                }
            }
            WorkoutPhase.REST -> {
                val isLastSet = current.setIndex >= prescription.sets - 1
                if (isLastSet) {
                    current.copy(
                        exerciseIndex = current.exerciseIndex + 1, setIndex = 0,
                        phase = WorkoutPhase.COUNTDOWN, secondsRemaining = PREP_COUNTDOWN_SECONDS,
                    )
                } else {
                    current.copy(
                        setIndex = current.setIndex + 1,
                        phase = WorkoutPhase.COUNTDOWN, secondsRemaining = PREP_COUNTDOWN_SECONDS,
                    )
                }
            }
            WorkoutPhase.COMPLETE -> current
        }
    }
}
