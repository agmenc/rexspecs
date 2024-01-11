package com.rexspecs.specs

import com.rexspecs.RowResult
import com.rexspecs.TestRow

val httpCalculationTest = TabularTest(
    "Calculator",
    listOf("First Param", "Operator", "Second Param"),
    listOf("HTTP Response", "Result"),
    listOf(
        TestRow(listOf("7", "+", "8"), RowResult("200", "15")),
        TestRow(listOf("7", "x", "8"), RowResult("201", "56"))
    )
)

val directCalculationTest = TabularTest(
    "Calculator",
    listOf("First Param", "Operator", "Second Param"),
    listOf("Result"),
    listOf(
        TestRow(listOf("7", "+", "8"), RowResult("15")),
        TestRow(listOf("7", "x", "8"), RowResult("56"))
    )
)