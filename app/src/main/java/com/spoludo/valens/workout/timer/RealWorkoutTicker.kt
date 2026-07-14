package com.spoludo.valens.workout.timer

import kotlinx.coroutines.delay

class RealWorkoutTicker : WorkoutTicker {
    override suspend fun awaitNextSecond() = delay(1000)
}
