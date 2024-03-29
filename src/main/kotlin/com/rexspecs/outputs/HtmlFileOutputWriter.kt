package com.rexspecs.outputs

import com.rexspecs.ExecutedSpec
import com.rexspecs.InvalidStartingState
import com.rexspecs.RowResult
import com.rexspecs.TestRow
import com.rexspecs.specs.*
import com.rexspecs.utils.errored
import com.rexspecs.utils.writeFile
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.createDirectory

open class HtmlFileOutputWriter(private val rexspecsDirectory: String) : OutputWriter {
    override fun writeSpecResults(executedSpec: ExecutedSpec) {
        writeFile(generateHtml(executedSpec), Path(rexspecsDirectory, "results", executedSpec.identifier))
    }

    // TODO: Privatise
    fun generateHtml(executedSpec: ExecutedSpec): String {

        val snippet = """
            <!doctype html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <link rel="stylesheet" href="../theme.css">
                <script type="application/javascript" src="../toggle.js"></script>
            </head>
            <body>
            </body>
        """.trimIndent()

        val simplerDocument = Jsoup.parse(snippet)
        executedSpec.executedTests.forEach { test ->
            when (test.specComponent) {
                is Title -> simplerDocument.head().appendElement("title").html(test.specComponent.title)
                is TabularTest -> simplerDocument.body().appendChild(toTable(test.specComponent, test.actualRowResults))
                is Heading -> simplerDocument.body().appendElement("h1").html(test.specComponent.words).addClass("title")
                is Description -> simplerDocument.body().appendElement("p").html(test.specComponent.words)
                is Ignorable -> Unit
            }
        }

        return simplerDocument.toString()
    }

    override fun prepareForOutput() {
        val resultsDir = File(rexspecsDirectory, "results")
        when {
            !resultsDir.parentFile.exists() -> throw InvalidStartingState("Cannot find Rexspecs directory [$rexspecsDirectory]")
            !resultsDir.exists() -> Path(rexspecsDirectory, "results").createDirectory()
        }

        File(rexspecsDirectory, "results").walk()
            .filter { it.isFile }
            .forEach {
                if (!it.delete()) errored("Failed to delete File [${it.absolutePath}]" )
            }
    }

    private fun toTable(tabularTest: TabularTest, actualRowResults: List<RowResult>): Node {
        val table = Element("table")
        val header = Element("thead")
        header.appendElement("tr").appendElement("th").html(tabularTest.fixtureName)
        header.appendElement("tr").appendChildren(
            tabularTest.inputColumns.map { Element("th").html(it).addClass("input") } +
            tabularTest.expectationColumns.map { Element("th").html(it) }
        )

        val body = Element("tbody")
        val bodyRows: List<Element> = tabularTest.testRows
            .zip(actualRowResults)
            .map { (inputRow, resultRow) -> toTableRow(inputRow, resultRow) }

        body.appendChildren(bodyRows)

        return table.appendChild(header).appendChild(body)
    }

    private fun toTableRow(inputRow: TestRow, resultRow: RowResult): Element {
        if (inputRow.inputParams.isEmpty()) {
            return Element("tr").appendChildren(listOf(wideError("No input elements are defined for this table. Add class=\"input\" to relevant table columns.")))
        }

        if (inputRow.expectationCount() != resultRow.cells()) {
            return Element("tr").appendChildren(listOf(wideError("Number of expected results [${inputRow.expectationCount()}] does not match the number of actual results [${resultRow.cells()}]")))
        }

        val results = inputRow.expectedResult.resultValues
            .zip(resultRow.resultValues)
            .map { (expected, actual) -> expectedButWas(expected, actual) }

        return Element("tr").appendChildren(inputRow.inputParams.map { param -> Element("td").html(param) } + results)
    }

    private fun expectedButWas(expected: String, actual: String): Element =
        if (expected == actual)
            success(actual)
        else
            failure(expected, actual)

    private fun failure(expected: String, actual: String): Element =
        cell("Expected [$expected] but was: [$actual]", "fail")

    private fun success(actual: String): Element = cell(actual, "success")

    private fun error(description: String): Element = cell("ERROR: $description", "error")

    private fun wideError(description: String): Element = error(description).attr("colspan", "100")

    private fun cell(text: String, className: String): Element =
        Element("td")
            .addClass(className)
            .html(text)
}
