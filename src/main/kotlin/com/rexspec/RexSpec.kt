package com.rexspec

import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.nio.ByteBuffer
import kotlin.text.Charsets.UTF_8

data class RexSpec(
    val input: String,
    val index: Map<String, (List<String>) -> Request>,
    val httpHandler: HttpHandler
) {
    fun execute(): ExecutedSpec = ExecutedSpec(
        Jsoup.parse(input).allElements
            .toList()
            .filter { it.tagName() == "table" }
            .map { convertTablesToTestReps(it) }
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
        return RowResult(response.status.code, toByteArray(response.body.payload).toString(UTF_8))
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

data class TableRep(val fixtureName: String, val rowReps: List<RowRep>)
data class RowRep(val inputParams: List<String>, val expectedResult: RowResult)
data class RowResult(val httpResponse: Int, val result: String)

enum class RowStatus { PASSED, FAILED, IGNORED }

data class ExecutedSpec(val tables: List<ExecutedTable>)

fun ExecutedSpec.success(): Boolean = tables.fold(true) { allGood, nextTest -> allGood && nextTest.success() }

data class ExecutedTable(val tableRep: TableRep, val rowResults: List<RowResult>)

fun ExecutedTable.success(): Boolean {
    tableRep.rowReps
        .map { it.expectedResult }
        .zip(rowResults)
        .forEach { (exp, act) -> if (exp != act) return false }

    return true
}

fun convertTablesToTestReps(table: Element): TableRep {
    val fixtureCell = table.selectXpath("//thead//tr//th").toList().first()
    val hardcodedHeadifiers = listOf("First Param", "Operator", "Second Param", "HTTP Response", "Result")
    val rowReps: List<RowRep> = table.selectXpath("//tbody//tr")
        .toList()
        .map {
            val (result, params) = it.children()
                .toList()
                .zip(hardcodedHeadifiers)
                .partition { (_, paramName) -> paramName == "HTTP Response" || paramName == "Result" }
            RowRep(
                params.map { (elem, _) -> elem.text() },
                RowResult(result.first().first.text().toInt(), result.last().first.text())
            )
        }

    return TableRep(fixtureCell.text(), rowReps)
}