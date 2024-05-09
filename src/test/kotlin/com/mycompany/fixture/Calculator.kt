package com.mycompany.fixture

import com.app.CalculationResult
import com.app.calculate
import com.rexspecs.*
import com.rexspecs.connectors.Connector
import com.rexspecs.connectors.DirectConnector
import com.rexspecs.connectors.HttpConnector
import com.rexspecs.fixture.Fixture
import com.rexspecs.specs.TabularTest
import org.http4k.core.*
import java.net.URLEncoder
// TODO: Allow Fixture classes to provide a selection of supported Connectors, so that there is less boilerplate
class Calculator: Fixture {
    // TODO - Type this. No more Any. Should return a CalculationResult
    override fun execute(
        rowDescriptor: RowDescriptor,
        connector: Connector,
        columnValues: Map<String, Either<String, TabularTest>>
    ): Map<String, Either<String, ExecutedSpecComponent>> {
        val params: List<Pair<String, String>> = lefts(rowDescriptor.inputResults).map { (k, v) -> Pair(k, v.left) }

        return when (connector) {
            is HttpConnector -> connectOverHttp(params, connector)
            is DirectConnector -> connectDirectly(params)
            else -> throw RuntimeException("Unsupported connector: $connector")
        }
    }

    private fun connectDirectly(params: List<Pair<String, String>>): Map<String, Either<String, ExecutedSpecComponent>> {
        val calculate: CalculationResult = calculate(params)

        return mapOf(
            "HTTP Response" to Either.Left("200"),
            "Result" to Either.Left(calculate.value)
        )
    }

    private fun connectOverHttp(params: List<Pair<String, String>>, httpConnector: HttpConnector): Map<String, Either<String, ExecutedSpecComponent>> {
        val request = calculatorRequestBuilder(params)

        val response: Response = httpConnector.handler(request)

        return mapOf(
            "HTTP Response" to Either.Left(response.status.code.toString()),
            "Result" to Either.Left(response.bodyString())
        )
    }

    override fun processResult(
        columnName: String,
        value: Either<String, TabularTest>,
        connector: Connector,
        nestingCallback: (TabularTest) -> ExecutedSpecComponent,
        rowDescriptor: RowDescriptor
    ): Either<String, ExecutedSpecComponent> {

        // TODO - Specs are the world of Strings. We don't need CalculationResult

        val result = rowDescriptor.executionResult as CalculationResult

        return when (columnName) {
            "HTTP Response" -> Either.Left(result.status.code.toString())
            "Result" -> Either.Left(result.value)
            else -> Either.Left("Unknown column name: $columnName")
        }
    }

    private fun calculatorRequestBuilder(newParams: List<Pair<String, String>>): Request {
        // TODO: shouldn't need to provide a URI at this point, that is the HttpConnector's job
        val uri = Uri.of("http://not-actually-a-real-host.com/")
            .path("/target")
            .query(newParams.map { (k, v) -> "${encodePlus(k)}=${encodePlus(v)}" }.joinToString("&"))
        return Request(Method.GET, uri, version = HttpMessage.HTTP_1_1)
    }

    // TODO Should this be done for free by RexSpec?
    private fun encodePlus(param: String) = URLEncoder.encode(param, "UTF-8")
}

