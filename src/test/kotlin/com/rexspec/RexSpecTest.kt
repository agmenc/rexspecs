package com.rexspec

import com.rexspec.fixtures.calculatorFixture
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
            |     <td>200</td>
            |     <td>56</td>
            |    </tr>
            |   </tbody>
            |  </table>
            |  <p></p>
            | </body>
            |</html>
        """.trimMargin()

val expectedOutput = sampleInput.replace("<td>56</td>", "<td style=\"color: red\">56</td>")

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

        val spec = RexSpec(
            sampleInput,
            fakeFixtureReturning { Request(Method.POST, "localhost") },
            allOkHttpHandler()
        )

        spec.execute()
            .flatMap { it.results }
            .zip(expectedResults)
            .forEach { (actual, expected) -> assertEquals(expected, actual) }
    }

    private fun fakeFixtureReturning(function: (String) -> Request): Map<String, (List<String>) -> Request> {
        fun fakeCalculatorFixture(params: List<String>): Request = function(params[1])

        return mapOf<String,(List<String>) -> Request>("Calculator" to ::fakeCalculatorFixture)
    }

    @Test
    fun `We know if a test has passed or failed`() {

        val charset = Charsets.UTF_8
        val byteArray: ByteArray = "Hello".toByteArray(charset)
        println(byteArray.contentToString()) // [72, 101, 108, 108, 111]
        println(byteArray.toString(charset)) // Hello

        val passingSpec = RexSpec(
            sampleInput,
            fakeFixtureReturning { Request(Method.POST, "localhost") },
            allOkHttpHandler()
        )

        assertTrue(passingSpec.execute().first().success())

        val failingSpec = RexSpec(
            sampleInput,
            fakeFixtureReturning { Request(Method.POST, "localhost") },
            allOkHttpHandler()
        )

        assertFalse(failingSpec.execute().first().success())
    }

    @Test
    fun `Can take my skeleton for a walk`() {
        // parse the doc into test reps
        // Use the fixture to get an HTML request
        // Make the HTTP request
        // Mock the response
        // Convert the HTTP response into an ExecutedTestRep
        // Generate the output doc
        // Return (RexStatus, OutputDoc)

        Request(Method.POST, "localhost")
        val index = mapOf("Calculator" to ::calculatorFixture)
        val httpHandler: HttpHandler = stubbedHttpHandler(MemoryResponse(Status.OK, body = MemoryBody("monkeys")))
        RexSpec(sampleInput, index, httpHandler).execute()

//        val (status: RexStatus, sampleOutput: String) =
//
//        assertEquals(RexStatus.PASSED, status)
//        assertEquals(expectedOutput, sampleOutput)
    }

    private fun allOkHttpHandler(): HttpHandler {
        return { _: Request -> MemoryResponse(Status.OK, body = MemoryBody("Hello World")) }
    }

    private fun stubbedHttpHandler(stubbedResponse: Response): HttpHandler {
        return { _: Request -> stubbedResponse }
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