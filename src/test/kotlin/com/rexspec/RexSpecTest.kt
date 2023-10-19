package com.rexspec

import com.rexspec.fixtures.calculatorRequestBuilder
import org.http4k.core.*
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
            |     <td>201</td>
            |     <td>56</td>
            |    </tr>
            |   </tbody>
            |  </table>
            |  <p></p>
            | </body>
            |</html>
        """.trimMargin()

val expectedOutput = sampleInput
    .replace("<td>56</td>",  "<td style=\"color: red\">Expected [56] but was [Unsupported operator: \"x\"]</td>")
    .replace("<td>201</td>", "<td style=\"color: red\">Expected [200] but was: [500]</td>")

internal class RexSpecTest {

    @Test
    fun `Can decorate a document by colouring in some cells`() {
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

        val expectedResult = TableRep(
            "Calculator",
            listOf("First Param", "Operator", "Second Param", "HTTP Response", "Result"),
            listOf(
                RowRep(listOf("7", "+", "8"), RowResult("200", "15")),
                RowRep(listOf("7", "x", "8"), RowResult("200", "56"))
            )
        )

        assertEquals(expectedResult, convertTablesToTableReps(tableElement))
    }

    @Test
    fun `Captures the results of fixture calls`() {
        val expectedResults = listOf(
            RowResult("200", "15"),
            RowResult("200", "56"),
        )

        val spec = RexSpec(
            sampleInput,
            mapOf("Calculator" to urlParamsRequestBuilder()),
            stubbedHttpHandler(
                mapOf(
                    Request(Method.GET, "http://someserver.com/target?p0=7&p1=%2b&p2=8") to MemoryResponse(
                        Status.OK,
                        body = MemoryBody("15")
                    ),
                    Request(Method.GET, "http://someserver.com/target?p0=7&p1=x&p2=8") to MemoryResponse(
                        Status.OK,
                        body = MemoryBody("56")
                    )
                )
            )
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
            stubbedHttpHandler(
                mapOf(
                    Request(Method.GET, "http://someserver.com/target?p0=7&p1=%2b&p2=8") to MemoryResponse(
                        Status.OK,
                        body = MemoryBody("15")
                    ),
                    Request(Method.GET, "http://someserver.com/target?p0=7&p1=x&p2=8") to MemoryResponse(
                        Status.OK,
                        body = MemoryBody("56")
                    )
                )
            )
        )

        assertTrue(passingSpec.execute().success())
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
    fun `I can redraw tables into the output doc`() {
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
    fun `Can take my skeleton for a walk`() {
        val calls = mapOf(Request(Method.POST, "localhost") to MemoryResponse(Status.OK, body = MemoryBody("monkeys")))
        val spec = RexSpec(sampleInput, mapOf("Calculator" to ::calculatorRequestBuilder), stubbedHttpHandler(calls))

        val results: ExecutedSpec = spec.execute()

        // Generate the output doc

        // Return (RexStatus, OutputDoc)

//        val (status: RexStatus, sampleOutput: String) =
//
//        assertEquals(RexStatus.PASSED, status)
//        assertEquals(expectedOutput, sampleOutput)
    }

    private fun stubbedHttpHandler(calls: Map<Request, Response>): HttpHandler = { req: Request ->
        calls.getOrDefault(req, MemoryResponse(Status.EXPECTATION_FAILED, body = MemoryBody("Unstubbed API call")))
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