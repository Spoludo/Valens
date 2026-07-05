package com.spoludo.valens.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class IdsTest {
    @Test
    fun exerciseId_wrapsRawValue() {
        val id = ExerciseId("wall_sit")
        assertEquals("wall_sit", id.value)
    }
}
