package com.rexspecs.specs

import com.rexspecs.TestRow
import com.rexspecs.eithers

val httpCalculationTest = TabularTest(
    "Calculator",
    listOf("First Param", "Operator", "Second Param"),
    listOf("HTTP Response", "Result"),
    listOf(
        TestRow(
            3,
            "First Param" to "7",
            "Operator" to "+",
            "Second Param" to "8",
            "HTTP Response" to "200",
            "Result" to "15"
        ),
        TestRow(
            3,
            "First Param" to "7",
            "Operator" to "x",
            "Second Param" to "8",
            "HTTP Response" to "201",
            "Result" to "56"
        )
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