package com.mycompany.fixture

import com.app.calculate
import com.rexspecs.Either
import com.rexspecs.Either.Left
import com.rexspecs.ExecutedSpecComponent
import com.rexspecs.connectors.Connector
import com.rexspecs.connectors.DirectConnector
import com.rexspecs.connectors.HttpConnector
import com.rexspecs.fixture.Fixture
import com.rexspecs.specs.TabularTest
import org.http4k.asString
import org.http4k.core.HttpMessage
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Uri
import java.net.URLEncoder

// TODO: Allow Fixture classes to provide a selection of supported Connectors, so that there is less boilerplate
class Calculator: Fixture {
    override fun processRow(
        inputsAndExpectedResults: Map<String, Either<String, TabularTest>>,
        connector: Connector,
        nestingCallback: (TabularTest) -> ExecutedSpecComponent
    ): List<Either<String, ExecutedSpecComponent>> =
        when (connector) {
            is HttpConnector -> connectOverHttp(inputsAndExpectedResults, connector)
            is DirectConnector -> connectDirectly(inputsAndExpectedResults)
            else -> throw RuntimeException("Unsupported connector: $connector")
        }

    private fun connectDirectly(inputs: Map<String, Either<String, TabularTest>>): List<Either<String, ExecutedSpecComponent>> {
        return listOf(Left(calculate(lefts(inputs).map { (k, v) -> Pair(k, v.left) }).value))
    }

    // TODO - Make this typesafe and not awful
    private fun lefts(inputs: Map<String, Either<String, TabularTest>>): Map<String, Left<String>> {
        return inputs.filter { (_, v) -> v is Left<String> } as Map<String, Left<String>>
    }

    private fun connectOverHttp(inputs: Map<String, Either<String, TabularTest>>, httpConnector: HttpConnector): List<Either<String, ExecutedSpecComponent>> {
        val request = calculatorRequestBuilder(inputs
            .filter { (k, _) ->
                listOf("First Param", "Operator", "Second Param").contains(k)
            })
        val response = httpConnector.handler(request)
        return listOf(
            Left(response.status.code.toString()),
            Left(response.body.payload.asString())
        )
    }

    private fun calculatorRequestBuilder(params: Map<String, Either<String, TabularTest>>): Request {
        // TODO: shouldn't need to provide a URI at this point, that is the HttpConnector's job
        val uri = Uri.of("http://not-actually-a-real-host.com/")
            .path("/target")
            .query(lefts(params).map { (k, v) -> "${encodePlus(k)}=${encodePlus(v.left)}" }.joinToString("&"))
        return Request(Method.GET, uri, version = HttpMessage.HTTP_1_1)
    }

    // TODO Should this be done for free by RexSpec?
    private fun encodePlus(param: String) = URLEncoder.encode(param, "UTF-8")
}
