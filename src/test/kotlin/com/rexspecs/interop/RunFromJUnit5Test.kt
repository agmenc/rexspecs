package com.rexspecs.interop

import com.mycompany.fixture.Calculator
import com.rexspecs.RexSpecs
import com.rexspecs.connectors.DirectConnector
import com.rexspecs.directProps
import com.rexspecs.inputs.SingleJsonFileInputReader
import com.rexspecs.outputs.HtmlFileOutputWriter
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import com.rexspecs.interop.runSuitePassFail as runSuitePassFail1

class RunFromJUnit5Test {
    @Test
    fun `Any Suite with failing tests causes a JUnit5 test to fail`() {
        assertThrows<RexSpecFailedError> {
            RexSpecs.runSuitePassFail1(
                SingleJsonFileInputReader("Json Example.json", directProps.rexspecsDirectory),
                HtmlFileOutputWriter(directProps.rexspecsDirectory),
                mapOf("Calculator" to Calculator()),
                DirectConnector()
            )
        }
    }
}
