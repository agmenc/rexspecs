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

    override fun specs(): List<Spec> {
        return specIdentifiers().map { Json.decodeFromString<Spec>(it.readText()) }
    }

    fun convertJsonToTabularTest(json: String): TabularTest {
        return Json.decodeFromString<TabularTest>(json)
    }

}