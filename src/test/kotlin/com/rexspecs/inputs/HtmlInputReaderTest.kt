package com.rexspecs.inputs

import com.rexspecs.RowResult
import com.rexspecs.TabularTest
import com.rexspecs.TestRow
import com.rexspecs.specs.Spec
import com.rexspecs.specs.calculationTest
import org.jsoup.Jsoup
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class HtmlInputReaderTest {
    @Test
    fun `Knows which tests can be found`() {
        val inputReader = HtmlInputReader("rexspecs")

        assertEquals(
            listOf("rexspecs/specs/AnAcceptanceTest.html", "rexspecs/specs/AcceptanceTestOne.html", "rexspecs/specs/nesting/AcceptanceTestTwo.html"),
            inputReader.specIdentifiers()
        )
    }

    @Test
    fun `Can find a Spec by ID`() {
        val inputReader = HtmlInputReader("rexspecs")

        assertTrue(inputReader.specs().iterator().hasNext())
    }

    @Test
    @Disabled
    fun `Can iterate through a Spec's components`() {
        val inputReader = HtmlInputReader("rexspecs")

        val spec: Spec = inputReader.specs().first()

        assertTrue(spec.components.isNotEmpty())
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

        val component = HtmlInputReader("Whatever").convertTableToTest(tableElement)

        assertEquals(expectedResult, component)
    }

    @Test
    fun `Can read in a source file as input`() {
        val spec = SingleHtmlInputReader("rexspecs/specs/AnAcceptanceTest.html").specs().first()

        assertEquals(calculationTest, spec.components.first())
    }

    @Test
    @Disabled
    fun `DirectoryManager barfs when the source root doesn't contain a specs folder or a results folder`() {}
}