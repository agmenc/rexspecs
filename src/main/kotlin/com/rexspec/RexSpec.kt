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
    fun execute(): List<ExecutedTest> =
        Jsoup.parse(input).allElements
            .toList()
            .filter { it.tagName() == "table" }
            .map { testify(it) }
            .map { testRep -> ExecutedTest(testRep, executeSingleTableTest(testRep, index)) }

    private fun executeSingleTableTest(rexTestRep: RexTestRep, index: Map<String, (List<String>) -> Request>): List<RexResult> {
        val function: ((List<String>) -> Request) = index[rexTestRep.fixtureName]!!
        return rexTestRep.rexTestRows
            .map{ row -> function(listOf(row.inputParams[0], row.inputParams[1], row.inputParams[2])) }
            .map{ req -> hitTheApi(req) }
            .map{ res -> toRexResults(res) }
    }

    private fun hitTheApi(request: Request): Response {
        return httpHandler(request)
    }

    private fun toRexResults(response: Response): RexResult {
        return RexResult(response.status.code, toByteArray(response.body.payload).toString(UTF_8))
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

data class RexTestRep(val fixtureName: String, val rexTestRows: List<RexTestRow>)
data class RexTestRow(val inputParams: List<String>, val expectedResult: RexResult)
data class RexResult(val httpResponse: Int, val result: String)
data class ExecutedTest(val rexTestRep: RexTestRep, val results: List<RexResult>)
enum class RexStatus { PASSED, FAILED, IGNORED }

fun ExecutedTest.success(): Boolean {
    rexTestRep.rexTestRows
        .map { it.expectedResult }
        .zip(results)
        .forEach { (exp, act) -> if (exp != act) return false }

    return true
}

fun testify(table: Element): RexTestRep {
    val fixtureCell = table.selectXpath("//thead//tr//th").toList().first()
    val hardcodedHeadifiers = listOf("First Param", "Operator", "Second Param", "HTTP Response", "Result")
    val rexTestRows: List<RexTestRow> = table.selectXpath("//tbody//tr")
        .toList()
        .map {
            val (result, params) = it.children()
                .toList()
                .zip(hardcodedHeadifiers)
                .partition { (_, paramName) -> paramName == "HTTP Response" || paramName == "Result" }
            RexTestRow(
                params.map { (elem, _) -> elem.text() },
                RexResult(result.first().first.text().toInt(), result.last().first.text())
            )
        }

    return RexTestRep(fixtureCell.text(), rexTestRows)
}