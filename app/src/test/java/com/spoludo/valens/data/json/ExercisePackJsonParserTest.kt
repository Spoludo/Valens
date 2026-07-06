package com.spoludo.valens.data.json

import com.spoludo.valens.domain.model.ExerciseId
import com.spoludo.valens.domain.model.JointLoadRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExercisePackJsonParserTest {
    private val parser = ExercisePackJsonParser()
    private val source = RealBundledExercisePackJsonSource(findBundledIsometricFoundationsPack())

    @Test
    fun load_decodesRealBundledPack() {
        val pack = parser.load(source)

        assertEquals(11, pack.exercises.size)
        assertEquals(8, pack.movementPatterns.size)

        val wallSit = pack.exercises.single { it.id == ExerciseId("wall_sit") }
        assertEquals(0.6, wallSit.jointStress["knee"]?.get(JointLoadRole.BILATERAL) ?: 0.0, 0.0001)

        val singleLegGluteBridgeHold = pack.exercises.single { it.id == ExerciseId("single_leg_glute_bridge_hold") }
        assertEquals(
            0.4,
            singleLegGluteBridgeHold.jointStress["hip"]?.get(JointLoadRole.WORKING_SIDE) ?: 0.0,
            0.0001,
        )

        val englishTranslations = pack.translations["en"]
        assertTrue(englishTranslations != null && englishTranslations.strings.isNotEmpty())
        assertEquals(
            "Isometric Foundations",
            englishTranslations?.strings?.get("pack.isometric_foundations.name"),
        )
    }
}
