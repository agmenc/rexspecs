package com.rexspecs.inputs

import com.rexspecs.IdentifiedSpec
import com.rexspecs.fileAsString
import com.rexspecs.specs.HackyHtmlSpec
import com.rexspecs.specs.Spec
import java.io.File

/**
 * An InputReader knows where to find specs, and how to read them into their JSON representations
 */
interface InputReader {
    fun specs(): List<IdentifiedSpec>
    fun specIdentifiers(): List<String>
    fun speccies(): List<Spec>
}

open class HtmlInputReader(private val testRoot: String): InputReader {
    override fun specs(): List<IdentifiedSpec> {
        TODO("Eliminate this method")
    }

    override fun specIdentifiers(): List<String> {
        return File("$testRoot/specs").walk().toList()
            .filter { it.isFile }
            .map { it.path }
    }

    override fun speccies(): List<Spec> {
        return specIdentifiers().map { HackyHtmlSpec(fileAsString(it)) }
    }
}

class SingleHtmlInputReader(private val sourcePath: String): HtmlInputReader(sourcePath) {
    override fun specs(): List<IdentifiedSpec> {
        return listOf(IdentifiedSpec(fileAsString(sourcePath), sourcePath))
    }

    override fun specIdentifiers(): List<String> {
        return listOf(sourcePath)
    }
}