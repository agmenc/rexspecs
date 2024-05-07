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

fun unpack(calculationResult: CalculationResult): Response {
    return Response(calculationResult.status).body(calculationResult.value)
}

data class CalculationResult(val value: String, val status: Status = OK)

// TODO - Stop any of the HTTP API gubbins from polluting application code (specifically Status)
fun calculate(params: Parameters): CalculationResult {
    val lookup = params.associate { it.first to it.second }
    val operand1 = lookup["First Param"]?.toInt() ?: 0
    val operand2 = lookup["Second Param"]?.toInt() ?: 0
    val operator = lookup["Operator"] ?: "+"

    return when (operator) {
        "+" -> CalculationResult((operand1 + operand2).toString(), OK)
        "*" -> CalculationResult((operand1 * operand2).toString(), OK)
        else -> CalculationResult("Unsupported operator: \"$operator\"", BAD_REQUEST)
    }
}
