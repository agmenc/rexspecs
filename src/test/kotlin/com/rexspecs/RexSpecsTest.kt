package com.rexspecs

import com.rexspecs.connectors.stubbedHttpHandler
import com.rexspecs.inputs.HtmlInputReader
import com.rexspecs.inputs.sanified
import com.rexspecs.outputs.HtmlFileOutputWriter
import com.rexspecs.utils.RexSpecPropertiesLoader
import org.http4k.core.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import kotlin.math.sin

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

class SingleHtmlInputReader(private val singleFile: String): HtmlInputReader("rexspecs") {
    override fun specIdentifiers(): List<String> {
        return listOf(singleFile)
    }
}

class RexSpecsTest {
    @Test
    fun `We know when the whole suite fails`() {
        val props = RexSpecPropertiesLoader.properties()

        val executedSuite = runSuite(
            SingleHtmlInputReader("AnAcceptanceTest.html"),
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
            SingleHtmlInputReader("AnAcceptanceTest.html"),
            HtmlFileOutputWriter(props.targetPath),
            mapOf("Calculator" to ::calculatorRequestBuilder),
            stubbedHttpHandler(mapOf(calcOneSucceeds, calcTwoFails))
        )

        val expectedOutputFile = sanified("src/test/resources/expectations/AnAcceptanceTest.html")
        val actualOutputFile = sanified("rexspecs/results/AnAcceptanceTest.html")
        assertEquals(expectedOutputFile, actualOutputFile)
    }

    @Test
    fun `Can call a real HTTP server`() {
        val props = RexSpecPropertiesLoader.properties()

        runSuite(
            SingleHtmlInputReader("AnAcceptanceTest.html"),
            HtmlFileOutputWriter(props.targetPath),
            mapOf("Calculator" to ::calculatorRequestBuilder),
            HttpClient(props.host, props.port).handle
        )

        assertEquals(
            sanified("src/test/resources/expectations/AnAcceptanceTest.html"),
            sanified("rexspecs/results/AnAcceptanceTest.html")
        )
    }

    @Test
    @Disabled
    fun `Can run RexSpecs by passing in JSON directly`() {}

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
