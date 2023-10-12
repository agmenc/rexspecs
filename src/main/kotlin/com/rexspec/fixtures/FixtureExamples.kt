package com.rexspec.fixtures

import com.rexspec.RexResult

// TODO: make params a Map<String, String> so that we have the key as well as the value
fun calculatorFixture(params: List<String>): RexResult {
    // Map to HTTP request

    return RexResult(200, "Monkeys")
}