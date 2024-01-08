package com.rexspecs.inputs

import com.rexspecs.RowResult
import com.rexspecs.TestRow
import com.rexspecs.specs.*
import com.rexspecs.utils.RexSpecPropertiesLoader
import org.jsoup.Jsoup
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class HtmlFileInputReaderTest {

    @Test
    fun `Can find a Spec by ID`() {
        val inputReader = HtmlFileInputReader("suites/rexspecs")

        assertTrue(inputReader.specs().iterator().hasNext())
    }

    @Test
    @Disabled
    fun `Specs are identified by their relative path from the specs source root directory`() {
        val inputReader = HtmlFileInputReader("rexspecs")

        assertEquals(
            listOf("AnAcceptanceTest.html", "AcceptanceTestOne.html", "nesting/AcceptanceTestTwo.html"),
            inputReader.specs().map { it.identifier }
        )
    }

    @Test
    fun `Can convert a table to a test representation`() {
        val tableElement = Jsoup.parse(sampleInput).allElements
            .toList()
            .first { it.tagName() == "table" }

        val expectedResult = TabularTest(
            "Calculator",
            listOf("First Param", "Operator", "Second Param", "HTTP Response", "Result"),
            listOf(
                TestRow(listOf("7", "+", "8"), RowResult("200", "15")),
                TestRow(listOf("7", "x", "8"), RowResult("201", "56"))
            )
        )

        val component = HtmlFileInputReader("Whatever").convertTableToTest(tableElement)

        assertEquals(expectedResult, component)
    }

    @Test
    fun `Can read in a source file as input`() {
        val props = RexSpecPropertiesLoader.properties()
        val spec = SingleHtmlFileInputReader("Calculator Over HTTP.html", props.rexspecsDirectory).specs().first()

        val expectedSpec = Spec(
            "Calculator Over HTTP.html",
            listOf(
                Title("Simple Acceptance Test Example"),
                Heading("Calculator Over HTTP Example"),
                Description("Calculator App: receives operands and an operator, and calculates the result."),
                httpCalculationTest
            )
        )


        assertEquals(expectedSpec, spec)
    }

    @Test
    @Disabled
    fun `DirectoryManager barfs when the source root doesn't contain a specs folder or a results folder`() {}
}