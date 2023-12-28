package com.rexspecs.inputs

import com.rexspecs.IdentifiedSpec
import com.rexspecs.fileAsString

interface InputReader {
    fun specs(): List<IdentifiedSpec>
}

open class FileInputReader : InputReader {
    override fun specs(): List<IdentifiedSpec> {
        TODO("Not yet implemented")
    }
}

class SingleInputReader(private val sourcePath: String): FileInputReader() {
    override fun specs(): List<IdentifiedSpec> {
        return listOf(IdentifiedSpec(fileAsString(sourcePath), sourcePath))
    }
}