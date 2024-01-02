package com.rexspecs.specs

import com.rexspecs.RowResult
import com.rexspecs.TabularTest
import com.rexspecs.TestRow

val httpCalculationTest = TabularTest(
    "Calculator",
    listOf("First Param", "Operator", "Second Param", "HTTP Response", "Result"),
    listOf(
        TestRow(listOf("7", "+", "8"), RowResult.from("200", "15")),
        TestRow(listOf("7", "x", "8"), RowResult.from("201", "56"))
    )
)

val directCalculationTest = TabularTest(
    "Calculator",
    listOf("First Param", "Operator", "Second Param", "Result"),
    listOf(
        TestRow(listOf("7", "+", "8"), RowResult.from("15")),
        TestRow(listOf("7", "x", "8"), RowResult.from("56"))
    )
)