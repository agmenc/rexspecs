package com.rexspec.fixtures

import org.http4k.core.Method
import org.http4k.core.Request
import java.net.URLEncoder

fun calculatorRequestBuilder(params: Map<String, String>): Request {
    return Request(Method.GET, "http://someserver.com/target?${params.map { (k, v) -> "${encodePlus(k)}=${encodePlus(v)}" }.joinToString("&")}")
}

// TODO Should this be done for free by RexSpec?
private fun encodePlus(param: String) = URLEncoder.encode(param, "UTF-8")