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

    @Test
    fun `Can redraw tables - with errors and highlighting - into the output doc`() {
        val expectedRow1 = TestRow(listOf("7", "+", "8"), RowResult("200", "15"))
        val expectedRow2 = TestRow(listOf("7", "x", "8"), RowResult("201", "56"))
        val actualRow1 = RowResult("200", "15")
        val actualRow2 = RowResult("400", "Unsupported operator: \"x\"")

        val executedSpec = ExecutedSpec(
            listOf(
                ExecutedTest(
                    TabularTest(
                        "Calculator",
                        listOf("First Param", "Operator", "Second Param", "HTTP Response", "Result"),
                        listOf(expectedRow1, expectedRow2)
                    ),
                    listOf(actualRow1, actualRow2)
                )
            )
        )

        val decoratedHtml = HtmlFileOutputWriter("whatever").decorateHtml(executedSpec)

        assertEquals(expectedOutput, htmlSanitised(decoratedHtml))
    }
}
