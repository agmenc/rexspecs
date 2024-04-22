package com.rexspecs.specs

import com.rexspecs.TestRow
import com.rexspecs.eithers

val httpCalculationTest = TabularTest(
    "Calculator",
    listOf("First Param", "Operator", "Second Param"),
    listOf("HTTP Response", "Result"),
    listOf(
        TestRow(eithers("7", "+", "8"), eithers("200", "15")),
        TestRow(eithers("7", "x", "8"), eithers("201", "56"))
    )
)

val directCalculationTest = TabularTest(
    "Calculator",
    listOf("First Param", "Operator", "Second Param"),
    listOf("Result"),
    listOf(
        TestRow(eithers("7", "+", "8"), eithers("15")),
        TestRow(eithers("7", "x", "8"), eithers("56"))
    )
)