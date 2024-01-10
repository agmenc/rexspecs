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
        val headerRows = table.selectXpath("thead//tr").toList()
        val (first, second) = headerRows
        val fixtureCell = first.selectXpath("th").toList().first()
        val columnHeaders = second.selectXpath("th").toList().map { it.text() }

        val testRows: List<TestRow> = table.selectXpath("tbody//tr")
            .toList()
            .map { tableRow ->

                /* TODO: Have a better way of separating the input params from the expected results cells.
                   Here we zip with the columnHeaders, just so that we can extract by hard-coded column name. */
                val (results: List<Pair<String, String>>, params: List<Pair<String, String>>) =
                    tableRow.children() // table cells
                        .map { elem: Element -> elem.text() }
                        .toList()
                        .zip(columnHeaders)
                        .partition { (_, paramName) -> paramName == "HTTP Response" || paramName == "Result" }

                TestRow(
                    params.map { (elem, _) -> elem },
                    RowResult(results.map { (elem, _) -> elem })
                )
            }

        return TabularTest(fixtureCell.text(), columnHeaders, testRows)
    }
}

class SingleHtmlFileInputReader(private val singleFile: String, rexspecsDirectory: String): HtmlFileInputReader(rexspecsDirectory) {
    override fun specIdentifiers(): List<String> {
        return listOf(singleFile)
    }
}