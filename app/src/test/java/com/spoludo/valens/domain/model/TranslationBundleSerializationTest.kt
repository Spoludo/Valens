package com.spoludo.valens.domain.model

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class TranslationBundleSerializationTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun translationBundle_decodesEnglishStrings() {
        val source = """
            {
              "schemaVersion": "0.2.0",
              "locale": "en",
              "strings": {
                "pack.isometric_foundations.name": "Isometric Foundations",
                "joint.knee": "Knee"
              }
            }
        """.trimIndent()

        val bundle = json.decodeFromString<TranslationBundle>(source)

        assertEquals("0.2.0", bundle.schemaVersion)
        assertEquals("en", bundle.locale)
        assertEquals("Isometric Foundations", bundle.strings["pack.isometric_foundations.name"])
        assertEquals("Knee", bundle.strings["joint.knee"])
    }
}
