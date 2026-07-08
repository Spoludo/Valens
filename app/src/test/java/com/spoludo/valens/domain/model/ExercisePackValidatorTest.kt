package com.spoludo.valens.domain.model

import com.spoludo.valens.data.json.ExercisePackJsonParser
import com.spoludo.valens.data.json.RealBundledExercisePackJsonSource
import com.spoludo.valens.data.json.findBundledIsometricFoundationsPack
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExercisePackValidatorTest {

    @Test
    fun validate_realBundledPack_hasNoErrors() {
        val parser = ExercisePackJsonParser()
        val source = RealBundledExercisePackJsonSource(findBundledIsometricFoundationsPack())
        val pack = parser.load(source)

        val result = ExercisePackValidator.validate(pack)

        assertTrue(result.errors.toString(), result.isValid)
    }

    @Test
    fun validate_minimalValidPack_hasNoErrors() {
        val pack = testPack(exercises = listOf(testExercise()))

        val result = ExercisePackValidator.validate(pack)

        assertTrue(result.isValid)
    }

    @Test
    fun validate_unknownMovementPatternId_reportsError() {
        val pack = testPack(exercises = listOf(testExercise(movementPatternId = "unknown_pattern")))

        val result = ExercisePackValidator.validate(pack)

        assertTrue(
            result.errors.contains(
                ExercisePackValidationError.UnknownReference(
                    ExerciseId("test_exercise"),
                    ExercisePackReferenceField.MOVEMENT_PATTERN_ID,
                    "unknown_pattern",
                ),
            ),
        )
    }

    @Test
    fun validate_unknownMuscleId_reportsError() {
        val pack = testPack(exercises = listOf(testExercise(primaryMuscles = listOf("unknown_muscle"))))

        val result = ExercisePackValidator.validate(pack)

        assertTrue(
            result.errors.contains(
                ExercisePackValidationError.UnknownReference(
                    ExerciseId("test_exercise"),
                    ExercisePackReferenceField.MUSCLE_PRIMARY,
                    "unknown_muscle",
                ),
            ),
        )
    }

    @Test
    fun validate_unknownJointInJointStress_reportsError() {
        val pack = testPack(
            exercises = listOf(
                testExercise(jointStress = mapOf("unknown_joint" to mapOf(JointLoadRole.BILATERAL to 0.5))),
            ),
        )

        val result = ExercisePackValidator.validate(pack)

        assertTrue(
            result.errors.contains(
                ExercisePackValidationError.UnknownReference(
                    ExerciseId("test_exercise"),
                    ExercisePackReferenceField.JOINT_STRESS,
                    "unknown_joint",
                ),
            ),
        )
    }

    @Test
    fun validate_unknownJointInFatigueCost_reportsError() {
        val pack = testPack(
            exercises = listOf(
                testExercise(fatigueJoint = mapOf("unknown_joint" to mapOf(JointLoadRole.BILATERAL to 5))),
            ),
        )

        val result = ExercisePackValidator.validate(pack)

        assertTrue(
            result.errors.contains(
                ExercisePackValidationError.UnknownReference(
                    ExerciseId("test_exercise"),
                    ExercisePackReferenceField.FATIGUE_COST_JOINT,
                    "unknown_joint",
                ),
            ),
        )
    }

    @Test
    fun validate_invalidAlternativeId_reportsError() {
        val pack = testPack(exercises = listOf(testExercise(alternatives = listOf("nonexistent"))))

        val result = ExercisePackValidator.validate(pack)

        assertTrue(
            result.errors.contains(
                ExercisePackValidationError.UnknownReference(
                    ExerciseId("test_exercise"),
                    ExercisePackReferenceField.ALTERNATIVE,
                    "nonexistent",
                ),
            ),
        )
    }

    @Test
    fun validate_invalidRegressionId_reportsError() {
        val pack = testPack(exercises = listOf(testExercise(regressions = listOf("nonexistent"))))

        val result = ExercisePackValidator.validate(pack)

        assertTrue(
            result.errors.contains(
                ExercisePackValidationError.UnknownReference(
                    ExerciseId("test_exercise"),
                    ExercisePackReferenceField.REGRESSION,
                    "nonexistent",
                ),
            ),
        )
    }

    @Test
    fun validate_regressionReferencingKnownProgressionStepId_isValid() {
        val exerciseA = testExercise(id = "exercise_a", regressions = listOf("exercise_b_step_1"))
        val exerciseB = testExercise(id = "exercise_b")
        val pack = testPack(exercises = listOf(exerciseA, exerciseB))

        val result = ExercisePackValidator.validate(pack)

        assertTrue(
            result.errors.none { it is ExercisePackValidationError.UnknownReference && it.field == ExercisePackReferenceField.REGRESSION },
        )
    }

    @Test
    fun validate_duplicateExerciseId_reportsError() {
        val pack = testPack(exercises = listOf(testExercise(id = "dup"), testExercise(id = "dup")))

        val result = ExercisePackValidator.validate(pack)

        assertTrue(result.errors.contains(ExercisePackValidationError.DuplicateId(DuplicateIdKind.EXERCISE, "dup")))
    }

    @Test
    fun validate_duplicateMovementPatternId_reportsError() {
        val pack = testPack(
            exercises = listOf(testExercise()),
            movementPatterns = listOf(testMovementPattern(), testMovementPattern()),
        )

        val result = ExercisePackValidator.validate(pack)

        assertTrue(
            result.errors.contains(ExercisePackValidationError.DuplicateId(DuplicateIdKind.MOVEMENT_PATTERN, "squat")),
        )
    }

    @Test
    fun validate_duplicateMuscleId_reportsError() {
        val pack = testPack(
            exercises = listOf(testExercise()),
            muscles = listOf(testMuscle("quadriceps"), testMuscle("quadriceps")),
        )

        val result = ExercisePackValidator.validate(pack)

        assertTrue(
            result.errors.contains(ExercisePackValidationError.DuplicateId(DuplicateIdKind.MUSCLE, "quadriceps")),
        )
    }

    @Test
    fun validate_duplicateJointId_reportsError() {
        val pack = testPack(
            exercises = listOf(testExercise()),
            joints = listOf(testJoint("knee"), testJoint("knee")),
        )

        val result = ExercisePackValidator.validate(pack)

        assertTrue(result.errors.contains(ExercisePackValidationError.DuplicateId(DuplicateIdKind.JOINT, "knee")))
    }

    @Test
    fun validate_duplicateProgressionStepId_reportsError() {
        val pack = testPack(exercises = listOf(testExercise(progressionIds = listOf("step_a", "step_a"))))

        val result = ExercisePackValidator.validate(pack)

        assertTrue(
            result.errors.contains(
                ExercisePackValidationError.DuplicateProgressionStepId(ExerciseId("test_exercise"), "step_a"),
            ),
        )
        assertEquals(1, result.errors.count { it is ExercisePackValidationError.DuplicateProgressionStepId })
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

    private fun testMuscle(id: String) = Muscle(
        id = MuscleId(id),
        nameKey = LocalizationKey("muscle.$id.name"),
        region = MuscleRegion.CORE,
    )

    private fun testJoint(id: String) = Joint(
        id = JointId(id),
        nameKey = LocalizationKey("joint.$id.name"),
        region = JointRegion.KNEE,
        sides = listOf(JointSide.MIDLINE),
    )

    private fun testExercise(
        id: String = "test_exercise",
        movementPatternId: String = "squat",
        primaryMuscles: List<String> = listOf("quadriceps"),
        jointStress: Map<String, Map<JointLoadRole, Double>> = mapOf("knee" to mapOf(JointLoadRole.BILATERAL to 0.5)),
        fatigueJoint: Map<String, Map<JointLoadRole, Int>> = mapOf("knee" to mapOf(JointLoadRole.BILATERAL to 5)),
        regressions: List<String> = emptyList(),
        alternatives: List<String> = emptyList(),
        progressionIds: List<String> = listOf("${id}_step_1", "${id}_step_2"),
    ) = Exercise(
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
            primary = primaryMuscles.map { MuscleLoad(MuscleId(it), 0.5) },
            secondary = emptyList(),
            stabilizers = emptyList(),
        ),
        jointStress = jointStress,
        fatigueCost = FatigueCost(global = 5, local = emptyMap(), joint = fatigueJoint),
        progression = progressionIds.map {
            ProgressionStep(id = it, labelKey = LocalizationKey("progression.$it"), difficulty = 1)
        },
        regressions = regressions.map { ExerciseId(it) },
        alternatives = alternatives.map { ExerciseId(it) },
        contraindications = emptyList(),
        cues = Cues(setup = emptyList(), during = emptyList(), stopIf = emptyList()),
        assets = ExerciseAssets(),
    )

    private fun testPack(
        exercises: List<Exercise>,
        movementPatterns: List<MovementPattern> = listOf(testMovementPattern()),
        muscles: List<Muscle> = listOf(testMuscle("quadriceps")),
        joints: List<Joint> = listOf(testJoint("knee")),
    ) = ExercisePack(
        manifest = testManifest(),
        movementPatterns = movementPatterns,
        muscles = muscles,
        joints = joints,
        exercises = exercises,
        translations = emptyMap(),
    )
}
