package com.rexspecs.outputs

import com.rexspecs.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
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
                tableElem.appendChildren(toTable(result.tabularTest, result.actualRowResults))}

        return document.toString()
    }

    override fun cleanTargetDir() {
        File(testSourceRoot).listFiles()?.forEach {
            val didItWork = it.delete()
            println("Deleted ${it.absolutePath} ==> ${didItWork}")
        }
    }
}

fun toTable(tabularTest: TabularTest, actualRowResults: List<RowResult>): MutableCollection<out Node> {
    val header = Element("thead")
    header.appendElement("tr").appendElement("th").html(tabularTest.fixtureName)
    header.appendElement("tr").appendChildren(tabularTest.columnNames.map { Element("th").html(it) })

    val body = Element("tbody")
    val bodyRows: List<Element> = tabularTest.testRows
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

fun htmlToTables(inputDocument: Document) = inputDocument.allElements
    .toList()
    .filter { it.tagName() == "table" }