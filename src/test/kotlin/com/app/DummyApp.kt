package com.app

import org.http4k.core.*
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.server.SunHttp
import org.http4k.server.asServer

fun main() {
    val app: HttpHandler = { request: Request ->
        when (request.uri.path) {
            "/target" -> unpack(calculate(request.uri.queries()))
            else -> Response(NOT_FOUND)
        }
    }
    app.asServer(SunHttp(2345)).start()
}

// Stop any of the HTTP API gubbins from polluting application code
fun unpack(calculationResult: CalculationResult): Response {
    return Response(if (calculationResult.success) OK else BAD_REQUEST).body(calculationResult.body)
}

data class CalculationResult(val success: Boolean, val body: String)

fun calculate(params: Parameters): CalculationResult {
    val lookup = params.associate { it.first to it.second }
    val operand1 = lookup["First Param"]?.toInt() ?: 0
    val operand2 = lookup["Second Param"]?.toInt() ?: 0
    val operator = lookup["Operator"] ?: "+"

    return when (operator) {
        "+" -> CalculationResult(true, (operand1 + operand2).toString())
        "*" -> CalculationResult(true, (operand1 * operand2).toString())
        else -> CalculationResult(false, "Unsupported operator: \"$operator\"")
    }
}
