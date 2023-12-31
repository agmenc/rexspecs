package com.rexspecs

import com.rexspecs.connectors.stubbedHttpHandler
import com.rexspecs.outputs.HtmlFileOutputWriter
import com.rexspecs.specs.Spec
import com.rexspecs.specs.calculationTest
import com.rexspecs.inputs.sampleInput
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class SpecRunnerTest {
    @Test
    fun `We know that a passing test has passed`() {
        val passingSpec = SpecRunner(
            Spec(listOf(calculationTest)),
            mapOf("Calculator" to ::calculatorRequestBuilder),
            stubbedHttpHandler(mapOf(calcOneSucceeds, calcTwoSucceeds))
        )

        val executedSpec = passingSpec.execute()

        assertTrue(executedSpec.success())
    }

    @Test
    fun `We know that a failing test has failed`() {
        val failingSpec = SpecRunner(
            Spec(listOf(calculationTest)),
            mapOf("Calculator" to ::calculatorRequestBuilder),
            stubbedHttpHandler(mapOf())
        )

        assertFalse(failingSpec.execute().success())
    }

    @Test
    fun `Can use Fixture to build HTTP requests`() {
        val spec = SpecRunner(
            Spec(listOf(calculationTest)),
            mapOf("Calculator" to ::calculatorRequestBuilder),
            stubbedHttpHandler(mapOf(calcOneSucceeds, calcTwoSucceeds))
        )

        val executedSpec = spec.execute()

        assertEquals(sampleInput, HtmlFileOutputWriter("whatever").decorateHtml(executedSpec))
        assertTrue(executedSpec.success())
    }
}