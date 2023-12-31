package com.rexspecs.outputs

import com.rexspecs.ExecutedSpec

interface OutputWriter {
    fun writeSpecResults(executedSpec: ExecutedSpec)
    fun cleanTargetDir()
}
