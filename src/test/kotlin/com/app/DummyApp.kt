package com.app

import org.http4k.core.*
import org.http4k.core.Status.Companion.INTERNAL_SERVER_ERROR
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
    app.asServer(SunHttp(8000)).start()
}

fun unpack(calculationResult: CalculationResult): Response {
    return Response(calculationResult.status).body(calculationResult.body)
}

data class CalculationResult(val status: Status, val body: String)

fun calculate(params: Parameters): CalculationResult {
    val lookup = params.map { it.first to it.second }.toMap()
    val operand1 = lookup["First Param"]?.toInt() ?: 0
    val operand2 = lookup["Second Param"]?.toInt() ?: 0
    val operator = lookup["Operator"] ?: "+"

    return when (operator) {
        "+" -> CalculationResult(OK, (operand1 + operand2).toString())
        "*" -> CalculationResult(OK, (operand1 + operand2).toString())
        else -> CalculationResult(INTERNAL_SERVER_ERROR, "Unsupported operator: \"$operator\"")
    }
}
