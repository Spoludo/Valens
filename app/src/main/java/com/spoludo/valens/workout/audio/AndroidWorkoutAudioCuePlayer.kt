package com.spoludo.valens.workout.audio

import android.content.Context
import android.speech.tts.TextToSpeech

class AndroidWorkoutAudioCuePlayer(context: Context) : WorkoutAudioCuePlayer {
    @Volatile private var isReady = false
    @Volatile private var isShutdown = false

    private val tts = TextToSpeech(context.applicationContext) { status ->
        isReady = status == TextToSpeech.SUCCESS && !isShutdown
    }

    override fun speak(text: String) {
        if (!isReady || isShutdown) return
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, text.hashCode().toString())
    }

    override fun shutdown() {
        isShutdown = true
        tts.stop()
        tts.shutdown()
    }
}
