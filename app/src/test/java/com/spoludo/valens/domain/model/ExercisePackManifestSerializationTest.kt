package com.spoludo.valens.domain.model

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class ExercisePackManifestSerializationTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun manifest_decodesIsometricFoundationsPack() {
        val source = """
            {
              "id": "isometric-foundations",
              "schemaVersion": "0.1.0",
              "version": "0.1.0",
              "nameKey": "pack.isometric_foundations.name",
              "descriptionKey": "pack.isometric_foundations.description",
              "author": "Valens Project",
              "license": "MIT",
              "homepage": "https://github.com/Spoludo/Valens",
              "minAppVersion": "0.1.0",
              "tags": ["isometric", "home", "beginner", "healthy-aging"],
              "contents": {
                "movementPatterns": "movement-patterns.json",
                "muscles": "muscles.json",
                "joints": "joints.json",
                "exercises": "exercises/",
                "translations": "translations/"
              }
            }
        """.trimIndent()

        val manifest = json.decodeFromString<ExercisePackManifest>(source)

        assertEquals("isometric-foundations", manifest.id)
        assertEquals("0.1.0", manifest.schemaVersion)
        assertEquals(
            listOf("isometric", "home", "beginner", "healthy-aging"),
            manifest.tags,
        )
        assertEquals("exercises/", manifest.contents.exercises)
        assertEquals("translations/", manifest.contents.translations)
    }
}
