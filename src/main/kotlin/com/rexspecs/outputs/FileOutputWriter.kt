package com.rexspecs.outputs

import com.rexspecs.ExecutedSpec
import com.rexspecs.RowResult
import com.rexspecs.Test
import com.rexspecs.writeFile
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import java.io.File

open class FileOutputWriter(private val testSourceRoot: String) : OutputWriter {
    // TODO: move filePath into ExecutedSpec
    override fun writeSpecResults(executedSpec: ExecutedSpec, filePath: String) {
        writeFile(executedSpec.output(), filePath)
    }

    override fun cleanTargetDir() {
        File(testSourceRoot).listFiles()?.forEach {
            val didItWork = it.delete()
            println("Deleted ${it.absolutePath} ==> ${didItWork}")
        }
    }
}

open class HtmlFileOutputWriter(private val testSourceRoot: String) : FileOutputWriter(testSourceRoot) {
    override fun writeSpecResults(executedSpec: ExecutedSpec, filePath: String) {
        writeFile(executedSpec.output(), filePath)
    }
}

fun toTable(test: Test, actualRowResults: List<RowResult>): MutableCollection<out Node> {
    val header = Element("thead")
    header.appendElement("tr").appendElement("th").html(test.fixtureName)
    header.appendElement("tr").appendChildren(test.columnNames.map { Element("th").html(it) })

    val body = Element("tbody")
    val bodyRows: List<Element> = test.testRows
        .zip(actualRowResults)
        .map { (inputRow, resultRow) -> inputRow.toTableRow(resultRow) }

    body.appendChildren(bodyRows)

    return mutableListOf(header, body)
}