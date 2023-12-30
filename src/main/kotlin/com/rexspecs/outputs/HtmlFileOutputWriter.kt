package com.rexspecs.outputs

import com.rexspecs.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import java.io.File

open class HtmlFileOutputWriter(private val testSourceRoot: String) : OutputWriter {
    // TODO: move filePath into ExecutedSpec
    override fun writeSpecResults(executedSpec: ExecutedSpec, filePath: String) {
        writeFile(decorateHtml(executedSpec), filePath)
    }

    fun decorateHtml(executedSpec: ExecutedSpec): String {
        val document = Jsoup.parse(executedSpec.input)

        // Nasty mutating call to appendChildren()
        htmlToTables(document)
            .zip(executedSpec.executedTests)
            .map { (tableElem, result) ->
                tableElem.empty()
                tableElem.appendChildren(toTable(result.test, result.actualRowResults))}

        return document.toString()
    }

    override fun cleanTargetDir() {
        File(testSourceRoot).listFiles()?.forEach {
            val didItWork = it.delete()
            println("Deleted ${it.absolutePath} ==> ${didItWork}")
        }
    }
}

fun convertTableToTest(table: Element): Test {
    val headerRows = table.selectXpath("thead//tr").toList()
    val (first, second) = headerRows
    val fixtureCell = first.selectXpath("th").toList().first()
    val columnHeaders = second.selectXpath("th").toList().map { it.text() }
    val testRows: List<TestRow> = table.selectXpath("tbody//tr")
        .toList()
        .map {
            val (result, params) = it.children()
                .toList()
                .zip(columnHeaders)
                .partition { (_, paramName) -> paramName == "HTTP Response" || paramName == "Result" }
            TestRow(
                params.map { (elem, _) -> elem.text() },
                RowResult(result.first().first.text(), result.last().first.text())
            )
        }

    return Test(fixtureCell.text(), columnHeaders, testRows)
}

fun toTable(test: Test, actualRowResults: List<RowResult>): MutableCollection<out Node> {
    val header = Element("thead")
    header.appendElement("tr").appendElement("th").html(test.fixtureName)
    header.appendElement("tr").appendChildren(test.columnNames.map { Element("th").html(it) })

    val body = Element("tbody")
    val bodyRows: List<Element> = test.testRows
        .zip(actualRowResults)
        .map { (inputRow, resultRow) -> toTableRow(inputRow, resultRow) }

    body.appendChildren(bodyRows)

    return mutableListOf(header, body)
}

fun toTableRow(inputRow: TestRow, resultRow: RowResult): Element {
    val paramsCells = inputRow.inputParams.map { param -> Element("td").html(param) }
    val responseCell = expectedButWas(inputRow.expectedResult.httpResponse, resultRow.httpResponse)
    val resultCell = expectedButWas(inputRow.expectedResult.result, resultRow.result)
    return Element("tr").appendChildren(paramsCells + responseCell + resultCell)
}

fun expectedButWas(expected: String, actual: String): Element =
    if (expected == actual)
        Element("td").html(actual)
    else
        Element("td")
            .attr("style", "color: red")
            .html("Expected [$expected] but was: [$actual]")
