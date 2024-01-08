package com.rexspecs.interop

import com.rexspecs.RexSpecs
import com.rexspecs.inputs.sanified
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RunFromIDETest {
    @Test
    fun `Can run RexSpec with CTRL-SHIFT-F10, as if from a RunConfiguration`() {
        RexSpecs.executeSingleHtmlFile("DirectlyCalledExample.html")

        assertEquals(
            sanified("src/test/resources/expectations/DirectlyCalledExample.html"),
            sanified("suites/rexspecs/results/DirectlyCalledExample.html")
        )
    }
}
