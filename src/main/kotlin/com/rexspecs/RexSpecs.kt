package com.rexspecs

import com.rexspecs.inputs.InputReader
import com.rexspecs.outputs.OutputWriter
import com.rexspecs.outputs.convertTableToTest
import com.rexspecs.outputs.toTable
import com.rexspecs.specs.Spec
import com.rexspecs.specs.SpecComponent
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.nio.ByteBuffer
import kotlin.text.Charsets.UTF_8

typealias FixtureLookup = Map<String, (Map<String, String>) -> Request>

data class ExecutedSuite(val executedSpecs: List<ExecutedSpec>) {
    fun success(): Boolean = executedSpecs.fold(true) { allGood, nextSpec -> allGood && nextSpec.success() }
    fun firstSpec(): ExecutedSpec = executedSpecs.first()
}

// SuiteRunner (built-in): moves through the list of specs identified by the InputReader, and executes them one-by-one
// SuiteRunner (built-in): performs tidy-ups by telling the OutputWriter to do pre-test housekeeping.
// InputReader: knows where to find specs, and how to read them into their JSON representation
// Specs: are identified in a tree structure (regardless of filesystem, DB, or whatever source)
// Specs: emit, or are composed of JSON, processed row by row (e.g. from HTML tables)
// SpecRunner (built-in): Reads JSON from the reader and sends it to the Connector
// SpecRunner (built-in): Receives a JSON result from the Connector
// SpecRunner (built-in): Sends both input and output to the OutputWriter
// HttpHandler: a type of ***Connector***, that accepts JSON, makes an API call, and translates the response back into JSON
// OutputWriter: outputs a decorated version of the input, highlighting expected vs actual results
// FixtureLookup: matches table names to test fixtures.
// Dependencies: InputReader, OutputWriter, FixtureLookup, HttpHandler
fun runSuite(
    inputReader: InputReader,
    outputWriter: OutputWriter,
    fixtureLookup: FixtureLookup,
    httpHandler: HttpHandler
): ExecutedSuite {
    outputWriter.cleanTargetDir()
    return ExecutedSuite(inputReader.speccies().map { SpecRunner(it, fixtureLookup, httpHandler).execute() })
        .also { executedSuite ->
            // TODO: make this part of single-spec execution
            outputWriter.writeSpecResults(executedSuite.firstSpec(), "rexspecs/AnAcceptanceTest.html")
        }
        .also { executedSuite ->
            println("RexSpecs: ${if (executedSuite.success()) "SUCCESS" else "FAILURE"}")
        }
}

class SpecRunner(
    val spec: Spec,
    val index: FixtureLookup,
    val httpHandler: HttpHandler
) {
    fun execute(): ExecutedSpec = ExecutedSpec(
        spec.guts(),
        spec.components()
            .filterIsInstance<Test>()
            .map { test -> ExecutedTest(test, executeTest(test, index)) }
    )

    private fun executeTest(test: Test, index: FixtureLookup): List<RowResult> {
        val function: ((Map<String, String>) -> Request) = index[test.fixtureName]!!

        return test.testRows
            .map { row -> function(zipToMap(test, row)) }
            .map { req -> httpHandler(req) }
            .map { res -> toRexResults(res) }
    }

    private fun zipToMap(test: Test, row: TestRow): Map<String, String> {
        test.columnNames.zip(row.inputParams)

        return mapOf(
            test.columnNames[0] to row.inputParams[0],
            test.columnNames[1] to row.inputParams[1],
            test.columnNames[2] to row.inputParams[2]
        )
    }

    private fun toRexResults(response: Response): RowResult {
        // TODO: Move HTTP gubbins elsewhere
        return RowResult(response.status.code.toString(), toByteArray(response.body.payload).toString(UTF_8))
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
}

fun htmlToTables(inputDocument: Document) = inputDocument.allElements
    .toList()
    .filter { it.tagName() == "table" }

data class Test(val fixtureName: String, val columnNames: List<String>, val testRows: List<TestRow>): SpecComponent

data class TestRow(val inputParams: List<String>, val expectedResult: RowResult)

data class RowResult(val httpResponse: String, val result: String)

// A Spec has a title, some descriptions, and some tests (which have JSON rows)
data class ExecutedSpec(val input: String, val executedTests: List<ExecutedTest>) {
    fun output(): String {
        val document = Jsoup.parse(input)
        htmlToTables(document)
            .zip(executedTests)
            .map { (tableElem, result) ->
                tableElem.empty()
                tableElem.appendChildren(toTable(result.test, result.actualRowResults))}

        return document.toString()
    }

    fun success(): Boolean = executedTests.fold(true) { allGood, nextTable -> allGood && nextTable.success() }
}

data class ExecutedTest(val test: Test, val actualRowResults: List<RowResult>) {
    fun success(): Boolean {
        test.testRows
            .map { it.expectedResult }
            .zip(actualRowResults)
            .forEach { (expected, actual) -> if (expected != actual) return false }

        return true
    }
}