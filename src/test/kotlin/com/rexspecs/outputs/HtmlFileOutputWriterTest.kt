package com.rexspecs.outputs

import com.rexspecs.ExecutedSpec
import com.rexspecs.ExecutedSpecComponent
import com.rexspecs.RowResult
import com.rexspecs.TestRow
import com.rexspecs.inputs.expectedOutputWithFailure
import com.rexspecs.inputs.htmlSanitised
import com.rexspecs.specs.TabularTest
import com.rexspecs.specs.Title
import com.rexspecs.utils.fileAsString
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

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
                    listOf("First Param", "Operator", "Second Param", "HTTP Response", "Result"),
                    listOf(
                        TestRow(listOf("7", "+", "8"), RowResult("200", "15")),
                        TestRow(listOf("7", "x", "8"), RowResult("201", "56"))
                    )
                ),
                listOf(
                    RowResult("200", "15"),
                    RowResult("400", "Unsupported operator: \"x\"")
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
}
