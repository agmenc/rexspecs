package com.rexspecs

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class RowDescriptorTest {
    @Test
    fun `RowDescriptor knows when all the inputs have been gathered`() {
        val rowDescriptor = RowDescriptor(
            2,
            listOf("Input One", "Input Two"),
            listOf("Output One", "Output Two"),
            emptyMap()
        )

        assertFalse(rowDescriptor.inputsComplete())

        val updatedRowDescriptor = rowDescriptor + ("Input One" to Either.Left("1"))

        assertFalse(updatedRowDescriptor.inputsComplete())

        val updatedRowDescriptor2 = updatedRowDescriptor + ("Input Two" to Either.Left("2"))

        assertTrue(updatedRowDescriptor2.inputsComplete())
    }
}