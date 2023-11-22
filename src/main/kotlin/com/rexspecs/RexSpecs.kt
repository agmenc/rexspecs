package com.rexspecs

import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import java.io.File
import java.nio.ByteBuffer
import kotlin.text.Charsets.UTF_8

typealias FixtureLookup = Map<String, (Map<String, String>) -> Request>

open class IdentifiedSpec(val specContents: String, val specIdentifier: String)

interface SpecDatabase {
    fun specs(): List<IdentifiedSpec>
}

open class FileSpecDatabase : SpecDatabase {
    override fun specs(): List<IdentifiedSpec> {
        TODO("Not yet implemented")
    }
}

data class ExecutedSuite(val executedSpecs: List<ExecutedSpec>) {
    fun success(): Boolean = executedSpecs.fold(true) { allGood, nextSpec -> allGood && nextSpec.success() }

    // TODO is there a way to mark side-effecting code in Kotlin? Arrow FX and IO<T>, etc?
    fun writeSpecResults(filePath: String) {
        executedSpecs
            .map{ it.output() }
            .forEach{ writeFile(it, filePath) }
    }
}

data class RexSpec(val properties: RexSpecProperties, val specProvider: SpecDatabase, val index: FixtureLookup, val httpHandler: HttpHandler) {
    fun execute(): ExecutedSuite = ExecutedSuite(specProvider.specs().map { SpecExecutor(it, index, httpHandler).execute() })

    // TODO: look to bring all IO to the top level
    fun cleanTargetDir() {
        File(properties.targetPath).listFiles().map {
            val didItWork = it.delete()
            println("Deleted ${it.absolutePath} ==> ${didItWork}")
            didItWork
        }
    }
}

class SpecExecutor(
    val identifiedSpec: IdentifiedSpec,
    val index: FixtureLookup,
    val httpHandler: HttpHandler
) {
    fun execute(): ExecutedSpec = ExecutedSpec(
        identifiedSpec.specContents,
        htmlToTables(Jsoup.parse(identifiedSpec.specContents))
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
    val responseCell = expectedButWas(expectedResult.httpResponse, resultRow.httpResponse)
    val resultCell = expectedButWas(expectedResult.result, resultRow.result)
    return Element("tr").appendChildren(paramsCells + responseCell + resultCell)
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

fun ExecutedSpec.success(): Boolean = executedTables.fold(true) { allGood, nextTable -> allGood && nextTable.success() }

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