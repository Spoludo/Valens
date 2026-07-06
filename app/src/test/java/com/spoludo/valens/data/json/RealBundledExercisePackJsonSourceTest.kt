package com.spoludo.valens.data.json

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RealBundledExercisePackJsonSourceTest {
    private val source = RealBundledExercisePackJsonSource(findBundledIsometricFoundationsPack())

    @Test
    fun source_locatesRealBundledPackFiles() {
        assertTrue(source.manifest().contains("isometric-foundations"))
        assertEquals(11, source.exercises().size)
        assertEquals(1, source.translations().size)
    }
}
