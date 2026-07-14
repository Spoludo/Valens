package com.spoludo.valens.ui.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.spoludo.valens.data.repository.ExerciseRepository
import com.spoludo.valens.data.repository.ExerciseRepositoryResult
import com.spoludo.valens.domain.model.Exercise
import com.spoludo.valens.domain.model.ExercisePack
import com.spoludo.valens.workout.engine.FixedBeginnerRoutine
import com.spoludo.valens.workout.engine.WorkoutEngine
import com.spoludo.valens.workout.engine.WorkoutEngineState
import com.spoludo.valens.workout.engine.WorkoutPhase
import com.spoludo.valens.workout.timer.RealWorkoutTicker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface WorkoutUiState {
    data object Loading : WorkoutUiState
    data class Error(val message: String) : WorkoutUiState
    data class Running(
        val exerciseName: String,
        val phase: WorkoutPhase,
        val secondsRemaining: Int,
        val currentSet: Int,
        val totalSets: Int,
        val nextExerciseName: String?,
        val isRunning: Boolean,
        val isStarted: Boolean,
        val isComplete: Boolean,
    ) : WorkoutUiState
}

class WorkoutViewModel(
    private val exerciseRepository: ExerciseRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<WorkoutUiState>(WorkoutUiState.Loading)
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    private var engine: WorkoutEngine? = null
    private var routine: List<Exercise> = emptyList()
    private var displayNames: Map<String, String> = emptyMap()

    init {
        viewModelScope.launch {
            when (val result = exerciseRepository.loadExercisePack()) {
                is ExerciseRepositoryResult.Success -> startEngine(result.exercisePack)
                is ExerciseRepositoryResult.InvalidPack ->
                    _uiState.value = WorkoutUiState.Error(
                        "Exercise pack is invalid: ${result.validationResult.errors.size} error(s)",
                    )
                is ExerciseRepositoryResult.LoadFailure ->
                    _uiState.value = WorkoutUiState.Error("Could not load exercise pack: ${result.cause.message}")
            }
        }
    }

    private fun startEngine(pack: ExercisePack) {
        routine = FixedBeginnerRoutine.build(pack)
        displayNames = pack.translations["en"]?.strings.orEmpty()
        if (routine.isEmpty()) {
            _uiState.value = WorkoutUiState.Error("No exercises available for the beginner routine")
            return
        }
        val newEngine = WorkoutEngine(routine, RealWorkoutTicker())
        engine = newEngine
        viewModelScope.launch { newEngine.run() }
        viewModelScope.launch {
            newEngine.state.collect { state -> _uiState.value = toUiState(state) }
        }
    }

    private fun toUiState(state: WorkoutEngineState): WorkoutUiState.Running {
        val exercise = routine.getOrNull(state.exerciseIndex)
        val next = routine.getOrNull(state.exerciseIndex + 1)
        return WorkoutUiState.Running(
            exerciseName = exercise?.let { displayName(it) } ?: "",
            phase = state.phase,
            secondsRemaining = state.secondsRemaining,
            currentSet = state.setIndex + 1,
            totalSets = exercise?.defaultPrescription?.sets ?: 0,
            nextExerciseName = next?.let { displayName(it) },
            isRunning = state.isRunning,
            isStarted = state.hasStarted,
            isComplete = state.phase == WorkoutPhase.COMPLETE,
        )
    }

    private fun displayName(exercise: Exercise): String =
        displayNames[exercise.nameKey.value] ?: exercise.nameKey.value

    fun onStartOrResume() {
        engine?.resume()
    }

    fun onPause() {
        engine?.pause()
    }

    fun onNext() {
        engine?.skip()
    }

    class Factory(private val exerciseRepository: ExerciseRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            WorkoutViewModel(exerciseRepository) as T
    }
}
