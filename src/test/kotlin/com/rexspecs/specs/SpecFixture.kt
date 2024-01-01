package com.rexspecs.specs

import com.rexspecs.RowResult
import com.rexspecs.TabularTest
import com.rexspecs.TestRow

val calculationTest = TabularTest(
    "Calculator",
    listOf("First Param", "Operator", "Second Param", "HTTP Response", "Result"),
    listOf(
        TestRow(listOf("7", "+", "8"), RowResult.from("200", "15")),
        TestRow(listOf("7", "x", "8"), RowResult.from("201", "56"))
    )
)