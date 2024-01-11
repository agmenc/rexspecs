package com.rexspecs.inputs

import com.rexspecs.RowResult
import com.rexspecs.TestRow
import com.rexspecs.specs.*
import com.rexspecs.utils.fileAsString
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.io.File
import kotlin.io.path.Path

open class HtmlFileInputReader(rexspecsDirectory: String): InputReader {

    private val specsRoot = File(rexspecsDirectory, "specs")

    protected open fun specIdentifiers(): List<String> {
        return specsRoot.walk().toList()
            .filter { it.isFile }
            .map { it.relativeTo(specsRoot).path }
    }

    override fun specs(): List<Spec> {
        return specIdentifiers().map { filePath ->
            val inputDocument = Jsoup.parse(fileAsString(Path(specsRoot.path, filePath).toString()))
            Spec(filePath, htmlToSpecComponents(inputDocument))
        }
    }

    private fun htmlToSpecComponents(inputDocument: Document): List<SpecComponent> = inputDocument.allElements
        .toList()
        .map { element ->
            when (element.tagName()) {
                "table" -> convertTableToTest(element)
                "title" -> Title(element.text())
                "h1" -> Heading(element.text())
                "p" -> Description(element.text())
                else -> Ignorable()
            }
        }
        .filter { it !is Ignorable }

    // TODO: Privatise
    fun convertTableToTest(table: Element): TabularTest {
        val (firstHeaderRow, secondHeaderRow) = table.selectXpath("thead//tr").toList()
        val fixtureName = firstHeaderRow.selectXpath("th").toList().first()
        val (inputNames: List<String>, outputNames: List<String>) =
            secondHeaderRow.selectXpath("th")
                .toList()
                .partition { it.attr("class") == "input" }.let { (inputs, outputs) ->
                    Pair(inputs.map { it.text() }, outputs.map { it.text() })
                }

        val testRows: List<TestRow> = table.selectXpath("tbody//tr")
            .toList()
            .map { tableRow ->
                val cellValues: List<String> = tableRow.children().map { elem: Element -> elem.text() }
                val (inputs, expectations) = tableRow.children()
                    .map { elem: Element -> elem.text() }
                    .partition { cellValues.indexOf(it) < inputNames.size }

                TestRow(
                    inputs,
                    RowResult(expectations)
                )
            }

        return TabularTest(fixtureName.text(), inputNames, outputNames, testRows)
    }
}

class SingleHtmlFileInputReader(private val singleFile: String, rexspecsDirectory: String): HtmlFileInputReader(rexspecsDirectory) {
    override fun specIdentifiers(): List<String> {
        return listOf(singleFile)
    }

    fun spec(): Spec = specs().first()

    fun firstTest(): TabularTest = spec().components.first { it is TabularTest } as TabularTest
}