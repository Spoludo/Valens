package com.spoludo.valens.data.json

import com.spoludo.valens.domain.model.Exercise
import com.spoludo.valens.domain.model.ExercisePack
import com.spoludo.valens.domain.model.ExercisePackManifest
import com.spoludo.valens.domain.model.JointCollection
import com.spoludo.valens.domain.model.MovementPatternCollection
import com.spoludo.valens.domain.model.MuscleCollection
import com.spoludo.valens.domain.model.TranslationBundle
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class ExercisePackJsonParser : ExercisePackLoader {
    private val json = Json { ignoreUnknownKeys = true }

    override fun load(source: ExercisePackJsonSource): ExercisePack {
        val manifest = json.decodeFromString<ExercisePackManifest>(source.manifest())
        val movementPatterns = json.decodeFromString<MovementPatternCollection>(source.movementPatterns()).patterns
        val muscles = json.decodeFromString<MuscleCollection>(source.muscles()).muscles
        val joints = json.decodeFromString<JointCollection>(source.joints()).joints
        val exercises = source.exercises()
            .sortedBy { it.relativePath }
            .map { json.decodeFromString<Exercise>(it.content) }
        val translations = source.translations()
            .sortedBy { it.relativePath }
            .map { json.decodeFromString<TranslationBundle>(it.content) }
            .associateBy { it.locale }

        return ExercisePack(
            manifest = manifest,
            movementPatterns = movementPatterns,
            muscles = muscles,
            joints = joints,
            exercises = exercises,
            translations = translations,
        )
    }
}
