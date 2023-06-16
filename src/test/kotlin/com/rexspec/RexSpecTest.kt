package com.rexspec

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals

internal class RexSpecTest {
    @Test
    fun testPoo() {
        assertEquals("poo", RexSpec().poo())
    }
}