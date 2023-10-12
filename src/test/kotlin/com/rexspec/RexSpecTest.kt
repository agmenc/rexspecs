package com.rexspec

import com.rexspec.fixtures.calculatorFixture
import org.jsoup.Jsoup
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test


val sampleInput = """
            |<html>
            | <head></head>
            | <body>
            |  <p>An <a href="http://example.com/"><b>example</b></a></p>
            |  <table>
            |   <thead>
            |    <tr>
            |     <th>Calculator</th>
            |    </tr>
            |    <tr>
            |     <th>First Param</th>
            |     <th>Operator</th>
            |     <th>Second Param</th>
            |     <th>HTTP Response</th>
            |     <th>Result</th>
            |    </tr>
            |   </thead>
            |   <tbody>
            |    <tr>
            |     <td>7</td>
            |     <td>+</td>
            |     <td>8</td>
            |     <td>200</td>
            |     <td>15</td>
            |    </tr>
            |    <tr>
            |     <td>7</td>
            |     <td>x</td>
            |     <td>8</td>
            |     <td>200</td>
            |     <td>56</td>
            |    </tr>
            |   </tbody>
            |  </table>
            |  <p></p>
            | </body>
            |</html>
        """.trimMargin()

internal class RexSpecTest {

    @Test
    fun `Can decorate a document by colouring in some cells`() {
        val expectedOutput = sampleInput.replace("<td>56</td>", "<td style=\"color: red\">56</td>")

        val elements = Jsoup.parse(sampleInput).allElements.map {
            when (it.tagName()) {
                "td" -> if (it.text() == "56") it.attr("style", it.attr("style") + "color: red") else it
                else -> it
            }
        }

        assertEquals(expectedOutput, elements.first().toString())
    }

    @Test
    fun `Can convert a table to a test representation`() {
        val tableElement = Jsoup.parse(sampleInput).allElements
            .toList()
            .first { it.tagName() == "table" }

        val expectedResult = RexTestRep(
            "Calculator",
            listOf(
                RexTestRow(listOf("7", "+", "8"), RexResult(200, "15")),
                RexTestRow(listOf("7", "x", "8"), RexResult(200, "56"))
            )
        )

        assertEquals(expectedResult, testify(tableElement))
    }

    @Test
    fun `Captures the results of fixture calls`() {
        val expectedResults = listOf(
            RexResult(200, "15"),
            RexResult(200, "56"),
        )

        val results = execute(sampleInput, fakeFixtureReturning { operator -> if (operator == "+") RexResult(200, "15") else RexResult(200, "56") })

        results
            .flatMap { it.results }
            .zip(expectedResults)
            .forEach {(actual, expected) ->  assertEquals(expected, actual) }
    }

    private fun fakeFixtureReturning(function: (String) -> RexResult): Map<String, (List<String>) -> RexResult> {
        fun fakeCalculatorFixture(params: List<String>): RexResult = function(params[1])

        return mapOf<String,(List<String>) -> RexResult>("Calculator" to ::fakeCalculatorFixture)
    }

    @Test
    fun `We know if a test has passed or failed`() {
        val passingResults = execute(sampleInput, fakeFixtureReturning { if (it == "+") RexResult(200, "15") else RexResult(200, "56") })

        assertTrue(passingResults.first().success())

        val failingResults = execute(sampleInput, fakeFixtureReturning { if (it == "+") RexResult(200, "15") else RexResult(500, "It go BOOM!!") })

        assertFalse(failingResults.first().success())
    }

    @Test
    fun `Can Call a real fixture using a real index`() {
        execute(sampleInput, mapOf("Calculator" to ::calculatorFixture))
    }

//    TODO - better way: strip the test out as a structure, execute it, then write it back in when it is done
//    fun theWholeThing(input: String, fixturator: FixtureMap):

    @Test
    @Disabled
    fun `Reports errors back to the failed cell`() {
        val expectedOutput = sampleInput
            .replace("<td>56</td>", "<td>Operator 'x' not allowed</td>")
//        val foundFile = FileManager.find("AnAcceptanceTest.html")
//        assertIsValidHtml("poo", RexSpec().poo())
    }

    @Test
    @Disabled
    fun `Can Find Source File`() {
//        val foundFile = FileManager.find("AnAcceptanceTest.html")
//        assertIsValidHtml("poo", RexSpec().poo())
    }
}