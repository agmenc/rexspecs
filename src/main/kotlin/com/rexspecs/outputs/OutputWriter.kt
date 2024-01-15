package com.rexspecs.outputs

import com.rexspecs.ExecutedSpec

interface OutputWriter {
    fun writeSpecResults(executedSpec: ExecutedSpec)

    // Ensure that target locations exist and are accessible. Clear old content from directories, databases, etc.
    fun prepareForOutput()
}
