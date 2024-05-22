package com.rexspecs.outputs

import com.rexspecs.*
import com.rexspecs.specs.*
import com.rexspecs.utils.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
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
                is TabularTest -> simplerDocument.body().appendChild(toTable(test.specComponent, test.resultsForAllRows))
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

    private fun toTable(tabularTest: TabularTest, actualRowResults: List<Map<String, Either<String, ExecutedSpecComponent>>>): Element {
        val table = Element("table")
        val header = Element("thead")
        tabularTest.fixtureName?.let { header.appendElement("tr").appendElement("th").html(it) }
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

    private fun toTableRow(inputRow: TestRow, resultRow: Map<String, Either<String, ExecutedSpecComponent>>): Element {
        if (inputRow.inputCount == 0) {
            return Element("tr").appendChildren(listOf(wideError("No input elements are defined for this table. Add class=\"input\" to relevant table columns.")))
        }

        val comparedCells = inputRow.allTheParams.map { (colName, inputCell) ->
            resultRow[colName]
                ?. let {compare(inputCell, it)}
                ?: error("Expected column [$colName] not found in actual results")
        }

        return Element("tr").appendChildren(comparedCells)
    }

    private fun compare(expected: Either<String, TabularTest>, actual: Either<String, ExecutedSpecComponent>): Element =
        when (expected) {
            is Either.Left -> compareStrings(expected.left, Either.Left(assumeLeft(actual)))
            is Either.Right -> Element("td").appendChild(toTable(expected.right, assumeRight(actual).resultsForAllRows))
        }

    private fun compareStrings(expected: String, actual: Either<String, TabularTest>): Element {
        return when (actual) {
            is Either.Left -> didTheyMatch(expected, actual.left)
            is Either.Right -> error("Wasn't expecting a nested table here")
        }
    }

    private fun didTheyMatch(expected: String, actual: String) =
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
