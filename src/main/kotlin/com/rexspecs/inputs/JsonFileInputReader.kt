package com.rexspecs.inputs

import com.rexspecs.specs.Spec
import com.rexspecs.specs.TabularTest
import kotlinx.serialization.json.Json
import java.io.File

open class JsonFileInputReader(rexspecsDirectory: String) : InputReader {
    protected val specsRoot = File(rexspecsDirectory, "specs")

    protected open fun specIdentifiers(): List<File> =
        specsRoot
            .walk()
            .toList()
            .filter { it.isFile }
            .map { it.relativeTo(specsRoot) }

    override fun specs(): List<Spec> =
        specIdentifiers().map {
            Json.decodeFromString<Spec>(File(specsRoot.path, it.path).readText())
        }

    fun convertJsonToTabularTest(json: String): TabularTest = Json.decodeFromString<TabularTest>(json)
}

class SingleJsonFileInputReader(private val singleFile: String, rexspecsDirectory: String): JsonFileInputReader(rexspecsDirectory) {
    override fun specIdentifiers(): List<File> = listOf(File(singleFile))
}