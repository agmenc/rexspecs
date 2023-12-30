package com.rexspecs

import com.rexspecs.inputs.InputReader
import com.rexspecs.outputs.OutputWriter
import com.rexspecs.specs.Spec
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import java.nio.ByteBuffer
import kotlin.text.Charsets.UTF_8

typealias FixtureLookup = Map<String, (Map<String, String>) -> Request>

open class IdentifiedSpec(val specContents: String, val specIdentifier: String)

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
        htmlToTables(Jsoup.parse(spec.guts()))
            .map { convertTablesToTableReps(it) }
            .map { testRep -> ExecutedTable(testRep, executeTable(testRep, index)) }
    )

    private fun executeTable(tableRep: TableRep, index: FixtureLookup): List<RowResult> {
        val function: ((Map<String, String>) -> Request) = index[tableRep.fixtureName]!!

        return tableRep.rowReps
            .map { row -> function(zipToMap(tableRep, row)) }
            .map { req -> httpHandler(req) }
            .map { res -> toRexResults(res) }
    }

    private fun zipToMap(tableRep: TableRep, row: RowRep): Map<String, String> {

        tableRep.columnNames.zip(row.inputParams)

        return mapOf(
            tableRep.columnNames[0] to row.inputParams[0],
            tableRep.columnNames[1] to row.inputParams[1],
            tableRep.columnNames[2] to row.inputParams[2]
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

data class TableRep(val fixtureName: String, val columnNames: List<String>, val rowReps: List<RowRep>)

fun convertTablesToTableReps(table: Element): TableRep {
    val headerRows = table.selectXpath("thead//tr").toList()
    val (first, second) = headerRows
    val fixtureCell = first.selectXpath("th").toList().first()
    val columnHeaders = second.selectXpath("th").toList().map { it.text() }
    val rowReps: List<RowRep> = table.selectXpath("tbody//tr")
        .toList()
        .map {
            val (result, params) = it.children()
                .toList()
                .zip(columnHeaders)
                .partition { (_, paramName) -> paramName == "HTTP Response" || paramName == "Result" }
            RowRep(
                params.map { (elem, _) -> elem.text() },
                RowResult(result.first().first.text(), result.last().first.text())
            )
        }

    return TableRep(fixtureCell.text(), columnHeaders, rowReps)
}

data class RowRep(val inputParams: List<String>, val expectedResult: RowResult) {
    fun toTableRow(resultRow: RowResult): Element {
        val paramsCells = inputParams.map { param -> Element("td").html(param) }
        val responseCell = expectedButWas(expectedResult.httpResponse, resultRow.httpResponse)
        val resultCell = expectedButWas(expectedResult.result, resultRow.result)
        return Element("tr").appendChildren(paramsCells + responseCell + resultCell)
    }
}

fun expectedButWas(expected: String, actual: String): Element =
    if (expected == actual)
        Element("td").html(actual)
    else
        Element("td")
            .attr("style", "color: red")
            .html("Expected [$expected] but was: [$actual]")

data class RowResult(val httpResponse: String, val result: String)

enum class RowStatus { PASSED, FAILED, IGNORED }

// A Spec has a title, some descriptions, and some tests (which have JSON rows)
data class ExecutedSpec(val input: String, val executedTables: List<ExecutedTable>) {
    fun output(): String {
        val document = Jsoup.parse(input)
        htmlToTables(document)
            .zip(executedTables)
            .map { (tableElem, result) ->
                tableElem.empty()
                tableElem.appendChildren(result.toTable())}

        return document.toString()
    }

    fun success(): Boolean = executedTables.fold(true) { allGood, nextTable -> allGood && nextTable.success() }
}



// Take this, but make it work without all the HTML, using a non-judgemental intermediate data structure

data class ExecutedTable(val tableRep: TableRep, val actualRowResults: List<RowResult>) {
    fun toTable(): MutableCollection<out Node> {
        val header = Element("thead")
        header.appendElement("tr").appendElement("th").html(tableRep.fixtureName)
        header.appendElement("tr").appendChildren(tableRep.columnNames.map { Element("th").html(it) })

        val body = Element("tbody")
        val bodyRows: List<Element> = tableRep.rowReps
            .zip(actualRowResults)
            .map { (inputRow, resultRow) -> inputRow.toTableRow(resultRow) }

        body.appendChildren(bodyRows)

        return mutableListOf(header, body)
    }

    fun success(): Boolean {
        tableRep.rowReps
            .map { it.expectedResult }
            .zip(actualRowResults)
            .forEach { (expected, actual) -> if (expected != actual) return false }

        return true
    }
}