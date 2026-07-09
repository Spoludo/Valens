package com.spoludo.valens.data.repository

import com.spoludo.valens.domain.model.ExercisePack
import com.spoludo.valens.domain.model.ExercisePackValidationResult

sealed interface ExerciseRepositoryResult {
    data class Success(val exercisePack: ExercisePack) : ExerciseRepositoryResult
    data class InvalidPack(val validationResult: ExercisePackValidationResult) : ExerciseRepositoryResult
    data class LoadFailure(val cause: Throwable) : ExerciseRepositoryResult
}

interface ExerciseRepository {
    suspend fun loadExercisePack(): ExerciseRepositoryResult
    suspend fun reloadExercisePack(): ExerciseRepositoryResult
    suspend fun clearCache()
}
