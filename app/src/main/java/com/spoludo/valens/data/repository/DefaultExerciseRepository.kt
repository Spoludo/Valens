package com.spoludo.valens.data.repository

import com.spoludo.valens.data.json.ExercisePackJsonSource
import com.spoludo.valens.data.json.ExercisePackLoader
import com.spoludo.valens.domain.model.ExercisePack
import com.spoludo.valens.domain.model.ExercisePackValidator
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class DefaultExerciseRepository(
    private val sourceProvider: () -> ExercisePackJsonSource,
    private val loader: ExercisePackLoader,
    private val validator: ExercisePackValidator,
) : ExerciseRepository {
    private val mutex = Mutex()
    private var cached: ExercisePack? = null

    override suspend fun loadExercisePack(): ExerciseRepositoryResult {
        cached?.let { return ExerciseRepositoryResult.Success(it) }
        return mutex.withLock {
            cached?.let { return@withLock ExerciseRepositoryResult.Success(it) }
            loadAndValidate()
        }
    }

    override suspend fun reloadExercisePack(): ExerciseRepositoryResult =
        mutex.withLock {
            cached = null
            loadAndValidate()
        }

    override suspend fun clearCache() {
        mutex.withLock { cached = null }
    }

    private fun loadAndValidate(): ExerciseRepositoryResult {
        val pack = try {
            loader.load(sourceProvider())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return ExerciseRepositoryResult.LoadFailure(e)
        }

        val validationResult = validator.validate(pack)
        if (!validationResult.isValid) {
            return ExerciseRepositoryResult.InvalidPack(validationResult)
        }

        cached = pack
        return ExerciseRepositoryResult.Success(pack)
    }
}
