package com.rexspecs.outputs

import com.rexspecs.*
import com.rexspecs.inputs.htmlSanitised
import com.rexspecs.inputs.sampleInput
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class HtmlFileOutputWriterTest {
    private val expectedOutput = decorateWithErrorsAndColours(sampleInput)

    private fun decorateWithErrorsAndColours(input: String) = input
        .replace("<td>56</td>", "<td style=\"color: red\">Expected [56] but was: [Unsupported operator: \"x\"]</td>")
        .replace("<td>201</td>", "<td style=\"color: red\">Expected [201] but was: [400]</td>")

    private val executedSpec = ExecutedSpec(
        "GeneratedAcceptanceTest.html",
        listOf(
            ExecutedTest(
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
        val decoratedHtml = HtmlFileOutputWriter("whatever").decorateHtml(executedSpec)

        assertEquals(expectedOutput, htmlSanitised(decoratedHtml))
    }

    @Test
    fun `Writes Spec output to a relative path above the output directory`() {
        val outputWriter = HtmlFileOutputWriter("rexspecs")

        outputWriter.writeSpecResults(executedSpec)

        assertEquals(expectedOutput, htmlSanitised(fileAsString("rexspecs/results/${executedSpec.identifier}")))
    }
}
