package com.app

import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.server.SunHttp
import org.http4k.server.asServer

fun main() {
    val app: HttpHandler = { request: Request ->
        when (request.uri.path) {
            "/hello" -> Response(OK).body("Hello World!")
            else -> Response(NOT_FOUND)
        }
    }
    app.asServer(SunHttp(8000)).start()
}
