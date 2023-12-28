package com.rexspecs.inputs

import com.rexspecs.IdentifiedSpec
import com.rexspecs.fileAsString

/**
 * An InputReader knows where to find specs, and how to read them into their JSON representation
 */
interface InputReader {
    fun specs(): List<IdentifiedSpec>
}

class SingleInputReader(private val sourcePath: String): InputReader {
    override fun specs(): List<IdentifiedSpec> {
        return listOf(IdentifiedSpec(fileAsString(sourcePath), sourcePath))
    }
}