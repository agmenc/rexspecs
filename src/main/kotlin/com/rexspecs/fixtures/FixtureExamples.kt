package com.rexspecs.fixtures

import org.http4k.core.HttpMessage
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Uri
import java.net.URLEncoder

// TODO: Move to test source root
fun calculatorRequestBuilder(params: Map<String, String>): Request {
    val uri = Uri.of("http://not-actually-a-real-host.com/")
        .path("/target")
        .query(params.map { (k, v) -> "${encodePlus(k)}=${encodePlus(v)}" }.joinToString("&"))
    return Request(Method.GET, uri, version = HttpMessage.HTTP_1_1)
}

// TODO Should this be done for free by RexSpec?
private fun encodePlus(param: String) = URLEncoder.encode(param, "UTF-8")