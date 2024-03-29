package com.mycompany.fixture

import com.app.calculate
import com.rexspecs.RowResult
import com.rexspecs.connectors.Connector
import com.rexspecs.connectors.DirectConnector
import com.rexspecs.connectors.HttpConnector
import com.rexspecs.fixture.Fixture
import org.http4k.asString
import org.http4k.core.HttpMessage
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Uri
import java.net.URLEncoder

// TODO: Allow Fixture classes to provide a selection of supported Connectors, so that there is less boilerplate
class Calculator: Fixture {
    override fun processRow(inputs: Map<String, String>, connector: Connector): RowResult =
        when (connector) {
            is HttpConnector -> connectOverHttp(inputs, connector)
            is DirectConnector -> connectDirectly(inputs)
            else -> throw RuntimeException("Unsupported connector: $connector")
        }

    private fun connectDirectly(inputs: Map<String, String>): RowResult =
        RowResult(calculate(inputs.map { (k, v) -> Pair(k, v) }).value)

    private fun connectOverHttp(inputs: Map<String, String>, httpConnector: HttpConnector): RowResult {
        val request = calculatorRequestBuilder(inputs)
        val response = httpConnector.handler(request)
        return RowResult(
            response.status.code.toString(),
            response.body.payload.asString()
        )
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
}
