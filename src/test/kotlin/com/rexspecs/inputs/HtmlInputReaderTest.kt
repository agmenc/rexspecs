package com.rexspecs.inputs

import com.rexspecs.specs.Spec
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class HtmlInputReaderTest {
    @Test
    fun `Knows which tests can be found`() {
        val inputReader = HtmlInputReader("rexspecs")

        assertEquals(
            listOf("rexspecs/specs/AcceptanceTestOne.html", "rexspecs/specs/nesting/AcceptanceTestTwo.html"),
            inputReader.specIdentifiers()
        )
    }

    @Test
    fun `Can find a Spec by ID`() {
        val inputReader = HtmlInputReader("rexspecs")

        val spec: Spec = inputReader.speccies().first()

        assertEquals("An Acceptance Test", spec.title)
    }

    @Test
    @Disabled
    fun `Can iterate through a Spec`() {
        val inputReader = HtmlInputReader("rexspecs")

        val spec: Spec = inputReader.speccies().first()

        assertTrue(spec.components().hasNext())
    }

    @Test
    @Disabled
    fun `DirectoryManager barfs when the source root doesn't contain a specs folder or a results folder`() {}
}