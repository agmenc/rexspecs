package com.rexspecs

import com.rexspecs.fixture.Fixture
import org.http4k.core.*
import java.net.URLEncoder
import java.nio.ByteBuffer

fun calculatorRequestBuilder(params: Map<String, String>): Request {
    val uri = Uri.of("http://not-actually-a-real-host.com/")
        .path("/target")
        .query(params.map { (k, v) -> "${encodePlus(k)}=${encodePlus(v)}" }.joinToString("&"))
    return Request(Method.GET, uri, version = HttpMessage.HTTP_1_1)
}

// TODO Should this be done for free by RexSpec?
private fun encodePlus(param: String) = URLEncoder.encode(param, "UTF-8")

class Calculator: Fixture {
    override fun processRow(inputs: Map<String, String>, connector: HttpHandler): RowResult {
        val request = calculatorRequestBuilder(inputs)
        val response = connector(request)
        return RowResult(response.status.code.toString(), toByteArray(response.body.payload).toString(Charsets.UTF_8))
    }
}

// Horrible mutating Java. Note that:
//  - get() actually does a set() on the parameter
//  - rewind() is necessary if we are re-using the response
private fun toByteArray(byteBuf: ByteBuffer): ByteArray {
    val byteArray = ByteArray(byteBuf.capacity())
    byteBuf.get(byteArray)
    byteBuf.rewind()
    return byteArray
}