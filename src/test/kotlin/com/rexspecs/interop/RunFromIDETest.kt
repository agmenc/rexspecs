package com.rexspecs.interop

import com.rexspecs.RexSpecs
import com.rexspecs.inputs.sanified
import com.rexspecs.utils.RexSpecProperties
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RunFromIDETest {
    @Test
    fun `Can run RexSpec with CTRL-SHIFT-F10, as if from a RunConfiguration`() {
        val directProps = RexSpecProperties(
            "suites/direct_examples/",
            "com.mycompany.fixture.MyFixtureRegistry",
            "com.rexspecs.connectors.DirectConnector",
            "localhost",
            58008
        )

        RexSpecs.executeSingleHtmlFile("Calculator Called Directly.html", directProps)

        assertEquals(
            sanified("src/test/resources/direct_examples/Calculator Called Directly.html"),
            sanified("suites/direct_examples/results/Calculator Called Directly.html")
        )
    }
}
