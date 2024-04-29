package com.rexspecs.inputs

import com.rexspecs.*
import com.rexspecs.specs.*
import com.rexspecs.utils.fileAsString
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.io.File
import kotlin.io.path.Path

open class HtmlFileInputReader(rexspecsDirectory: String): InputReader {

    private val specsRoot = File(rexspecsDirectory, "specs")

    override fun prepareForInput() {
        when {
            !specsRoot.parentFile.exists() -> throw InvalidStartingState("Cannot find Rexspecs directory [${specsRoot.parentFile}]")
            !specsRoot.exists() -> throw InvalidStartingState("Cannot find Rexspecs source directory [$specsRoot]")
        }
    }

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
                // TODO - Need to skip past the whole table, so that nested tables don't get processed twice
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

        val testRows: List<TestRow> = table.selectXpath("tbody/tr")
            .toList()
            .map { tableRow ->
                val inputsAndExpectations = tableRow.children()
                    .map { elem ->
                        when (elem.children().size) {
                            0 -> Either.Left(elem.text())
                            1 -> Either.Right(
                                convertTableToTest(
                                    elem.children().first()
                                        ?: throw InvalidStructure("Expected first child element to be table, but was <${elem.tagName()}>.")
                                )
                            )

                            else -> throw InvalidStructure("Too many child elements inside <${elem.tagName()}>. Expected either String or nested <table>.")
                        }
                    }

                val (inputs, expectations) = split(inputsAndExpectations, inputNames.size)

                TestRow(
                    inputs,
                    expectations
                )
            }

        return TabularTest(fixtureName.text(), inputNames, outputNames, testRows)
    }
}

fun <T> split(list: List<T>, splitAfter: Int): Pair<List<T>, List<T>> {
    return Pair(list.take(splitAfter), list.drop(splitAfter))
}

class SingleHtmlFileInputReader(private val singleFile: String, rexspecsDirectory: String): HtmlFileInputReader(rexspecsDirectory) {
    override fun specIdentifiers(): List<String> {
        return listOf(singleFile)
    }

    fun spec(): Spec = specs().first()

    fun firstTest(): TabularTest = spec().components.first { it is TabularTest } as TabularTest
}