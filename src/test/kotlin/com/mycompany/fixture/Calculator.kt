package com.mycompany.fixture

import com.app.CalculationResult
import com.app.calculate
import com.rexspecs.*
import com.rexspecs.connectors.Connector
import com.rexspecs.connectors.DirectConnector
import com.rexspecs.connectors.HttpConnector
import com.rexspecs.fixture.Fixture
import com.rexspecs.fixture.extract
import com.rexspecs.fixture.missingFieldError
import com.rexspecs.specs.TabularTest
import com.rexspecs.utils.Either
import com.rexspecs.utils.assumeLeft
import com.rexspecs.utils.lefts
import org.http4k.core.*
import org.http4k.urlEncoded
import java.net.URLEncoder
// TODO: Allow Fixture classes to provide a selection of supported Connectors, so that there is less boilerplate
class Calculator: Fixture {
    override fun execute(
        rowDescriptor: RowDescriptor,
        connector: Connector,
        expectedColumnValues: Map<String, Either<String, TabularTest>>
    ): Map<String, Either<String, ExecutedSpecComponent>> {
        val params: List<Pair<String, String>> = lefts(rowDescriptor.resultsForThisRow).map { (k, v) -> Pair(k, v.left) }

        val allResultsSoFar: Map<String, Either<String, ExecutedSpecComponent>> = rowDescriptor.resultsForThisRow

        return when (connector) {
            is HttpConnector -> connectOverHttp(params, connector)
            is DirectConnector -> connectDirectly(allResultsSoFar)
            else -> throw RuntimeException("Unsupported connector: $connector")
        }
    }

    private fun connectDirectly(params: Map<String, Either<String, ExecutedSpecComponent>>): Map<String, Either<String, ExecutedSpecComponent>> {
        // TODO - Check the input types parse correctly when processing the input params, meaning we don't have to check them here
        // TODO - Choose something simpler, so that library users can pull params without boilerplate
        
        // TODO - Option 1
        val operand1 = params.extract("First Param") { it.toInt() } ?: return missingFieldError("Result", "First Param")

        // TODO - Option 2
        val operand2 = assumeLeft(params["Second Param"]).toInt()

        val operator = params.extract("Operator") { it } ?: return missingFieldError("Result", "Operator")

        val calculated: CalculationResult = calculate(operand1, operator, operand2)

        return mapOf(
            "Result" to Either.Left(calculated.value)
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

    private fun calculatorRequestBuilder(newParams: List<Pair<String, String>>): Request {
        // TODO: shouldn't need to provide a URI at this point, that is the HttpConnector's job
        val uri = Uri.of("http://not-actually-a-real-host.com/")
            .path("/target")
            .query(newParams.map { (k, v) -> "${k.urlEncoded()}=${v.urlEncoded()}" }.joinToString("&"))
        return Request(Method.GET, uri, version = HttpMessage.HTTP_1_1)
    }
}

