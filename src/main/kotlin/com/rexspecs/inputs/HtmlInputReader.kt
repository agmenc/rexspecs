package com.rexspecs.inputs

import com.rexspecs.RowResult
import com.rexspecs.TabularTest
import com.rexspecs.TestRow
import com.rexspecs.fileAsString
import com.rexspecs.specs.Ignorable
import com.rexspecs.specs.Spec
import com.rexspecs.specs.SpecComponent
import com.rexspecs.specs.Title
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.io.File
import kotlin.io.path.Path

open class HtmlInputReader(rexspecsDirectory: String): InputReader {
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

        return TabularTest(fixtureCell.text(), columnHeaders, testRows)
    }
}

