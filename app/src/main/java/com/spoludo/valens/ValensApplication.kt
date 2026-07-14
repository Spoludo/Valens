package com.spoludo.valens

import android.app.Application
import com.spoludo.valens.data.json.AndroidAssetExercisePackJsonSource
import com.spoludo.valens.data.json.ExercisePackJsonParser
import com.spoludo.valens.data.repository.DefaultExerciseRepository
import com.spoludo.valens.data.repository.ExerciseRepository
import com.spoludo.valens.domain.model.ExercisePackValidator

class ValensApplication : Application() {
    val exerciseRepository: ExerciseRepository by lazy {
        DefaultExerciseRepository(
            sourceProvider = { AndroidAssetExercisePackJsonSource(assets) },
            loader = ExercisePackJsonParser(),
            validator = ExercisePackValidator,
        )
    }
}
