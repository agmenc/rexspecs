package com.rexspecs

import org.http4k.client.ApacheClient
import org.http4k.core.HttpHandler
import org.http4k.core.Request

//TODO use the root and port.
open class HttpClient(val root: String, val port: Int) {
    val client = ApacheClient()

    //TODO The calling classes should not need to know about HTTP gubbins
    val handle: HttpHandler = { request: Request ->
        println("request = ${request.method} ${request.uri}")
        val response = client(request)
        println("response = ${response.status.code} ${response.bodyString()}")
        response
    }
}
