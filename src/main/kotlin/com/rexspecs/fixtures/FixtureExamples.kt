package com.rexspecs.fixtures

import org.http4k.core.Method
import org.http4k.core.Request
import java.net.URLEncoder

// TODO: Move to test source root
fun calculatorRequestBuilder(params: Map<String, String>): Request {
    return Request(Method.GET, "http://localhost:8000/target?${params.map { (k, v) -> "${encodePlus(k)}=${encodePlus(v)}" }.joinToString("&")}")
}

// TODO Should this be done for free by RexSpec?
private fun encodePlus(param: String) = URLEncoder.encode(param, "UTF-8")