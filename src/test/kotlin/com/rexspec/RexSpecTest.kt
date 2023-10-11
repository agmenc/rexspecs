package com.rexspec

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
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

    private fun fakeFixtureReturning(function: (String) -> RexResult): Map<String, (String, String, String) -> RexResult> {
        fun fakeCalculatorFixture(firstParam: String, operator: String, secondParam: String): RexResult = function(operator)

        return mapOf<String,(String, String, String) -> RexResult>("Calculator" to ::fakeCalculatorFixture)
    }

    @Test
    fun `We know if a test has passed or failed`() {
        val passingResults = execute(sampleInput, fakeFixtureReturning { if (it == "+") RexResult(200, "15") else RexResult(200, "56") })

        assertTrue(passingResults.first().success())

        val failingResults = execute(sampleInput, fakeFixtureReturning { if (it == "+") RexResult(200, "15") else RexResult(500, "It go BOOM!!") })

        assertFalse(failingResults.first().success())
    }

    fun execute(input: String, index: Map<String,(String, String, String) -> RexResult>): List<ExecutedTest> =
        Jsoup.parse(input).allElements
            .toList()
            .filter { it.tagName() == "table" }
            .map { testify(it) }
            .map { testRep -> ExecutedTest(testRep, executeSingleTableTest(testRep, index)) }

    private fun executeSingleTableTest(rexTestRep: RexTestRep, index: Map<String, (String, String, String) -> RexResult>): List<RexResult> {
        val function: ((String, String, String) -> RexResult) = index[rexTestRep.fixtureName]!!
        return rexTestRep.rexTestRows
            .map{ row -> function(row.inputParams[0], row.inputParams[1], row.inputParams[2]) }
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

    data class RexTestRep(val fixtureName: String, val rexTestRows: List<RexTestRow>)
    data class RexTestRow(val inputParams: List<String>, val expectedResult: RexResult)
    data class RexResult(val httpResponse: Int, val result: String)

    data class ExecutedTest(val rexTestRep: RexTestRep, val results: List<RexResult>)

    fun ExecutedTest.success(): Boolean {
        rexTestRep.rexTestRows
            .map { it.expectedResult }
            .zip(results)
            .forEach { (exp, act) -> if (exp != act) return false }

        return true
    }

    fun testify(table: Element): RexTestRep {
        val fixtureCell = table.selectXpath("//thead//tr//th").toList().first()
        val hardcodedHeadifiers = listOf("First Param", "Operator", "Second Param", "HTTP Response", "Result")
        val rexTestRows: List<RexTestRow> = table.selectXpath("//tbody//tr")
            .toList()
            .map {
                val (result, params) = it.children()
                    .toList()
                    .zip(hardcodedHeadifiers)
                    .partition { (_, paramName) -> paramName == "HTTP Response" || paramName == "Result" }
                RexTestRow(
                    params.map { (elem, _) -> elem.text() },
                    RexResult(result.first().first.text().toInt(), result.last().first.text())
                )
            }

        return RexTestRep(fixtureCell.text(), rexTestRows)
    }

//    TODO - better way: strip the test out as a structure, execute it, then write it back in when it is done
//    fun theWholeThing(input: String, fixturator: FixtureMap):

    val index = mapOf<String,(Int, String, Int) -> RexResult>("Calculator" to ::calculator)

    fun calculator(firstParam: Int, operator: String, secondParam: Int): RexResult {
        return RexResult(500, "It blew up")
    }

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