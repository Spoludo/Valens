package com.spoludo.valens.data.json

data class JsonTextFile(
    val relativePath: String,
    val content: String,
)

interface ExercisePackJsonSource {
    fun manifest(): String
    fun movementPatterns(): String
    fun muscles(): String
    fun joints(): String
    fun exercises(): List<JsonTextFile>
    fun translations(): List<JsonTextFile>
}
