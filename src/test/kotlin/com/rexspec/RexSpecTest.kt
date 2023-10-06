package com.rexspec

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class RexSpecTest {

    @Test
    fun `Can decorate a document by colouring in some cells`() {
        val input = """
            |<html>
            | <head></head>
            | <body>
            |  <p>An <a href="http://example.com/"><b>example</b></a></p>
            |  <table>
            |   <tbody>
            |    <tr>
            |     <td style="font-family: verdana;">Monkeys</td>
            |    </tr>
            |   </tbody>
            |  </table>
            |  <p></p>
            | </body>
            |</html>
        """.trimMargin()

        val expectedOutput = """
            |<html>
            | <head></head>
            | <body>
            |  <p>An <a href="http://example.com/"><b>example</b></a></p>
            |  <table>
            |   <tbody>
            |    <tr>
            |     <td style="font-family: verdana; color: red">Monkeys</td>
            |    </tr>
            |   </tbody>
            |  </table>
            |  <p></p>
            | </body>
            |</html>
        """.trimMargin()
        val doc: Document = Jsoup.parse(input)

        doc.allElements.map(::decorator)

        assertEquals(expectedOutput, doc.toString())
    }

    fun decorator(elem: Element): Element {
        if (elem.tagName() == "td") {
            elem.attr("style", elem.attr("style") + " color: red")
        }
        return elem
    }

    @Test
    fun `Can Find Source File`() {
//        val foundFile = FileManager.find("AnAcceptanceTest.html")
//        assertIsValidHtml("poo", RexSpec().poo())
    }
}