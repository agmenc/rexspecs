package com.rexspecs.inputs

import com.rexspecs.RowResult
import com.rexspecs.TabularTest
import com.rexspecs.TestRow
import com.rexspecs.fileAsString
import com.rexspecs.outputs.htmlToTables
import com.rexspecs.specs.Spec
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.io.File

open class HtmlInputReader(private val testRoot: String): InputReader {
    override fun specIdentifiers(): List<String> {
        return File("$testRoot/specs").walk().toList()
            .filter { it.isFile }
            .map { it.path }
    }

    override fun specs(): List<Spec> {
        return specIdentifiers().map { filePath ->
            val guts = fileAsString(filePath)
            Spec(
                htmlToTables(Jsoup.parse(guts)).map { table -> convertTableToTest(table) })
        }
    }

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

class SingleHtmlInputReader(private val sourcePath: String): HtmlInputReader(sourcePath) {
    override fun specIdentifiers(): List<String> {
        return listOf(sourcePath)
    }
}