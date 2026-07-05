package com.spoludo.valens.domain.model

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class MovementPatternSerializationTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun movementPattern_decodesSquat() {
        val source = """
            {
              "id": "squat",
              "domain": "strength",
              "category": "lower_body_squat",
              "nameKey": "movement.squat.name",
              "descriptionKey": "movement.squat.description",
              "primaryCapacities": ["lower_body_strength", "knee_tolerance"],
              "typicalMuscles": ["quadriceps", "glutes", "hamstrings", "adductors"],
              "typicalJoints": ["knee", "hip", "ankle"],
              "functionalPurposeKey": "movement.squat.purpose",
              "recommendedWeeklyFrequency": {"min": 1, "target": 3, "max": 5},
              "minimumEffectiveDoseSeconds": 60,
              "recoveryCost": 6
            }
        """.trimIndent()

        val pattern = json.decodeFromString<MovementPattern>(source)

        assertEquals(MovementPatternId("squat"), pattern.id)
        assertEquals(MovementDomain.STRENGTH, pattern.domain)
        assertEquals(
            listOf(CapacityId("lower_body_strength"), CapacityId("knee_tolerance")),
            pattern.primaryCapacities,
        )
        assertEquals(
            listOf(JointId("knee"), JointId("hip"), JointId("ankle")),
            pattern.typicalJoints,
        )
        assertEquals(WeeklyFrequency(min = 1, target = 3, max = 5), pattern.recommendedWeeklyFrequency)
        assertEquals(6, pattern.recoveryCost)
    }
}
