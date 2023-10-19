package com.rexspec

import com.rexspec.fixtures.calculatorRequestBuilder
import org.http4k.core.*
import org.jsoup.Jsoup
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

private val sampleInput = """
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
            |     <td>201</td>
            |     <td>56</td>
            |    </tr>
            |   </tbody>
            |  </table>
            |  <p></p>
            | </body>
            |</html>
        """.trimMargin()

private val expectedOutput = sampleInput
    .replace("<td>56</td>",  "<td style=\"color: red\">Expected [56] but was: [Unsupported operator: \"x\"]</td>")
    .replace("<td>201</td>", "<td style=\"color: red\">Expected [200] but was: [500]</td>")

private val calculationsSucceed = mapOf(
    Request(Method.GET, "http://someserver.com/target?p0=7&p1=%2b&p2=8") to MemoryResponse(
        Status.OK,
        body = MemoryBody("15")
    ),
    Request(Method.GET, "http://someserver.com/target?p0=7&p1=x&p2=8") to MemoryResponse(
        Status.CREATED,
        body = MemoryBody("56")
    )
)

internal class RexSpecTest {

    @Test
    fun `Can convert a table to a test representation`() {
        val tableElement = Jsoup.parse(sampleInput).allElements
            .toList()
            .first { it.tagName() == "table" }

        val expectedResult = TableRep(
            "Calculator",
            listOf("First Param", "Operator", "Second Param", "HTTP Response", "Result"),
            listOf(
                RowRep(listOf("7", "+", "8"), RowResult("200", "15")),
                RowRep(listOf("7", "x", "8"), RowResult("201", "56"))
            )
        )

        assertEquals(expectedResult, convertTablesToTableReps(tableElement))
    }

    @Test
    fun `Captures the results of fixture calls`() {
        val expectedResults = listOf(
            RowResult("200", "15"),
            RowResult("201", "56"),
        )

        val spec = RexSpec(
            sampleInput,
            mapOf("Calculator" to urlParamsRequestBuilder()),
            stubbedHttpHandler(calculationsSucceed)
        )

        spec.execute().executedTables
            .flatMap { it.actualRowResults }
            .zip(expectedResults)
            .forEach { (actual, expected) -> assertEquals(expected, actual) }
    }

    private fun urlParamsRequestBuilder(): (List<String>) -> Request = { params: List<String> ->
        Request(Method.GET, "http://someserver.com/target?p0=${params[0]}&p1=${encodePlus(params[1])}&p2=${params[2]}")
    }

    private fun encodePlus(param: String) = if (param == "+") "%2b" else param

    @Test
    fun `We know that a passing test has passed`() {
        val passingSpec = RexSpec(
            sampleInput,
            mapOf("Calculator" to urlParamsRequestBuilder()),
            stubbedHttpHandler(calculationsSucceed)
        )

        val executedSpec = passingSpec.execute()
        assertEquals(sampleInput, executedSpec.output())
        assertTrue(executedSpec.success())
    }

    @Test
    fun `We know that a failing test has failed`() {
        val failingSpec = RexSpec(
            sampleInput,
            mapOf("Calculator" to urlParamsRequestBuilder()),
            stubbedHttpHandler(mapOf())
        )

        assertFalse(failingSpec.execute().success())
    }

    @Test
    fun `Can redraw tables into the output doc`() {
        val expectedRow1 = RowRep(listOf("7", "+", "8"), RowResult("200", "15"))
        val expectedRow2 = RowRep(listOf("7", "x", "8"), RowResult("200", "56"))
        val actualRow1 = RowResult("200", "15")
        val actualRow2 = RowResult("500", "Unsupported operator: \"x\"")

        val executedSpec = ExecutedSpec(
            sampleInput,
            listOf(
                ExecutedTable(
                    TableRep(
                        "Calculator",
                        listOf("First Param", "Operator", "Second Param", "HTTP Response", "Result"),
                        listOf(expectedRow1, expectedRow2)
                    ),
                    listOf(actualRow1, actualRow2)
                )
            )
        )

        assertEquals(expectedOutput, executedSpec.output())
    }

    @Test
    fun `Can use Fixture to build HTTP requests`() {
        val spec = RexSpec(
            sampleInput,
            mapOf("Calculator" to ::calculatorRequestBuilder),
            stubbedHttpHandler(calculationsSucceed)
        )

        val executedSpec = spec.execute()

        assertEquals(sampleInput, executedSpec.output())
        assertTrue(executedSpec.success())
    }

    private fun stubbedHttpHandler(calls: Map<Request, Response>): HttpHandler = { req: Request ->
        calls.getOrDefault(req, MemoryResponse(Status.EXPECTATION_FAILED, body = MemoryBody("Unstubbed API call")))
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