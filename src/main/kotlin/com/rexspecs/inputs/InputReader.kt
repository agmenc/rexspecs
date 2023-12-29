package com.rexspecs.inputs

import com.rexspecs.IdentifiedSpec
import com.rexspecs.fileAsString
import java.io.File

/**
 * An InputReader knows where to find specs, and how to read them into their JSON representations
 */
interface InputReader {
    fun specs(): List<IdentifiedSpec>
    fun rexspecs(): List<String>
}

class HtmlInputReader(private val testRoot: String): InputReader {
    override fun specs(): List<IdentifiedSpec> {
        TODO("Eliminate this method")
    }

    override fun rexspecs(): List<String> {
        return File("$testRoot/specs").walk().toList()
            .filter { it.isFile }
            .map { it.path }
    }
}

class SingleHtmlInputReader(private val sourcePath: String): InputReader {
    override fun specs(): List<IdentifiedSpec> {
        return listOf(IdentifiedSpec(fileAsString(sourcePath), sourcePath))
    }

    override fun rexspecs(): List<String> {
        return listOf(sourcePath)
    }
}