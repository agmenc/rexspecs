package com.rexspec

import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import java.nio.ByteBuffer
import kotlin.text.Charsets.UTF_8

data class RexSpec(
    val input: String,
    val index: Map<String, (List<String>) -> Request>,
    val httpHandler: HttpHandler
) {
    fun execute(): ExecutedSpec = ExecutedSpec(
        input,
        htmlToTables(Jsoup.parse(input))
            .map { convertTablesToTableReps(it) }
            .map { testRep -> ExecutedTable(testRep, executeTable(testRep, index)) }
    )

    private fun executeTable(tableRep: TableRep, index: Map<String, (List<String>) -> Request>): List<RowResult> {
        val function: ((List<String>) -> Request) = index[tableRep.fixtureName]!!
        return tableRep.rowReps
            .map { row -> function(listOf(row.inputParams[0], row.inputParams[1], row.inputParams[2])) }
            .map { req -> hitTheApi(req) }
            .map { res -> toRexResults(res) }
    }

    private fun hitTheApi(request: Request): Response {
        return httpHandler(request)
    }

    private fun toRexResults(response: Response): RowResult {
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

data class RowRep(val inputParams: List<String>, val expectedResult: RowResult)

fun RowRep.toTableRow(resultRow: RowResult): Element {
    val paramsCells = inputParams.map { param -> Element("td").html(param) }
    val responseCell = Element("td").html(expectedButWas(expectedResult.httpResponse, resultRow.httpResponse))
    val resultCell = Element("td").html(expectedButWas(expectedResult.result, resultRow.result))
    return Element("tr").appendChildren(paramsCells + responseCell + resultCell)
}

fun expectedButWas(expected: String, actual: String): String =
    if (expected == actual) actual else "Expected [$expected] but was: [$actual]"

data class RowResult(val httpResponse: String, val result: String)

enum class RowStatus { PASSED, FAILED, IGNORED }

data class ExecutedSpec(val input: String, val executedTables: List<ExecutedTable>)

fun ExecutedSpec.output(): String {
    val document = Jsoup.parse(input)
    htmlToTables(document)
        .zip(executedTables)
        .map { (tableElem, result) ->
            tableElem.empty()
            tableElem.appendChildren(result.toTable())}

    return document.toString()
}

fun ExecutedSpec.success(): Boolean = executedTables.fold(true) { allGood, nextTest -> allGood && nextTest.success() }

data class ExecutedTable(val tableRep: TableRep, val actualRowResults: List<RowResult>)

fun ExecutedTable.toTable(): MutableCollection<out Node> {
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

fun ExecutedTable.success(): Boolean {
    tableRep.rowReps
        .map { it.expectedResult }
        .zip(actualRowResults)
        .forEach { (expected, actual) -> if (expected != actual) return false }

    return true
}