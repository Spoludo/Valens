package com.spoludo.valens.data.repository

import com.spoludo.valens.data.json.ExercisePackJsonParser
import com.spoludo.valens.data.json.ExercisePackJsonSource
import com.spoludo.valens.data.json.ExercisePackLoader
import com.spoludo.valens.data.json.JsonTextFile
import com.spoludo.valens.data.json.RealBundledExercisePackJsonSource
import com.spoludo.valens.data.json.findBundledIsometricFoundationsPack
import com.spoludo.valens.domain.model.Cues
import com.spoludo.valens.domain.model.EquipmentItem
import com.spoludo.valens.domain.model.Exercise
import com.spoludo.valens.domain.model.ExerciseAssets
import com.spoludo.valens.domain.model.ExerciseFamilyId
import com.spoludo.valens.domain.model.ExerciseId
import com.spoludo.valens.domain.model.ExercisePack
import com.spoludo.valens.domain.model.ExercisePackContents
import com.spoludo.valens.domain.model.ExercisePackManifest
import com.spoludo.valens.domain.model.ExercisePackValidator
import com.spoludo.valens.domain.model.ExerciseType
import com.spoludo.valens.domain.model.FatigueCost
import com.spoludo.valens.domain.model.LocalizationKey
import com.spoludo.valens.domain.model.Muscle
import com.spoludo.valens.domain.model.MuscleId
import com.spoludo.valens.domain.model.MuscleLoad
import com.spoludo.valens.domain.model.MuscleRegion
import com.spoludo.valens.domain.model.Muscles
import com.spoludo.valens.domain.model.MovementDomain
import com.spoludo.valens.domain.model.MovementPattern
import com.spoludo.valens.domain.model.MovementPatternId
import com.spoludo.valens.domain.model.Prescription
import com.spoludo.valens.domain.model.ProgressionStep
import com.spoludo.valens.domain.model.SideModel
import com.spoludo.valens.domain.model.WeeklyFrequency
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class DefaultExerciseRepositoryTest {

    @Test
    fun loadExercisePack_validPack_returnsSuccessAndCaches() = runTest {
        val fakeLoader = FakeExercisePackLoader(result = { validPack() })
        val repository = repositoryWith(fakeLoader)

        val first = repository.loadExercisePack()
        val second = repository.loadExercisePack()

        assertTrue(first is ExerciseRepositoryResult.Success)
        assertTrue(second is ExerciseRepositoryResult.Success)
        assertEquals(1, fakeLoader.invocationCount)
    }

    @Test
    fun loadExercisePack_invalidPack_returnsInvalidPackAndDoesNotCache() = runTest {
        val fakeLoader = FakeExercisePackLoader(result = { invalidPack() })
        val repository = repositoryWith(fakeLoader)

        val first = repository.loadExercisePack()
        repository.loadExercisePack()

        assertTrue(first is ExerciseRepositoryResult.InvalidPack)
        assertTrue((first as ExerciseRepositoryResult.InvalidPack).validationResult.errors.isNotEmpty())
        assertEquals(2, fakeLoader.invocationCount)
    }

    @Test
    fun loadExercisePack_loaderThrows_returnsLoadFailure() = runTest {
        val exception = SerializationException("boom")
        val fakeLoader = FakeExercisePackLoader(result = { throw exception })
        val repository = repositoryWith(fakeLoader)

        val first = repository.loadExercisePack()
        repository.loadExercisePack()

        assertTrue(first is ExerciseRepositoryResult.LoadFailure)
        assertEquals(exception, (first as ExerciseRepositoryResult.LoadFailure).cause)
        assertEquals(2, fakeLoader.invocationCount)
    }

    @Test
    fun loadExercisePack_sourceProviderThrows_returnsLoadFailure() = runTest {
        val exception = IllegalStateException("no source")
        val repository = DefaultExerciseRepository(
            sourceProvider = { throw exception },
            loader = FakeExercisePackLoader(result = { validPack() }),
            validator = ExercisePackValidator,
        )

        val result = repository.loadExercisePack()

        assertTrue(result is ExerciseRepositoryResult.LoadFailure)
        assertEquals(exception, (result as ExerciseRepositoryResult.LoadFailure).cause)
    }

    @Test
    fun loadExercisePack_cancellation_propagatesInsteadOfLoadFailure() = runTest {
        val fakeLoader = FakeExercisePackLoader(result = { throw CancellationException("cancelled") })
        val repository = repositoryWith(fakeLoader)

        try {
            repository.loadExercisePack()
            fail("expected CancellationException to propagate")
        } catch (e: CancellationException) {
            // expected
        }
    }

    @Test
    fun reloadExercisePack_forcesFreshSourceAndReplacesCache() = runTest {
        var current = validPack(id = "pack_a")
        val fakeLoader = FakeExercisePackLoader(result = { current })
        val repository = repositoryWith(fakeLoader)

        repository.loadExercisePack()
        current = validPack(id = "pack_b")
        val reloaded = repository.reloadExercisePack()
        val third = repository.loadExercisePack()

        assertTrue(reloaded is ExerciseRepositoryResult.Success)
        assertEquals("pack_b", (reloaded as ExerciseRepositoryResult.Success).exercisePack.exercises.single().id.value)
        assertEquals(2, fakeLoader.invocationCount)
        assertTrue(third is ExerciseRepositoryResult.Success)
        assertEquals("pack_b", (third as ExerciseRepositoryResult.Success).exercisePack.exercises.single().id.value)
    }

    @Test
    fun clearCache_makesNextLoadReinvokeLoader() = runTest {
        val fakeLoader = FakeExercisePackLoader(result = { validPack() })
        val repository = repositoryWith(fakeLoader)

        repository.loadExercisePack()
        repository.clearCache()
        repository.loadExercisePack()

        assertEquals(2, fakeLoader.invocationCount)
    }

    @Test
    fun loadExercisePack_realBundledPack_returnsSuccessWith11Exercises() = runTest {
        val repository = DefaultExerciseRepository(
            sourceProvider = { RealBundledExercisePackJsonSource(findBundledIsometricFoundationsPack()) },
            loader = ExercisePackJsonParser(),
            validator = ExercisePackValidator,
        )

        val result = repository.loadExercisePack()

        assertTrue(result is ExerciseRepositoryResult.Success)
        assertEquals(11, (result as ExerciseRepositoryResult.Success).exercisePack.exercises.size)
    }

    private fun repositoryWith(loader: FakeExercisePackLoader): DefaultExerciseRepository =
        DefaultExerciseRepository(
            sourceProvider = { NoOpExercisePackJsonSource },
            loader = loader,
            validator = ExercisePackValidator,
        )

    private class FakeExercisePackLoader(
        private val result: () -> ExercisePack,
    ) : ExercisePackLoader {
        var invocationCount = 0
            private set

        override fun load(source: ExercisePackJsonSource): ExercisePack {
            invocationCount++
            return result()
        }
    }

    private object NoOpExercisePackJsonSource : ExercisePackJsonSource {
        override fun manifest() = ""
        override fun movementPatterns() = ""
        override fun muscles() = ""
        override fun joints() = ""
        override fun exercises(): List<JsonTextFile> = emptyList()
        override fun translations(): List<JsonTextFile> = emptyList()
    }

    private fun testManifest() = ExercisePackManifest(
        id = "test-pack",
        schemaVersion = "0.2.0",
        version = "0.1.0",
        nameKey = LocalizationKey("pack.test.name"),
        descriptionKey = LocalizationKey("pack.test.description"),
        author = "Test",
        license = "MIT",
        contents = ExercisePackContents(
            movementPatterns = "movement-patterns.json",
            muscles = "muscles.json",
            joints = "joints.json",
            exercises = "exercises/",
            translations = "translations/",
        ),
    )

    private fun testMovementPattern(id: String = "squat") = MovementPattern(
        id = MovementPatternId(id),
        domain = MovementDomain.STRENGTH,
        category = "lower_body",
        nameKey = LocalizationKey("movement_pattern.$id.name"),
        descriptionKey = LocalizationKey("movement_pattern.$id.description"),
        primaryCapacities = emptyList(),
        typicalMuscles = emptyList(),
        typicalJoints = emptyList(),
        recommendedWeeklyFrequency = WeeklyFrequency(min = 1, target = 3, max = 5),
        recoveryCost = 3,
    )

    private fun testMuscle(id: String = "quadriceps") = Muscle(
        id = MuscleId(id),
        nameKey = LocalizationKey("muscle.$id.name"),
        region = MuscleRegion.CORE,
    )

    private fun testExercise(id: String, movementPatternId: String) = Exercise(
        id = ExerciseId(id),
        schemaVersion = "0.2.0",
        nameKey = LocalizationKey("exercise.$id.name"),
        descriptionKey = LocalizationKey("exercise.$id.description"),
        type = ExerciseType.ISOMETRIC,
        movementPatternId = MovementPatternId(movementPatternId),
        exerciseFamilyId = ExerciseFamilyId(id),
        difficulty = 2,
        equipment = listOf(EquipmentItem.NONE),
        homeFriendly = true,
        sideModel = SideModel.BILATERAL,
        defaultPrescription = Prescription(sets = 3, holdSeconds = 30, restSeconds = 60),
        muscles = Muscles(
            primary = listOf(MuscleLoad(MuscleId("quadriceps"), 0.5)),
            secondary = emptyList(),
            stabilizers = emptyList(),
        ),
        jointStress = emptyMap(),
        fatigueCost = FatigueCost(global = 5, local = emptyMap(), joint = emptyMap()),
        progression = listOf(ProgressionStep(id = "${id}_step_1", labelKey = LocalizationKey("progression.$id"), difficulty = 1)),
        regressions = emptyList(),
        alternatives = emptyList(),
        contraindications = emptyList(),
        cues = Cues(setup = emptyList(), during = emptyList(), stopIf = emptyList()),
        assets = ExerciseAssets(),
    )

    private fun validPack(id: String = "test_exercise") = ExercisePack(
        manifest = testManifest(),
        movementPatterns = listOf(testMovementPattern()),
        muscles = listOf(testMuscle()),
        joints = emptyList(),
        exercises = listOf(testExercise(id = id, movementPatternId = "squat")),
        translations = emptyMap(),
    )

    private fun invalidPack() = ExercisePack(
        manifest = testManifest(),
        movementPatterns = listOf(testMovementPattern()),
        muscles = listOf(testMuscle()),
        joints = emptyList(),
        exercises = listOf(testExercise(id = "test_exercise", movementPatternId = "unknown_pattern")),
        translations = emptyMap(),
    )
}
