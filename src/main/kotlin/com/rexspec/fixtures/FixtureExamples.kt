package com.rexspec.fixtures

import org.http4k.core.Method
import org.http4k.core.Request

// TODO: make params a Map<String, String> so that we have the key as well as the value
fun calculatorRequestBuilder(params: List<String>): Request {
    return Request(Method.POST, "localhost")
}