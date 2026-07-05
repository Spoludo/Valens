package com.spoludo.valens.domain.model

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class JointSerializationTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun joint_decodesLeftRightSides() {
        val source = """
            {
              "id": "knee",
              "nameKey": "joint.knee",
              "region": "knee",
              "sides": ["left", "right"]
            }
        """.trimIndent()

        val joint = json.decodeFromString<Joint>(source)

        assertEquals(JointId("knee"), joint.id)
        assertEquals(LocalizationKey("joint.knee"), joint.nameKey)
        assertEquals(JointRegion.KNEE, joint.region)
        assertEquals(listOf(JointSide.LEFT, JointSide.RIGHT), joint.sides)
    }
}
