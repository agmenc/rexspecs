package com.rexspecs.connectors

import com.rexspecs.utils.RexSpecProperties
import com.rexspecs.utils.RexSpecPropertiesLoader
import org.http4k.client.ApacheClient
import org.http4k.core.HttpHandler
import org.http4k.core.Request

interface Connector

class DirectConnector: Connector

// TODO: Eliminate the dependency on Http4k, so that any Client libary can be used.
open class HttpConnector: Connector {
    private val props: RexSpecProperties = RexSpecPropertiesLoader.properties()
    open val handler = HttpClient(props.host, props.port).handle
}

//TODO: Actually use the root and port.
open class HttpClient(val hostUrl: String, val hostPort: Int) {
    val client = ApacheClient()

    val handle: HttpHandler = { originalRequest: Request ->
        val updatedRequest = originalRequest.uri(originalRequest.uri.host(hostUrl).port(hostPort))
        val response = client(updatedRequest)
        response
    }
}