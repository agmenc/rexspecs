package com.rexspecs.connectors

import org.http4k.core.*

fun stubbedConnector(calls: Map<Request, Response>): HttpConnector = HttpConnector(httpHandler(calls))

fun httpHandler(calls: Map<Request, Response>) = { req: Request ->
    if (!calls.containsKey(req)) {
        println("Unstubbed request: \n${prettify(req)}")
        println("Expected one of: \n${calls.map{ (k,_) -> prettify(k) }.joinToString("\n")}")
    }
    calls.getOrDefault(req, MemoryResponse(Status.EXPECTATION_FAILED, body = MemoryBody("Unstubbed API call")))
}


fun prettify(req: Request): String  = "${req.method} ${req.uri} ${req.uri.path}"