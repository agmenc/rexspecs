package com.rexspecs

import com.rexspecs.connectors.stubbedHttpHandler
import com.rexspecs.inputs.SingleHtmlInputReader
import com.rexspecs.outputs.HtmlFileOutputWriter
import com.rexspecs.specs.HackyHtmlSpec
import com.rexspecs.utils.RexSpecPropertiesLoader
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

class RexSpecsTest {

    @Test
    fun `We know when the whole suite fails`() {
        val props = RexSpecPropertiesLoader.properties()

        val executedSuite = runSuite(
            SingleHtmlInputReader("src/test/resources/specs/AnAcceptanceTest.html"),
            HtmlFileOutputWriter(props.targetPath),
            mapOf("Calculator" to ::calculatorRequestBuilder),
            stubbedHttpHandler(mapOf(calcOneSucceeds, calcTwoFails))
        )

        assertFalse(executedSuite.success())
    }

    @Test
    fun `Can write to a target file as output`() {
        val props = RexSpecPropertiesLoader.properties()

        runSuite(
            SingleHtmlInputReader("src/test/resources/specs/AnAcceptanceTest.html"),
            HtmlFileOutputWriter(props.targetPath),
            mapOf("Calculator" to ::calculatorRequestBuilder),
            stubbedHttpHandler(mapOf(calcOneSucceeds, calcTwoFails))
        )

        val expectedOutputFile = sanified("src/test/resources/expectations/AnAcceptanceTest.html")
        val actualOutputFile = htmlSanitised(fileAsString("rexspecs/AnAcceptanceTest.html"))
        assertEquals(expectedOutputFile, actualOutputFile)
    }

    @Test
    fun `Can call a real HTTP server`() {
        val props = RexSpecPropertiesLoader.properties()

        runSuite(
            SingleHtmlInputReader("src/test/resources/specs/AnAcceptanceTest.html"),
            HtmlFileOutputWriter(props.targetPath),
            mapOf("Calculator" to ::calculatorRequestBuilder),
            HttpClient(props.host, props.port).handle
        )

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