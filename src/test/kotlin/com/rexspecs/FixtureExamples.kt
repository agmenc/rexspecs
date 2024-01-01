package com.rexspecs

import com.app.calculate
import com.rexspecs.connectors.Connector
import com.rexspecs.connectors.DirectConnector
import com.rexspecs.connectors.HttpConnector
import com.rexspecs.fixture.Fixture
import org.http4k.core.*
import java.net.URLEncoder
import java.nio.ByteBuffer

// TODO: Allow Fixture classes to provide a selection of supported Connectors, so that there is less boilerplate
class Calculator: Fixture {
    override fun processRow(inputs: Map<String, String>, connector: Connector): RowResult {
        when (connector) {
            is HttpConnector -> return connectOverHttp(inputs, connector)
            is DirectConnector -> return connectDirectly(inputs)
            else -> throw RuntimeException("Unsupported connector: $connector")
        }
    }

    // TODO: We really don't need the HTTP status when we aren't using HTTP
    private fun connectDirectly(inputs: Map<String, String>): RowResult =
        calculate(inputs.map { (k, v) -> Pair(k, v) }).let { result -> RowResult(result.body) }

    private fun connectOverHttp(inputs: Map<String, String>, httpConnector: HttpConnector): RowResult {
        val request = calculatorRequestBuilder(inputs)
        val response = httpConnector.process(request)
        return RowResult(response.status.code.toString(), toByteArray(response.body.payload).toString(Charsets.UTF_8))
    }

    private fun calculatorRequestBuilder(params: Map<String, String>): Request {
        // TODO: shouldn't need to provide a URI at this point, that is the HttpConnector's job
        val uri = Uri.of("http://not-actually-a-real-host.com/")
            .path("/target")
            .query(params.map { (k, v) -> "${encodePlus(k)}=${encodePlus(v)}" }.joinToString("&"))
        return Request(Method.GET, uri, version = HttpMessage.HTTP_1_1)
    }

    // TODO Should this be done for free by RexSpec?
    private fun encodePlus(param: String) = URLEncoder.encode(param, "UTF-8")

    // Horrible mutating Java. Note that:
    //  - get() actually does a set() on the parameter
    //  - rewind() is necessary if we are re-using the response
    private fun toByteArray(byteBuf: ByteBuffer): ByteArray {
        val byteArray = ByteArray(byteBuf.capacity())
        byteBuf.get(byteArray)
        byteBuf.rewind()
        return byteArray
    }
}
