package com.spoludo.valens.ui.workout

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.spoludo.valens.workout.engine.WorkoutPhase
import com.spoludo.valens.workout.pose.BodyPoseView
import com.spoludo.valens.workout.pose.PoseViewAngle
import com.spoludo.valens.workout.pose.RoutineExercisePoses
import com.spoludo.valens.workout.pose.poseProgressFor

@Composable
fun WorkoutScreen(
    viewModel: WorkoutViewModel,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val state = uiState

    KeepScreenOn(enabled = state is WorkoutUiState.Running && state.phase != WorkoutPhase.COMPLETE)

    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            when (state) {
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
    val views = RoutineExercisePoses.targetPoseViewsFor(state.exerciseId)

    BoxWithConstraints(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val isLandscapeLike = maxWidth > maxHeight
        if (isLandscapeLike) {
            WorkoutContentLandscape(
                state = state,
                views = views,
                animatedProgress = animatedProgress,
                onStartOrResume = onStartOrResume,
                onPause = onPause,
                onNext = onNext,
            )
        } else {
            WorkoutContentPortrait(
                state = state,
                views = views,
                animatedProgress = animatedProgress,
                onStartOrResume = onStartOrResume,
                onPause = onPause,
                onNext = onNext,
            )
        }
    }
}

@Composable
private fun WorkoutContentPortrait(
    state: WorkoutUiState.Running,
    views: List<BodyPoseView>,
    animatedProgress: Float,
    onStartOrResume: () -> Unit,
    onPause: () -> Unit,
    onNext: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        PoseViewsRow(
            views = views,
            progressToTarget = animatedProgress,
            illustrationHeight = 200.dp,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(text = state.exerciseName, style = MaterialTheme.typography.headlineMedium)
        Text(text = phaseLabel(state.phase), style = MaterialTheme.typography.titleLarge)
        Text(text = "${state.secondsRemaining}s", style = MaterialTheme.typography.displayLarge)
        LinearProgressIndicator(
            progress = { phaseProgressFor(state.phase, state.secondsRemaining, state.totalPhaseSeconds) },
            modifier = Modifier.fillMaxWidth(),
        )
        Text(text = "Exercise ${state.currentExerciseNumber} / ${state.totalExercises}", style = MaterialTheme.typography.bodyLarge)
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

@Composable
private fun WorkoutContentLandscape(
    state: WorkoutUiState.Running,
    views: List<BodyPoseView>,
    animatedProgress: Float,
    onStartOrResume: () -> Unit,
    onPause: () -> Unit,
    onNext: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        PoseViewsRow(
            views = views,
            progressToTarget = animatedProgress,
            illustrationHeight = 110.dp,
            modifier = Modifier.weight(0.4f),
        )
        Column(
            modifier = Modifier.weight(0.6f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = state.exerciseName, style = MaterialTheme.typography.headlineSmall)
            Text(text = phaseLabel(state.phase), style = MaterialTheme.typography.titleMedium)
            Text(text = "${state.secondsRemaining}s", style = MaterialTheme.typography.displayLarge)
            LinearProgressIndicator(
                progress = { phaseProgressFor(state.phase, state.secondsRemaining, state.totalPhaseSeconds) },
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = "Exercise ${state.currentExerciseNumber} / ${state.totalExercises} · Set ${state.currentSet} / ${state.totalSets}",
                style = MaterialTheme.typography.bodyMedium,
            )
            state.nextExerciseName?.let {
                Text(text = "Next: $it", style = MaterialTheme.typography.bodySmall)
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
}

@Composable
private fun PoseViewsRow(
    views: List<BodyPoseView>,
    progressToTarget: Float,
    illustrationHeight: Dp,
    modifier: Modifier = Modifier,
) {
    if (views.isEmpty()) {
        BodyPoseIllustration(
            pose = null,
            angle = PoseViewAngle.SIDE,
            progressToTarget = progressToTarget,
            modifier = modifier.height(illustrationHeight),
        )
        return
    }
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        views.forEach { view ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                BodyPoseIllustration(
                    pose = view.pose,
                    angle = view.angle,
                    progressToTarget = progressToTarget,
                    prop = view.prop,
                    propNearEdge = view.propNearEdge,
                    accessibilityDescription = "${view.label} posture illustration",
                    modifier = Modifier.fillMaxWidth().height(illustrationHeight),
                )
                Text(text = view.label, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

private fun phaseLabel(phase: WorkoutPhase): String = when (phase) {
    WorkoutPhase.COUNTDOWN -> "Get Ready"
    WorkoutPhase.WORK -> "Work"
    WorkoutPhase.REST -> "Rest"
    WorkoutPhase.COMPLETE -> "Done"
}
