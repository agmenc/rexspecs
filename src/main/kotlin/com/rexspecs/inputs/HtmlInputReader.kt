package com.rexspecs.inputs

import com.rexspecs.fileAsString
import com.rexspecs.specs.HackyHtmlSpec
import com.rexspecs.specs.Spec
import java.io.File

open class HtmlInputReader(private val testRoot: String): InputReader {
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
    override fun specIdentifiers(): List<String> {
        return listOf(sourcePath)
    }
}