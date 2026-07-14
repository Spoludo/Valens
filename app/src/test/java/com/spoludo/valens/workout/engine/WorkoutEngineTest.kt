package com.spoludo.valens.workout.engine

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
import com.spoludo.valens.workout.timer.WorkoutTicker
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkoutEngineTest {

    @Test
    fun run_advancesThroughFullTwoExerciseRoutine() = runTest {
        val ticker = ManualWorkoutTicker(this)
        val engine = WorkoutEngine(routine = listOf(exerciseA, exerciseB), ticker = ticker)
        backgroundScope.launch { engine.run() }

        engine.resume()
        repeat(3) { ticker.advanceOneSecond() } // countdown exhausted
        assertEquals(WorkoutPhase.WORK, engine.state.value.phase)
        assertEquals(2, engine.state.value.secondsRemaining)

        repeat(2) { ticker.advanceOneSecond() } // work exhausted (not last exercise)
        assertEquals(WorkoutPhase.REST, engine.state.value.phase)
        assertEquals(1, engine.state.value.secondsRemaining)

        ticker.advanceOneSecond() // rest exhausted -> next exercise countdown
        assertEquals(WorkoutPhase.COUNTDOWN, engine.state.value.phase)
        assertEquals(1, engine.state.value.exerciseIndex)

        repeat(3) { ticker.advanceOneSecond() } // countdown exhausted
        assertEquals(WorkoutPhase.WORK, engine.state.value.phase)

        ticker.advanceOneSecond() // work exhausted, last set + last exercise -> complete
        assertEquals(WorkoutPhase.COMPLETE, engine.state.value.phase)
        assertFalse(engine.state.value.isRunning)
    }

    @Test
    fun pause_freezesStateUntilResumed() = runTest {
        val ticker = ManualWorkoutTicker(this)
        val engine = WorkoutEngine(routine = listOf(exerciseA, exerciseB), ticker = ticker)
        backgroundScope.launch { engine.run() }

        engine.resume()
        repeat(3) { ticker.advanceOneSecond() }
        assertEquals(WorkoutPhase.WORK, engine.state.value.phase)
        assertEquals(2, engine.state.value.secondsRemaining)

        engine.pause()
        ticker.advanceOneSecond()
        assertEquals(2, engine.state.value.secondsRemaining) // unchanged while paused

        engine.resume()
        ticker.advanceOneSecond()
        assertEquals(1, engine.state.value.secondsRemaining)
    }

    @Test
    fun skip_jumpsImmediatelyWithoutWaitingOutRemainingTime() = runTest {
        val ticker = ManualWorkoutTicker(this)
        val engine = WorkoutEngine(routine = listOf(exerciseA, exerciseB), ticker = ticker)
        backgroundScope.launch { engine.run() }

        engine.resume()
        repeat(3) { ticker.advanceOneSecond() }
        assertEquals(WorkoutPhase.WORK, engine.state.value.phase)

        engine.skip()
        assertEquals(WorkoutPhase.REST, engine.state.value.phase)
    }

    @Test
    fun resume_setsHasStarted() = runTest {
        val engine = WorkoutEngine(routine = listOf(exerciseA), ticker = ManualWorkoutTicker(this))
        assertFalse(engine.state.value.hasStarted)

        engine.resume()

        assertTrue(engine.state.value.hasStarted)
    }

    /**
     * Sends a tick over a rendezvous channel and settles the test scheduler so the
     * engine's receiver coroutine has actually applied the tick before we read state
     * back — a bare `send()` only guarantees the value was handed off, not that the
     * receiver's follow-up code has run yet.
     */
    private class ManualWorkoutTicker(private val testScope: TestScope) : WorkoutTicker {
        private val trigger = Channel<Unit>(Channel.RENDEZVOUS)
        override suspend fun awaitNextSecond() {
            trigger.receive()
        }
        suspend fun advanceOneSecond() {
            trigger.send(Unit)
            testScope.runCurrent()
        }
    }

    private val exerciseA = testExercise(id = "exercise_a", holdSeconds = 2, restSeconds = 1)
    private val exerciseB = testExercise(id = "exercise_b", holdSeconds = 1, restSeconds = 1)

    private fun testExercise(id: String, holdSeconds: Int, restSeconds: Int) = Exercise(
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
        defaultPrescription = Prescription(sets = 1, holdSeconds = holdSeconds, restSeconds = restSeconds),
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
