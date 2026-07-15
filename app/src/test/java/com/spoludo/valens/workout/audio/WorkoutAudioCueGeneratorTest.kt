package com.spoludo.valens.workout.audio

import com.spoludo.valens.domain.model.Cues
import com.spoludo.valens.domain.model.EquipmentItem
import com.spoludo.valens.domain.model.Exercise
import com.spoludo.valens.domain.model.ExerciseAssets
import com.spoludo.valens.domain.model.ExerciseFamilyId
import com.spoludo.valens.domain.model.ExerciseId
import com.spoludo.valens.domain.model.ExerciseType
import com.spoludo.valens.domain.model.FatigueCost
import com.spoludo.valens.domain.model.LocalizationKey
import com.spoludo.valens.domain.model.MovementPatternId
import com.spoludo.valens.domain.model.Muscles
import com.spoludo.valens.domain.model.Prescription
import com.spoludo.valens.domain.model.ProgressionStep
import com.spoludo.valens.domain.model.SideModel
import com.spoludo.valens.workout.engine.PREP_COUNTDOWN_SECONDS
import com.spoludo.valens.workout.engine.WorkoutEngineState
import com.spoludo.valens.workout.engine.WorkoutPhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WorkoutAudioCueGeneratorTest {

    private val exerciseA = testExercise(id = "exercise_a", holdSeconds = 15, restSeconds = 5, sets = 1)
    private val exerciseB = testExercise(id = "exercise_b", holdSeconds = 8, restSeconds = 5, sets = 2)
    private val exerciseC = testExercise(id = "exercise_c", holdSeconds = 5, restSeconds = 5, sets = 1)
    private val exerciseD = testExercise(id = "exercise_d", holdSeconds = 10, restSeconds = 5, sets = 1)
    private val routine = listOf(exerciseA, exerciseB, exerciseC, exerciseD)

    private val names = mapOf(
        "exercise_a" to "Exercise A",
        "exercise_b" to "Exercise B",
        "exercise_c" to "Exercise C",
        "exercise_d" to "Exercise D",
    )
    private val generator = WorkoutAudioCueGenerator(routine) { exercise -> names.getValue(exercise.id.value) }

    private fun state(
        exerciseIndex: Int = 0,
        setIndex: Int = 0,
        phase: WorkoutPhase = WorkoutPhase.COUNTDOWN,
        secondsRemaining: Int = PREP_COUNTDOWN_SECONDS,
        isRunning: Boolean = true,
        hasStarted: Boolean = true,
    ) = WorkoutEngineState(exerciseIndex, setIndex, phase, secondsRemaining, isRunning, hasStarted)

    @Test
    fun cueFor_loadingBeforeStart_saysNothing() {
        val initial = state(hasStarted = false, isRunning = false)
        assertNull(generator.cueFor(null, initial))
    }

    @Test
    fun cueFor_startPressed_announcesFirstExercise() {
        val initial = state(hasStarted = false, isRunning = false)
        val started = state(hasStarted = true, isRunning = true)
        assertEquals("Let's begin. Prepare for Exercise A.", generator.cueFor(initial, started))
    }

    @Test
    fun cueFor_countdownEndsIntoWork_saysStart() {
        val countdown = state(phase = WorkoutPhase.COUNTDOWN, secondsRemaining = 1)
        val work = state(phase = WorkoutPhase.WORK, secondsRemaining = 15)
        assertEquals("Start.", generator.cueFor(countdown, work))
    }

    @Test
    fun cueFor_tenSecondsLeft_firesWhenHoldLongerThanTen() {
        val eleven = state(exerciseIndex = 0, phase = WorkoutPhase.WORK, secondsRemaining = 11)
        val ten = state(exerciseIndex = 0, phase = WorkoutPhase.WORK, secondsRemaining = 10)
        assertEquals("Ten seconds left.", generator.cueFor(eleven, ten))
    }

    @Test
    fun cueFor_tenSecondsLeft_doesNotRefireOnDuplicateEmission() {
        val ten = state(exerciseIndex = 0, phase = WorkoutPhase.WORK, secondsRemaining = 10)
        val tenAgain = state(exerciseIndex = 0, phase = WorkoutPhase.WORK, secondsRemaining = 10, isRunning = false)
        assertNull(generator.cueFor(ten, tenAgain))
    }

    @Test
    fun cueFor_tenSecondsLeft_guardedForHoldsOfTenSecondsOrLess() {
        // exerciseD's hold is exactly 10 seconds, so "ten seconds left" would be nonsensical (it's the whole hold).
        val eleven = state(exerciseIndex = 3, phase = WorkoutPhase.WORK, secondsRemaining = 11)
        val ten = state(exerciseIndex = 3, phase = WorkoutPhase.WORK, secondsRemaining = 10)
        assertNull(generator.cueFor(eleven, ten))
    }

    @Test
    fun cueFor_multiSetExercise_noEncouragementBetweenEarlierSets() {
        // exerciseB has 2 sets; finishing set 1 (setIndex 0) of 2 must stay silent.
        val work = state(exerciseIndex = 1, setIndex = 0, phase = WorkoutPhase.WORK, secondsRemaining = 1)
        val rest = state(exerciseIndex = 1, setIndex = 0, phase = WorkoutPhase.REST, secondsRemaining = 5)
        assertNull(generator.cueFor(work, rest))
    }

    @Test
    fun cueFor_multiSetExercise_encouragementOnlyOnLastSet() {
        // exerciseB's last set is setIndex 1 (2 sets total).
        val work = state(exerciseIndex = 1, setIndex = 1, phase = WorkoutPhase.WORK, secondsRemaining = 1)
        val rest = state(exerciseIndex = 1, setIndex = 1, phase = WorkoutPhase.REST, secondsRemaining = 5)
        assertEquals("Stay relaxed.", generator.cueFor(work, rest))
    }

    @Test
    fun cueFor_restToNextExercise_announcesNext() {
        val rest = state(exerciseIndex = 0, setIndex = 0, phase = WorkoutPhase.REST, secondsRemaining = 1)
        val nextCountdown = state(exerciseIndex = 1, setIndex = 0, phase = WorkoutPhase.COUNTDOWN, secondsRemaining = 3)
        assertEquals("Next: Exercise B. Prepare.", generator.cueFor(rest, nextCountdown))
    }

    @Test
    fun cueFor_restToSameExerciseNextSet_saysNothing() {
        val rest = state(exerciseIndex = 1, setIndex = 0, phase = WorkoutPhase.REST, secondsRemaining = 1)
        val nextSetCountdown = state(exerciseIndex = 1, setIndex = 1, phase = WorkoutPhase.COUNTDOWN, secondsRemaining = 3)
        assertNull(generator.cueFor(rest, nextSetCountdown))
    }

    @Test
    fun cueFor_finalWorkToComplete_saysWorkoutComplete() {
        val work = state(exerciseIndex = 3, setIndex = 0, phase = WorkoutPhase.WORK, secondsRemaining = 1)
        val complete = state(exerciseIndex = 3, setIndex = 0, phase = WorkoutPhase.COMPLETE, secondsRemaining = 0, isRunning = false)
        assertEquals("Workout complete.", generator.cueFor(work, complete))
    }

    @Test
    fun cueFor_pauseThenResume_saysNothing() {
        val running = state(exerciseIndex = 0, phase = WorkoutPhase.WORK, secondsRemaining = 8, isRunning = true)
        val paused = state(exerciseIndex = 0, phase = WorkoutPhase.WORK, secondsRemaining = 8, isRunning = false)
        assertNull(generator.cueFor(running, paused))
        assertNull(generator.cueFor(paused, running))
    }

    @Test
    fun cueFor_unknownExerciseIndex_returnsNull() {
        val anyState = state(exerciseIndex = 0)
        val outOfRange = state(exerciseIndex = 99)
        assertNull(generator.cueFor(anyState, outOfRange))
    }

    private fun testExercise(id: String, holdSeconds: Int, restSeconds: Int, sets: Int) = Exercise(
        id = ExerciseId(id),
        schemaVersion = "0.2.0",
        nameKey = LocalizationKey("exercise.$id.name"),
        descriptionKey = LocalizationKey("exercise.$id.description"),
        type = ExerciseType.ISOMETRIC,
        movementPatternId = MovementPatternId("squat"),
        exerciseFamilyId = ExerciseFamilyId(id),
        difficulty = 2,
        equipment = listOf(EquipmentItem.NONE),
        homeFriendly = true,
        sideModel = SideModel.BILATERAL,
        defaultPrescription = Prescription(sets = sets, holdSeconds = holdSeconds, restSeconds = restSeconds),
        muscles = Muscles(primary = emptyList(), secondary = emptyList(), stabilizers = emptyList()),
        jointStress = emptyMap(),
        fatigueCost = FatigueCost(global = 5, local = emptyMap(), joint = emptyMap()),
        progression = listOf(ProgressionStep(id = "${id}_step_1", labelKey = LocalizationKey("progression.$id"), difficulty = 1)),
        regressions = emptyList(),
        alternatives = emptyList(),
        contraindications = emptyList(),
        cues = Cues(setup = emptyList(), during = emptyList(), stopIf = emptyList()),
        assets = ExerciseAssets(),
    )
}
