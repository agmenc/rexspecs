package com.rexspecs.interop

import com.rexspecs.FixtureLookup
import com.rexspecs.RexSpecs
import com.rexspecs.connectors.Connector
import com.rexspecs.inputs.InputReader
import com.rexspecs.outputs.OutputWriter

public fun RexSpecs.Companion.runSuitePassFail(
    inputReader: InputReader,
    outputWriter: OutputWriter,
    fixtureLookup: FixtureLookup,
    connector: Connector
): Unit {
    val result = runSuite(
        inputReader,
        outputWriter,
        fixtureLookup,
        connector
    )

    if (!result.success()) throw RexSpecFailedError("RexSpecs suite failed")
}

class RexSpecFailedError(message: String) : AssertionError(message)