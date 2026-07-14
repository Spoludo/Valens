package com.spoludo.valens.data.json

import android.content.res.AssetManager

class AndroidAssetExercisePackJsonSource(
    private val assetManager: AssetManager,
) : ExercisePackJsonSource {
    override fun manifest() = readAsset("pack.json")
    override fun movementPatterns() = readAsset("movement-patterns.json")
    override fun muscles() = readAsset("muscles.json")
    override fun joints() = readAsset("joints.json")
    override fun exercises() = readAssetDirectory("exercises")
    override fun translations() = readAssetDirectory("translations")

    private fun readAsset(path: String): String =
        assetManager.open(path).bufferedReader().use { it.readText() }

    private fun readAssetDirectory(directory: String): List<JsonTextFile> =
        assetManager.list(directory).orEmpty()
            .filter { it.endsWith(".json") }
            .map { fileName -> JsonTextFile("$directory/$fileName", readAsset("$directory/$fileName")) }
}
