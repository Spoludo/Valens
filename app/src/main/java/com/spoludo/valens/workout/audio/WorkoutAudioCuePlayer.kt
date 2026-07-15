package com.spoludo.valens.workout.audio

interface WorkoutAudioCuePlayer {
    fun speak(text: String)
    fun shutdown()
}
