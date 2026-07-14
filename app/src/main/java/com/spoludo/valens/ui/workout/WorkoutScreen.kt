package com.spoludo.valens.ui.workout

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.spoludo.valens.workout.engine.WorkoutPhase
import com.spoludo.valens.workout.pose.RoutineExercisePoses
import com.spoludo.valens.workout.pose.poseProgressFor

@Composable
fun WorkoutScreen(
    viewModel: WorkoutViewModel,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            when (val state = uiState) {
                is WorkoutUiState.Loading -> CircularProgressIndicator()
                is WorkoutUiState.Error -> Text(text = state.message)
                is WorkoutUiState.Running -> WorkoutContent(
                    state = state,
                    onStartOrResume = viewModel::onStartOrResume,
                    onPause = viewModel::onPause,
                    onNext = viewModel::onNext,
                    onFinish = onFinish,
                )
            }
        }
    }
}

@Composable
private fun WorkoutContent(
    state: WorkoutUiState.Running,
    onStartOrResume: () -> Unit,
    onPause: () -> Unit,
    onNext: () -> Unit,
    onFinish: () -> Unit,
) {
    if (state.isComplete) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(text = "Workout complete!", style = MaterialTheme.typography.headlineMedium)
            Button(onClick = onFinish) { Text("Done") }
        }
        return
    }

    val animatedProgress by animateFloatAsState(
        targetValue = poseProgressFor(state.phase, state.secondsRemaining),
        animationSpec = tween(durationMillis = 900),
        label = "bodyPoseProgress",
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        BodyPoseIllustration(
            targetPose = RoutineExercisePoses.targetPoseFor(state.exerciseId),
            progressToTarget = animatedProgress,
            modifier = Modifier.fillMaxWidth().height(200.dp),
        )
        Text(text = state.exerciseName, style = MaterialTheme.typography.headlineMedium)
        Text(text = phaseLabel(state.phase), style = MaterialTheme.typography.titleLarge)
        Text(text = "${state.secondsRemaining}s", style = MaterialTheme.typography.displayLarge)
        Text(text = "Set ${state.currentSet} / ${state.totalSets}", style = MaterialTheme.typography.bodyLarge)
        state.nextExerciseName?.let {
            Text(text = "Next: $it", style = MaterialTheme.typography.bodyMedium)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (state.isRunning) {
                Button(onClick = onPause) { Text("Pause") }
            } else {
                Button(onClick = onStartOrResume) { Text(if (state.isStarted) "Resume" else "Start") }
            }
            Button(onClick = onNext) { Text("Next") }
        }
    }
}

private fun phaseLabel(phase: WorkoutPhase): String = when (phase) {
    WorkoutPhase.COUNTDOWN -> "Get Ready"
    WorkoutPhase.WORK -> "Work"
    WorkoutPhase.REST -> "Rest"
    WorkoutPhase.COMPLETE -> "Done"
}
