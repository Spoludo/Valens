package com.spoludo.valens.workout.audio

object NoOpWorkoutAudioCuePlayer : WorkoutAudioCuePlayer {
    override fun speak(text: String) = Unit
    override fun shutdown() = Unit
}
