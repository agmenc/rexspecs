package com.rexspecs.outputs

import com.rexspecs.*
import com.rexspecs.inputs.expectedOutputWithFailure
import com.rexspecs.inputs.htmlSanitised
import com.rexspecs.specs.TabularTest
import com.rexspecs.specs.Title
import com.rexspecs.utils.fileAsString
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.io.path.pathString

class HtmlFileOutputWriterTest {
    private val executedSpec = ExecutedSpec(
        "GeneratedAcceptanceTest.html",
        listOf(
            ExecutedSpecComponent(
                Title("An Acceptance Test"),
                emptyList()
            ),
            ExecutedSpecComponent(
                TabularTest(
                    "Calculator",
                    listOf("First Param", "Operator", "Second Param"),
                    listOf("HTTP Response", "Result"),
                    listOf(
                        TestRow(eithers("7", "+", "8"), eithers("200", "15")),
                        TestRow(eithers("7", "x", "8"), eithers("201", "56"))
                    )
                ),
                listOf(
                    eithers("7", "+", "8", "200", "15"),
                    eithers("7", "x", "8", "400", "Unsupported operator: \"x\"")
                )
            )
        )
    )

    @Test
    fun `Can redraw tables - with errors and highlighting - into the output doc`() {
        val decoratedHtml = HtmlFileOutputWriter("whatever").generateHtml(executedSpec)

        assertEquals(expectedOutputWithFailure, htmlSanitised(decoratedHtml))
    }

    @Test
    fun `Writes Spec output to a relative path above the output directory`() {
        val outputWriter = HtmlFileOutputWriter("suites/http_examples")

        outputWriter.writeSpecResults(executedSpec)

        assertEquals(expectedOutputWithFailure, htmlSanitised(fileAsString("suites/http_examples/results/${executedSpec.identifier}")))
    }

    @Test
    fun `Raises an error if the rexspecs directory is missing`() {
        assertThrows<InvalidStartingState>("Cannot find Rexspecs directory [sausage]") {
            HtmlFileOutputWriter("sausage").prepareForOutput()
        }
    }

    @Test
    fun `Creates the results directory if it is missing`() {
        val tempRexSpecsDir = createTempDirectory().pathString
        assertFalse(File(tempRexSpecsDir, "results").exists())

        HtmlFileOutputWriter(tempRexSpecsDir).prepareForOutput()

        assertTrue(File(tempRexSpecsDir, "results").exists())
    }
}
