package com.rexspecs

import org.http4k.client.ApacheClient
import org.http4k.core.HttpHandler
import org.http4k.core.Request

//TODO use the root and port.
open class HttpClient(val hostUrl: String, val hostPort: Int) {
    val client = ApacheClient()

    //TODO The calling classes should not need to know about HTTP gubbins such as Request and Response.
    val handle: HttpHandler = { originalRequest: Request ->
        val updatedRequest = originalRequest.uri(originalRequest.uri.host(hostUrl).port(hostPort))
        val response = client(updatedRequest)
        response
    }
}
