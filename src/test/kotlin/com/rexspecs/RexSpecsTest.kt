package com.rexspecs

import com.mycompany.fixture.Calculator
import com.rexspecs.RexSpecs.Companion.runSuite
import com.rexspecs.interop.executeSingleHtmlFile
import com.rexspecs.connectors.DirectConnector
import com.rexspecs.connectors.HttpConnector
import com.rexspecs.connectors.stubbedConnector
import com.rexspecs.inputs.SingleHtmlFileInputReader
import com.rexspecs.inputs.SingleJsonFileInputReader
import com.rexspecs.inputs.sanified
import com.rexspecs.outputs.HtmlFileOutputWriter
import com.rexspecs.utils.RexSpecProperties
import com.rexspecs.utils.RexSpecPropertiesLoader
import org.http4k.core.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

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

val directProps = RexSpecProperties(
    "suites/direct_examples/",
    "com.mycompany.fixture.MyFixtureRegistry",
    "com.rexspecs.connectors.DirectConnector",
    "localhost",
    58008
)

val httpProps = RexSpecProperties(
    "suites/http_examples/",
    "com.mycompany.fixture.MyFixtureRegistry",
    "com.rexspecs.connectors.HttpConnector",
    "localhost",
    2345
)

class RexSpecsTest {
    @Test
    fun `We know when the whole suite fails`() {
        val props = RexSpecPropertiesLoader.properties()

        val executedSuite = runSuite(
            SingleHtmlFileInputReader("Calculator Over HTTP.html", props.rexspecsDirectory),
            HtmlFileOutputWriter(props.rexspecsDirectory),
            mapOf("Calculator" to Calculator()),
            stubbedConnector(mapOf(calcOneSucceeds, calcTwoFails))
        )

        assertFalse(executedSuite.success())
    }

    @Test
    fun `Can write to a target file as output`() {
        runSuite(
            SingleHtmlFileInputReader("Calculator Over HTTP.html", httpProps.rexspecsDirectory),
            HtmlFileOutputWriter(httpProps.rexspecsDirectory),
            mapOf("Calculator" to Calculator()),
            stubbedConnector(mapOf(calcOneSucceeds, calcTwoFails))
        )

        assertEquals(
            sanified("src/test/resources/http_examples/Calculator Over HTTP.html"),
            sanified("suites/http_examples/results/Calculator Over HTTP.html")
        )
    }

    @Test
    fun `Can call a real HTTP server`() {
        runSuite(
            SingleHtmlFileInputReader("Calculator Over HTTP.html", httpProps.rexspecsDirectory),
            HtmlFileOutputWriter(httpProps.rexspecsDirectory),
            mapOf("Calculator" to Calculator()),
            HttpConnector(HttpClient(httpProps.host, httpProps.port).handle)
        )

        assertEquals(
            sanified("src/test/resources/http_examples/Calculator Over HTTP.html"),
            sanified("suites/http_examples/results/Calculator Over HTTP.html")
        )
    }

    @Test
    fun `Can call the target system directly`() {
        runSuite(
            SingleHtmlFileInputReader("Calculator Called Directly.html", directProps.rexspecsDirectory),
            HtmlFileOutputWriter(directProps.rexspecsDirectory),
            mapOf("Calculator" to Calculator()),
            DirectConnector()
        )

        assertEquals(
            sanified("src/test/resources/direct_examples/Calculator Called Directly.html"),
            sanified("suites/direct_examples/results/Calculator Called Directly.html")
        )
    }

    @Test
    fun `Provides a useful error message when it can't find the fixture`() {
        runSuite(
            SingleHtmlFileInputReader("No Such Fixture.html", directProps.rexspecsDirectory),
            HtmlFileOutputWriter(directProps.rexspecsDirectory),
            mapOf("Calculator" to Calculator()),
            DirectConnector()
        )

        assertEquals(
            sanified("src/test/resources/direct_examples/No Such Fixture.html"),
            sanified("suites/direct_examples/results/No Such Fixture.html")
        )
    }

    @Test
    fun `Can run RexSpecs by passing in JSON directly`() {
        runSuite(
            SingleJsonFileInputReader("Json Example.json", directProps.rexspecsDirectory),
            HtmlFileOutputWriter(directProps.rexspecsDirectory),
            mapOf("Calculator" to Calculator()),
            DirectConnector()
        )

        assertEquals(
            sanified("src/test/resources/direct_examples/Json Example.html"),
            // TODO: Make this emit a file with a .html suffix, by stripping suffixes from input files
            sanified("suites/direct_examples/results/Json Example.json")
        )
    }

    @Test
    @Disabled
    fun `Runs entire suites of tests`() {
        runSuite(
            SingleJsonFileInputReader("Json Example.json", directProps.rexspecsDirectory),
            HtmlFileOutputWriter(directProps.rexspecsDirectory),
            mapOf("Calculator" to Calculator()),
            DirectConnector()
        )

        fail("TODO: Implement this")
    }

    @Test
    @Disabled
    fun `Can run RexSpec with CTRL-SHIFT-F10, from a RunConfiguration`() {
        TODO("This")
    }

    @Test
    @Disabled
    fun `We can clean out the target directories, so that we can do the next test run`() {}

    @Test
    @Disabled
    fun `Can run RexSpec as a Gradle test dependency`() {}
}
