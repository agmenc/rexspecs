package com.rexspecs.interop

import com.rexspecs.RexSpecs
import com.rexspecs.generatedFiles
import com.rexspecs.httpProps
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RunFromGradleKtTest {
    @Test
    fun `Can run a whole RexSpec suite with just some properties`() {
        RexSpecs.executeSuiteHtml(httpProps)

        assertEquals(
            listOf("The Naughty Test.html", "Nested Spec.html", "Calculator Over HTTP.html"),
            generatedFiles(httpProps.rexspecsDirectory)
        )
    }
}