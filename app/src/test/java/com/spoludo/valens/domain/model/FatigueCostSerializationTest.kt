package com.spoludo.valens.domain.model

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class FatigueCostSerializationTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun fatigueCost_decodesBilateralJointRoleAndLocalMuscleKeys() {
        val source = """
            {
              "global": 6,
              "local": {
                "quadriceps": 8,
                "glutes": 5
              },
              "joint": {
                "knee": {
                  "bilateral": 6
                }
              }
            }
        """.trimIndent()

        val fatigueCost = json.decodeFromString<FatigueCost>(source)

        assertEquals(6, fatigueCost.global)
        assertEquals(8, fatigueCost.local["quadriceps"])
        assertEquals(5, fatigueCost.local["glutes"])
        assertEquals(6, fatigueCost.joint["knee"]?.get(JointLoadRole.BILATERAL))
    }

    @Test
    fun fatigueCost_decodesWorkingSideJointRole() {
        val source = """
            {
              "global": 5,
              "local": {
                "glutes": 8,
                "hamstrings": 4,
                "core": 3
              },
              "joint": {
                "hip": {
                  "workingSide": 4
                },
                "knee": {
                  "workingSide": 2
                }
              }
            }
        """.trimIndent()

        val fatigueCost = json.decodeFromString<FatigueCost>(source)

        assertEquals(3, fatigueCost.local["core"])
        assertEquals(4, fatigueCost.joint["hip"]?.get(JointLoadRole.WORKING_SIDE))
        assertEquals(2, fatigueCost.joint["knee"]?.get(JointLoadRole.WORKING_SIDE))
    }
}
