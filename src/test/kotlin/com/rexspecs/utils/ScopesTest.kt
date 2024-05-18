package com.rexspecs.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ScopesTest {

    @Test
    fun `Can chain nullable executions together nicely`() {
        val nullKey: String? = null
        val missingKey = "nope"
        val actualKey = "yup"
        val lookup: Map<String, Int> = mapOf(actualKey to 7)

        val chainResult = nullKey.andThen { lookup[it] }
            .orElse { missingKey.andThen { lookup[it] } }
            .orElse { actualKey.andThen { lookup[it] } }

        assertEquals(7, chainResult)
    }
}