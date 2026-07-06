package com.spoludo.valens.data.json

import java.io.File

fun findBundledIsometricFoundationsPack(): File {
    var dir: File? = File(System.getProperty("user.dir")).canonicalFile
    while (dir != null) {
        val candidate = File(dir, "exercise-packs/bundled/isometric-foundations/pack.json")
        if (candidate.exists()) {
            return candidate.parentFile
        }
        dir = dir.parentFile
    }
    error("Could not locate exercise-packs/bundled/isometric-foundations from ${System.getProperty("user.dir")}")
}

class RealBundledExercisePackJsonSource(private val root: File) : ExercisePackJsonSource {
    override fun manifest() = File(root, "pack.json").readText()
    override fun movementPatterns() = File(root, "movement-patterns.json").readText()
    override fun muscles() = File(root, "muscles.json").readText()
    override fun joints() = File(root, "joints.json").readText()

    override fun exercises(): List<JsonTextFile> =
        File(root, "exercises").listFiles { file -> file.extension == "json" }
            .orEmpty()
            .map { JsonTextFile(relativePath = "exercises/${it.name}", content = it.readText()) }

    override fun translations(): List<JsonTextFile> =
        File(root, "translations").listFiles { file -> file.extension == "json" }
            .orEmpty()
            .map { JsonTextFile(relativePath = "translations/${it.name}", content = it.readText()) }
}
