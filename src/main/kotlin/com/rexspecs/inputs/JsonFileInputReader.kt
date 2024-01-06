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

class SingleJsonFileInputReader(private val singleFile: String): JsonFileInputReader("rexspecs") {
    override fun specIdentifiers(): List<File> {
        // TODO: Normalise specIdentifiers in JsonFileInputReader, so that we don't need to provide a relative path from the root. Compare with HtmlFileInputReader.
        // TODO: Make this OS independent (path aware)
        return listOf(File("rexspecs/specs/$singleFile"))
    }
}