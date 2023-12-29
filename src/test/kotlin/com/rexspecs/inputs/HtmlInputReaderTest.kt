package com.rexspecs.inputs

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class HtmlInputReaderTest {
    @Test
    fun `Knows which tests can be found`() {
        val inputReader = HtmlInputReader("rexspecs")

        assertEquals(
            listOf("rexspecs/specs/AcceptanceTestOne.html", "rexspecs/specs/nesting/AcceptanceTestTwo.html"),
            inputReader.rexspecs()
        )
    }

    @Test
    @Disabled
    fun `Barfs when the source root doesn't contain a specs folder or a results folder`() {}
}