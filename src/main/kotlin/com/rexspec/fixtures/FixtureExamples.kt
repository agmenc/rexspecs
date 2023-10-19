package com.rexspec.fixtures

import org.http4k.core.Method
import org.http4k.core.Request

// TODO: make params a Map<String, String> so that we have the key as well as the value
fun calculatorRequestBuilder(params: List<String>): Request {
    return Request(Method.GET, "http://someserver.com/target?p0=${params[0]}&p1=${encodePlus(params[1])}&p2=${params[2]}")
}

private fun encodePlus(param: String) = if (param == "+") "%2b" else param