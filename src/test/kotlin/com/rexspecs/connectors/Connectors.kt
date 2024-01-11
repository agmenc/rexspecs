package com.rexspecs.connectors

import org.http4k.core.*

class StubbedHttpConnector(calls: Map<Request, Response>) : HttpConnector() {
    override val handler: HttpHandler = { req: Request ->
        if (!calls.containsKey(req)) {
            println("Unstubbed request: \n${prettify(req)}")
            println("Expected one of: \n${calls.map { (k, _) -> prettify(k) }.joinToString("\n")}")
        }
        calls.getOrDefault(req, MemoryResponse(Status.EXPECTATION_FAILED, body = MemoryBody("Unstubbed API call")))
    }

    private fun prettify(req: Request): String  = "${req.method} ${req.uri} ${req.uri.path}"
}



