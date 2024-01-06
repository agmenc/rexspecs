package com.rexspecs

import com.mycompany.fixture.Calculator
import com.rexspecs.connectors.DirectConnector
import com.rexspecs.connectors.stubbedConnector
import com.rexspecs.inputs.expectedOutputWithFailure
import com.rexspecs.inputs.expectedOutputWithSuccess
import com.rexspecs.outputs.HtmlFileOutputWriter
import com.rexspecs.specs.Spec
import com.rexspecs.specs.httpCalculationTest
import com.rexspecs.inputs.sanified
import com.rexspecs.specs.Title
import com.rexspecs.specs.directCalculationTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class SpecRunnerTest {
    @Test
    fun `We know that a passing test has passed`() {
        val passingSpec = SpecRunner(
            Spec("some/input.file", listOf(httpCalculationTest)),
            mapOf("Calculator" to Calculator()),
            stubbedConnector(mapOf(calcOneSucceeds, calcTwoSucceeds))
        )

        val executedSpec = passingSpec.execute()

        assertTrue(executedSpec.success())
    }

    @Test
    fun `We know that a failing test has failed`() {
        val failingSpec = SpecRunner(
            Spec("some/input.file", listOf(httpCalculationTest)),
            mapOf("Calculator" to Calculator()),
            stubbedConnector(mapOf())
        )

        assertFalse(failingSpec.execute().success())
    }

    @Test
    fun `Can use Fixture to build HTTP requests`() {
        val spec = SpecRunner(
            Spec("some/input.file", listOf(Title("An Acceptance Test"), httpCalculationTest)),
            mapOf("Calculator" to Calculator()),
            stubbedConnector(mapOf(calcOneSucceeds, calcTwoSucceeds))
        )

        val executedSpec = spec.execute()

        assertEquals(expectedOutputWithSuccess, HtmlFileOutputWriter("whatever").generateHtml(executedSpec))
        assertTrue(executedSpec.success())
    }

    @Test
    fun `Can use Fixture to connect directly to the target code`() {
        val spec = SpecRunner(
            Spec("some/input.file", listOf(Title("An Acceptance Test"), directCalculationTest)),
            mapOf("Calculator" to Calculator()),
            DirectConnector()
        )

        val executedSpec = spec.execute()

        assertEquals(
            sanified("src/test/resources/expectations/DirectlyCalledExample.html"),
            HtmlFileOutputWriter("whatever").generateHtml(executedSpec)
        )
    }
}