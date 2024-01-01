package com.rexspecs.connectors

import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response

interface Connector

// TODO: s/HttpConnector/CustomHttpConnector/
class HttpConnector(val httpHandler: HttpHandler): Connector {
    fun process(request: Request): Response = httpHandler(request)
}

class DirectConnector: Connector