package com.spoludo.valens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.spoludo.valens.ui.home.HomeScreen
import com.spoludo.valens.ui.theme.ValensTheme
import com.spoludo.valens.ui.workout.WorkoutScreen
import com.spoludo.valens.ui.workout.WorkoutViewModel
import com.spoludo.valens.workout.audio.AndroidWorkoutAudioCuePlayer
import com.spoludo.valens.workout.audio.WorkoutAudioCuePlayer

class MainActivity : ComponentActivity() {
    private var _audioCuePlayer: WorkoutAudioCuePlayer? = null

    private val audioCuePlayer: WorkoutAudioCuePlayer
        get() {
            return _audioCuePlayer ?: AndroidWorkoutAudioCuePlayer(this).also { _audioCuePlayer = it }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ValensTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        HomeScreen(onStart = { navController.navigate("workout") })
                    }
                    composable("workout") {
                        val application = LocalContext.current.applicationContext as ValensApplication
                        val viewModel: WorkoutViewModel = viewModel(
                            factory = WorkoutViewModel.Factory(application.exerciseRepository, audioCuePlayer),
                        )
                        LaunchedEffect(Unit) { viewModel.updateAudioCuePlayer(audioCuePlayer) }
                        WorkoutScreen(
                            viewModel = viewModel,
                            onFinish = { navController.popBackStack() },
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        _audioCuePlayer?.shutdown()
        super.onDestroy()
    }
}
