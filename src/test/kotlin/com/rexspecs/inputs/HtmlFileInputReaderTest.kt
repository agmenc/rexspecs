package com.rexspecs.inputs

import com.rexspecs.*
import com.rexspecs.specs.*
import com.rexspecs.utils.RexSpecPropertiesLoader
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.io.path.pathString

class HtmlFileInputReaderTest {

    @Test
    fun `Can find a Spec by ID`() {
        val inputReader = HtmlFileInputReader("suites/http_examples")

        assertTrue(inputReader.specs().iterator().hasNext())
    }

    @Disabled
    @Test
    fun `Fails with a clear error message when there are no inputs`() {
        val test = SingleHtmlFileInputReader("The Naughty Test.html", httpProps.rexspecsDirectory).firstTest()

        // TODO - Where's the error message?
        val expectedResult = TabularTest(
            "Calculator",
            emptyList(),
            listOf("First Param", "Operator", "Second Param", "HTTP Response", "Result"),
            listOf(
                TestRow(
                    0,
                    "First Param" to "7",
                    "Operator" to "+",
                    "Second Param" to "8",
                    "HTTP Response" to "200",
                    "Result" to "15"
                ),
                TestRow(
                    0,
                    "First Param" to "7",
                    "Operator" to "x",
                    "Second Param" to "8",
                    "HTTP Response" to "201",
                    "Result" to "56"
                )
            )
        )

        assertEquals(expectedResult, test)
    }

    @Test
    fun `Specs are identified by their relative path from the specs source root directory`() {
        val inputReader = HtmlFileInputReader("suites/http_examples")

        assertEquals(
            listOf("The Naughty Test.html", "nesting/Nested Spec.html", "Calculator Over HTTP.html"),
            inputReader.specs().map { it.identifier }
        )
    }

    @Test
    fun `Can convert a table to a test representation`() {
        val tableElement = Jsoup.parse(sampleInput).allElements
            .toList()
            .first { it.tagName() == "table" }

        val expectedResult = TabularTest(
            "Calculator",
            listOf("First Param", "Operator", "Second Param"),
            listOf("HTTP Response", "Result"),
            listOf(
                TestRow(
                    3,
                    "First Param" to "7",
                    "Operator" to "+",
                    "Second Param" to "8",
                    "HTTP Response" to "200",
                    "Result" to "15"
                ),
                TestRow(
                    3,
                    "First Param" to "7",
                    "Operator" to "x",
                    "Second Param" to "8",
                    "HTTP Response" to "201",
                    "Result" to "56"
                )
            )
        )

        val component = HtmlFileInputReader("Whatever").convertTableToTest(tableElement)

        assertEquals(expectedResult, component)
    }

    @Test
    fun `Can read in a source file as input`() {
        val props = RexSpecPropertiesLoader.properties()
        val spec = SingleHtmlFileInputReader("Calculator Over HTTP.html", props.rexspecsDirectory).specs().first()

        val expectedSpec = Spec(
            "Calculator Over HTTP.html",
            listOf(
                Title("Simple Acceptance Test Example"),
                Heading("Calculator Over HTTP Example"),
                Description("Calculator App: receives operands and an operator, and calculates the result."),
                httpCalculationTest
            )
        )


        assertEquals(expectedSpec, spec)
    }

    @Test
    fun `DirectoryManager barfs when the source root doesn't exist`() {
        assertThrows<InvalidStartingState>("Cannot find Rexspecs directory [potato]") {
            HtmlFileInputReader("potato").prepareForInput()
        }
    }

    @Test
    fun `DirectoryManager barfs when the specs folder doesn't exist`() {
        val tempRexSpecsDir = createTempDirectory().pathString
        assertFalse(File(tempRexSpecsDir, "specs").exists())

        assertThrows<InvalidStartingState>("Cannot find Rexspecs source directory [$tempRexSpecsDir/specs]") {
            HtmlFileInputReader(tempRexSpecsDir).prepareForInput()
        }
    }

    @Test
    fun `Tables can contain nested tables as inputs`() {
        val inputDocument: Document = Jsoup.parse(nestedInput)

        val nestedInput = TabularTest(
            null,
            listOf("Start", "End"),
            emptyList(),
            listOf(
                TestRow(
                    2,
                    "Start" to "Monday",
                    "End" to "Wednesday"
                )
            )
        )

        val nestedOutput = TabularTest(
            "Count Type",
            emptyList(),
            listOf("Eggs", "Chicks"),
            listOf(
                TestRow(
                    0,
                    "Eggs" to "2",
                    "Chicks" to "1"
                )
            )
        )

        val expectedSpecComponents = listOf(
            Title("Nested Tables Example"),
            Heading("Nested Tables Example"),
            Description("Turtles, all the way down."),
            TabularTest(
                "Bird Counter",
                listOf("Species", "Time Range"),
                listOf("Census"),
                listOf(
                    TestRow(
                        2,
                        mapOf(
                            "Species" to Either.Left("Blue Tit"),
                            "Time Range" to Either.Right(nestedInput),
                            "Census" to Either.Right(nestedOutput)
                        )
                    )
                )
            )
        )

        val actualSpecComponents = HtmlFileInputReader("Whatever").htmlToSpecComponents(inputDocument)

        assertEquals(expectedSpecComponents, actualSpecComponents)
    }
}