package com.rexspecs

import com.rexspecs.connectors.stubbedHttpHandler
import com.rexspecs.outputs.HtmlFileOutputWriter
import com.rexspecs.specs.Spec
import com.rexspecs.specs.calculationTest
import com.rexspecs.inputs.sampleInput
import com.rexspecs.specs.Title
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class SpecRunnerTest {
    @Test
    fun `We know that a passing test has passed`() {
        val passingSpec = SpecRunner(
            Spec("some/input.file", listOf(calculationTest)),
            mapOf("Calculator" to ::calculatorRequestBuilder),
            stubbedHttpHandler(mapOf(calcOneSucceeds, calcTwoSucceeds))
        )

        val executedSpec = passingSpec.execute()

        assertTrue(executedSpec.success())
    }

    @Test
    fun `We know that a failing test has failed`() {
        val failingSpec = SpecRunner(
            Spec("some/input.file", listOf(calculationTest)),
            mapOf("Calculator" to ::calculatorRequestBuilder),
            stubbedHttpHandler(mapOf())
        )

        assertFalse(failingSpec.execute().success())
    }

    @Test
    fun `Can use Fixture to build HTTP requests`() {
        val spec = SpecRunner(
            Spec("some/input.file", listOf(Title("An Acceptance Test"), calculationTest)),
            mapOf("Calculator" to ::calculatorRequestBuilder),
            stubbedHttpHandler(mapOf(calcOneSucceeds, calcTwoSucceeds))
        )

        val executedSpec = spec.execute()

        assertEquals(sampleInput, HtmlFileOutputWriter("whatever").generateHtml(executedSpec))
        assertTrue(executedSpec.success())
    }
}