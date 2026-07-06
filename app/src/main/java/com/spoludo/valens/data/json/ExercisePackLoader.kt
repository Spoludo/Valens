package com.spoludo.valens.data.json

import com.spoludo.valens.domain.model.ExercisePack

interface ExercisePackLoader {
    fun load(source: ExercisePackJsonSource): ExercisePack
}
