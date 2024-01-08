package com.rexspecs.interop

import com.rexspecs.RexSpecs
import com.rexspecs.inputs.sanified
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RunFromIDETest {
    @Test
    fun `Can run RexSpec with CTRL-SHIFT-F10, as if from a RunConfiguration`() {
        RexSpecs.executeSingleHtmlFile("Calculator Called Directly.html")

        assertEquals(
            sanified("src/test/resources/expectations/Calculator Called Directly.html"),
            sanified("suites/rexspecs/results/Calculator Called Directly.html")
        )
    }
}
