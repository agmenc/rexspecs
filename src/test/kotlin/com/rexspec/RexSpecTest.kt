package com.rexspec

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
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
        val expectedOutput = sampleInput
            .replace("<td>56</td>", "<td style=\"color: red\">56</td>")

        val doc: Document = Jsoup.parse(sampleInput)

        doc.allElements.map(::testDecorator)

        assertEquals(expectedOutput, doc.toString())
    }

    fun testDecorator(elem: Element): Element {
        return when (elem.tagName()) {
            "td" -> if (elem.text() == "56") elem.attr("style", elem.attr("style") + "color: red") else elem
            else -> elem
        }
    }

    @Test
    fun `Can call methods on a fixture based on data in table cells`() {
        data class Params(val firstParam: Int, val operator: String, val secondParam: Int)
        val actualParams = mutableListOf<Params>()
        val expectedParams = mutableListOf<Params>(
            Params(7, "+", 8),
            Params(7, "*", 8)
        )

        fun fakeCalculatorFixture(firstParam: Int, operator: String, secondParam: Int): Result {
            actualParams.add(Params(firstParam, operator, secondParam))
            return Result(500, "Bang")
        }

        val fakeIndex = mapOf<String,(Int, String, Int) -> Result>("Calculator" to ::fakeCalculatorFixture)

        execute(sampleInput, fakeIndex)

        assertEquals(expectedParams, actualParams)
    }

    fun execute(input: String, index: Map<String,(Int, String, Int) -> Result>) {
        Jsoup.parse(input).allElements
            .toList()
            .filter { it.tagName() == "table" }
            .map { testify(it) }
    }

    @Test
    fun `Can convert a table to a test representation`() {
        val tableElement = Jsoup.parse(sampleInput).allElements
            .toList()
            .first { it.tagName() == "table" }

        val expectedResult = TestRep(
            "Calculator",
            listOf(
                TestRow(listOf("7", "+", "8"), Result(200, "15")),
                TestRow(listOf("7", "x", "8"), Result(200, "56"))
            )
        )

        assertEquals(expectedResult, testify(tableElement))
    }

    data class TestRep(val fixtureName: String, val testRows: List<TestRow>)
    data class TestRow(val inputParams: List<String>, val expectedResult: Result)
    data class Result(val httpResponse: Int, val result: String)

    fun testify(table: Element): TestRep {
        val fixtureCell = table.selectXpath("//thead//tr//th").toList().first()
        val hardcodedHeadifiers = listOf("First Param", "Operator", "Second Param", "HTTP Response", "Result")
        val testRows: List<TestRow> = table.selectXpath("//tbody//tr")
            .toList()
            .map {
                val (result, params) = it.children()
                    .toList()
                    .zip(hardcodedHeadifiers)
                    .partition { (_, paramName) -> paramName == "HTTP Response" || paramName == "Result" }
                TestRow(
                    params.map { (elem, _) -> elem.text() },
                    Result(result.first().first.text().toInt(), result.last().first.text())
                )
            }

        return TestRep(fixtureCell.text(), testRows)
    }

//    TODO - better way: strip the test out as a structure, execute it, then write it back in when it is done

//    data class TestResult(val pass: Int)
//    data class ExeSpecResult(val output: String, val testResult: TestResult)
//    fun theWholeThing(input: String, fixturator: FixtureMap):


    val index = mapOf<String,(Int, String, Int) -> Result>("Calculator" to ::calculator)



    fun calculator(firstParam: Int, operator: String, secondParam: Int): Result {
        return Result(500, "It blew up")
    }

    @Test
    fun whenGroupItems_thenSuccess () {
        val theList = listOf(1, 2, 3, 4, 5, 6)
        val resultMap = theList.groupBy{ it % 3}

        assertEquals(3, resultMap.size)

//        resultMap[]

        assertTrue(resultMap[1]!!.contains(1))
        assertTrue(resultMap[2]!!.contains(5))
    }

    @Test
    fun `Reports errors back to the failed cell`() {
        val expectedOutput = sampleInput
            .replace("<td>56</td>", "<td>Operator 'x' not allowed</td>")
//        val foundFile = FileManager.find("AnAcceptanceTest.html")
//        assertIsValidHtml("poo", RexSpec().poo())
    }

    @Test
    fun `Can Find Source File`() {
//        val foundFile = FileManager.find("AnAcceptanceTest.html")
//        assertIsValidHtml("poo", RexSpec().poo())
    }
}