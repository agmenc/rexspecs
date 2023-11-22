package com.rexspecs

import com.rexspecs.fixtures.calculatorRequestBuilder
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

private val expectedOutput = decorateWithErrorsAndColours(sampleInput)

private fun decorateWithErrorsAndColours(input: String) = input
    .replace("<td>56</td>", "<td style=\"color: red\">Expected [56] but was: [Unsupported operator: \"x\"]</td>")
    .replace("<td>201</td>", "<td style=\"color: red\">Expected [201] but was: [500]</td>")

val calcOneSucceeds =
    Request(Method.GET, "http://someserver.com/target?First+Param=7&Operator=%2B&Second+Param=8") to MemoryResponse(
        Status.OK,
        body = MemoryBody("15")
    )

val calcTwoSucceeds =
    Request(Method.GET, "http://someserver.com/target?First+Param=7&Operator=x&Second+Param=8") to MemoryResponse(
        Status.CREATED,
        body = MemoryBody("56")
    )

val calcTwoFails =
    Request(Method.GET, "http://someserver.com/target?First+Param=7&Operator=x&Second+Param=8") to MemoryResponse(
        Status.INTERNAL_SERVER_ERROR,
        body = MemoryBody("Unsupported operator: \"x\"")
    )

internal class RexSpecsTest {

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

        val spec = SpecExecutor(
            IdentifiedSpec(sampleInput, "some/spec/path"),
            mapOf("Calculator" to ::calculatorRequestBuilder),
            stubbedHttpHandler(mapOf(calcOneSucceeds, calcTwoSucceeds))
        )

        spec.execute().executedTables
            .flatMap { it.actualRowResults }
            .zip(expectedResults)
            .forEach { (actual, expected) -> assertEquals(expected, actual) }
    }

    @Test
    fun `We know that a passing test has passed`() {
        val passingSpec = SpecExecutor(
            IdentifiedSpec(sampleInput, "some/spec/path"),
            mapOf("Calculator" to ::calculatorRequestBuilder),
            stubbedHttpHandler(mapOf(calcOneSucceeds, calcTwoSucceeds))
        )

        val executedSpec = passingSpec.execute()
        assertEquals(sampleInput, executedSpec.output())
        assertTrue(executedSpec.success())
    }

    @Test
    fun `We know that a failing test has failed`() {
        val failingSpec = SpecExecutor(
            IdentifiedSpec(sampleInput, "some/spec/path"),
            mapOf("Calculator" to ::calculatorRequestBuilder),
            stubbedHttpHandler(mapOf())
        )

        assertFalse(failingSpec.execute().success())
    }

    @Test
    fun `Can redraw tables - with errors and highlighting - into the output doc`() {
        val expectedRow1 = RowRep(listOf("7", "+", "8"), RowResult("200", "15"))
        val expectedRow2 = RowRep(listOf("7", "x", "8"), RowResult("201", "56"))
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
        val spec = SpecExecutor(
            IdentifiedSpec(sampleInput, "some/spec/path"),
            mapOf("Calculator" to ::calculatorRequestBuilder),
            stubbedHttpHandler(mapOf(calcOneSucceeds, calcTwoSucceeds))
        )

        val executedSpec = spec.execute()

        assertEquals(sampleInput, executedSpec.output())
        assertTrue(executedSpec.success())
    }

    private fun stubbedHttpHandler(calls: Map<Request, Response>): HttpHandler = { req: Request ->
        if (!calls.containsKey(req)) {
            println("Unstubbed request: \n${req.method} ${req.uri}")
            println("Expected one of: \n${calls.map{ (k,_) -> k.toString().trim() }.joinToString("\n")}")
        }
        calls.getOrDefault(req, MemoryResponse(Status.EXPECTATION_FAILED, body = MemoryBody("Unstubbed API call")))
    }

    @Test
    fun `Can use a source file as input`() {
        val testFileContents = SingleSpecFileDatabase("src/test/resources/specs/AnAcceptanceTest.html").specs().first()
        val formattedContents = Jsoup.parse(testFileContents.specContents).toString()

        val spec = SpecExecutor(
            IdentifiedSpec(formattedContents, "some/spec/path"),
            mapOf("Calculator" to ::calculatorRequestBuilder),
            stubbedHttpHandler(mapOf(calcOneSucceeds, calcTwoFails))
        )

        val executedSpec = spec.execute()

        assertFalse(executedSpec.success())
        assertEquals(decorateWithErrorsAndColours(formattedContents), executedSpec.output())
    }

    @Test
    fun `Can write to a target file as output`() {
        val rexSpec = RexSpec(
            RexSpecPropertiesLoader.properties(),
            SingleSpecFileDatabase("src/test/resources/specs/AnAcceptanceTest.html"),
            mapOf("Calculator" to ::calculatorRequestBuilder),
            stubbedHttpHandler(mapOf(calcOneSucceeds, calcTwoFails))
        )

        val executedSuite = rexSpec.execute()
        assertFalse(executedSuite.success())

        executedSuite.writeSpecResults("rexspec-results/AnAcceptanceTest.html")

        val expectedOutputFile = sanified("src/test/resources/expectations/AnAcceptanceTest.html")
        val actualOutputFile = htmlSanitised(fileAsString("rexspec-results/AnAcceptanceTest.html"))
        assertEquals(expectedOutputFile, actualOutputFile)
    }

    class SingleSpecFileDatabase(private val sourcePath: String): FileSpecDatabase() {
        override fun specs(): List<IdentifiedSpec> {
            return listOf(IdentifiedSpec(fileAsString(sourcePath), sourcePath))
        }
    }

    @Test
    fun `Can call a real HTTP server`() {
        val rexSpec = RexSpec(
            RexSpecPropertiesLoader.properties(),
            SingleSpecFileDatabase("src/test/resources/specs/AnAcceptanceTest.html"),
            mapOf("Calculator" to ::calculatorRequestBuilder),
            HttpClient("a", 8000).handle
        )

        // TODO: Make part of rexSpec.execute()???
        rexSpec.cleanTargetDir()

        val executedSuite = rexSpec.execute()

        // TODO: Make rexSpec.execute() do this???
        executedSuite.writeSpecResults("rexspecs/AnAcceptanceTest.html")

        assertEquals(
            sanified("src/test/resources/expectations/AnAcceptanceTest.html"),
            sanified("rexspecs/AnAcceptanceTest.html")
        )
    }

    private fun sanified(filePath: String) = htmlSanitised(fileAsString(filePath))

    @Test
    @Disabled
    fun `We can clean out the target directories, so that we can do the next test run`() {}

    @Test
    @Disabled
    fun `Can run RexSpec as a Gradle test dependency`() {}

    @Test
    @Disabled
    fun `Can run RexSpec with CTRL-SHIFT-F10`() {}
}