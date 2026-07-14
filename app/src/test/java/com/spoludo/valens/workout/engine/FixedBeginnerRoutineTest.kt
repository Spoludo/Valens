package com.spoludo.valens.workout.engine

import com.spoludo.valens.data.json.ExercisePackJsonParser
import com.spoludo.valens.data.json.RealBundledExercisePackJsonSource
import com.spoludo.valens.data.json.findBundledIsometricFoundationsPack
import com.spoludo.valens.domain.model.ExerciseId
import org.junit.Assert.assertEquals
import org.junit.Test

class FixedBeginnerRoutineTest {
    @Test
    fun build_realBundledPack_returnsExpectedSequence() {
        val pack = ExercisePackJsonParser().load(
            RealBundledExercisePackJsonSource(findBundledIsometricFoundationsPack()),
        )

        val routine = FixedBeginnerRoutine.build(pack)

        assertEquals(
            listOf(
                "wall_push", "hollow_body_hold", "calf_raise_hold", "wall_sit",
                "single_leg_balance_hold", "reverse_table_hold", "plank_hold",
            ).map { ExerciseId(it) },
            routine.map { it.id },
        )
    }
}
