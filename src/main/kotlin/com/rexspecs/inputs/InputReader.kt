package com.rexspecs.inputs

import com.rexspecs.IdentifiedSpec

interface InputReader {
    fun specs(): List<IdentifiedSpec>
}

open class FileInputReader : InputReader {
    override fun specs(): List<IdentifiedSpec> {
        TODO("Not yet implemented")
    }
}