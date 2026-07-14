package com.spoludo.valens.workout.timer

fun interface WorkoutTicker {
    suspend fun awaitNextSecond()
}
