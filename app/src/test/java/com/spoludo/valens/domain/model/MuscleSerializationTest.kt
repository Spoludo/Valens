package com.spoludo.valens.domain.model

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class MuscleSerializationTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun muscle_decodesBilateralSide() {
        val source = """
            {
              "id": "quadriceps",
              "nameKey": "muscle.quadriceps",
              "region": "thigh",
              "side": "bilateral"
            }
        """.trimIndent()

        val muscle = json.decodeFromString<Muscle>(source)

        assertEquals(MuscleId("quadriceps"), muscle.id)
        assertEquals(LocalizationKey("muscle.quadriceps"), muscle.nameKey)
        assertEquals(MuscleRegion.THIGH, muscle.region)
        assertEquals(MuscleSide.BILATERAL, muscle.side)
    }
}
