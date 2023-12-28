package com.rexspecs

import com.rexspecs.inputs.SingleInputReader
import com.rexspecs.outputs.FileOutputWriter
import com.rexspecs.utils.RexSpecPropertiesLoader
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
    .replace("<td>201</td>", "<td style=\"color: red\">Expected [201] but was: [400]</td>")

val calcOneSucceeds =
    Request(Method.GET, "http://not-actually-a-real-host.com/target?First+Param=7&Operator=%2B&Second+Param=8") to MemoryResponse(
        Status.OK,
        body = MemoryBody("15")
    )

val calcTwoSucceeds =
    Request(Method.GET, "http://not-actually-a-real-host.com/target?First+Param=7&Operator=x&Second+Param=8") to MemoryResponse(
        Status.CREATED,
        body = MemoryBody("56")
    )

val calcTwoFails =
    Request(Method.GET, "http://not-actually-a-real-host.com/target?First+Param=7&Operator=x&Second+Param=8") to MemoryResponse(
        Status.BAD_REQUEST,
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
        val actualRow2 = RowResult("400", "Unsupported operator: \"x\"")

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
            println("Unstubbed request: \n${prettify(req)}")
            println("Expected one of: \n${calls.map{ (k,_) -> prettify(k) }.joinToString("\n")}")
        }
        calls.getOrDefault(req, MemoryResponse(Status.EXPECTATION_FAILED, body = MemoryBody("Unstubbed API call")))
    }

    private fun prettify(req: Request): String  = "${req.method} ${req.uri} ${req.uri.path}"

    @Test
    fun `Can use a source file as input`() {
        val testFileContents = SingleInputReader("src/test/resources/specs/AnAcceptanceTest.html").specs().first()
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
        val props = RexSpecPropertiesLoader.properties()
        val outputWriter = FileOutputWriter(props.targetPath)

        val rexSpec = RexSpec(
            props.targetPath,
            SingleInputReader("src/test/resources/specs/AnAcceptanceTest.html"),
            outputWriter,
            mapOf("Calculator" to ::calculatorRequestBuilder),
            stubbedHttpHandler(mapOf(calcOneSucceeds, calcTwoFails))
        )

        val executedSuite = rexSpec.execute()
        assertFalse(executedSuite.success())

        outputWriter.writeSpecResults(executedSuite.firstSpec(), "rexspecs/AnAcceptanceTest.html")

        val expectedOutputFile = sanified("src/test/resources/expectations/AnAcceptanceTest.html")
        val actualOutputFile = htmlSanitised(fileAsString("rexspecs/AnAcceptanceTest.html"))
        assertEquals(expectedOutputFile, actualOutputFile)
    }

    @Test
    fun `Can call a real HTTP server`() {
        val props = RexSpecPropertiesLoader.properties()
        val outputWriter = FileOutputWriter(props.targetPath)

        val rexSpec = RexSpec(
            props.targetPath,
            SingleInputReader("src/test/resources/specs/AnAcceptanceTest.html"),
            outputWriter,
            mapOf("Calculator" to ::calculatorRequestBuilder),
            HttpClient(props.host, props.port).handle
        )

        // TODO: Make part of SuiteRunner
        outputWriter.cleanTargetDir()

        val executedSuite = rexSpec.execute()

        // TODO: the TestRunner should use the OutputWriter to do this
        outputWriter.writeSpecResults(executedSuite.firstSpec(), "rexspecs/AnAcceptanceTest.html")

        assertEquals(
            sanified("src/test/resources/expectations/AnAcceptanceTest.html"),
            sanified("rexspecs/AnAcceptanceTest.html")
        )
    }

    private fun sanified(filePath: String) = htmlSanitised(fileAsString(filePath))

    @Test
    @Disabled
    fun `Can run RexSpec by passing in JSON directly`() {
//        RexSpecs.executeSuite()
    }

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