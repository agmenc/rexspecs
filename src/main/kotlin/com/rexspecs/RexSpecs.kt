@file:UseSerializers(EitherSerializer::class)
package com.rexspecs

import com.rexspecs.connectors.Connector
import com.rexspecs.fixture.Fixture
import com.rexspecs.inputs.InputReader
import com.rexspecs.outputs.OutputWriter
import com.rexspecs.specs.SpecComponent
import com.rexspecs.specs.TabularTest
import com.rexspecs.utils.EitherSerializer
import com.rexspecs.utils.failed
import com.rexspecs.utils.succeeded
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

typealias FixtureLookup = Map<String, Fixture>

/*
Benders:
    - TODO: Wire it in to something real.
    - Nested tables
    - Remove header rows from nested tables
    - Process every cell, so that we can write back error messages and status colours in input cells. Something like:
        - each row becomes a list of triples: Column Name, Execution results, data result
        - at any point in row processing, we can access any processed value by Column Name
    - List Fixtures - given whatever is in the header, the results should look like all the rows
    - Real candidates:
        STACKRR - The Stack Tree Tracker. JSON over CLI.
        UrThredz II - The Wrath of Tabs
        Kanban in Code - a way to manage stories, epics, etc in your repo. Possibly a little app that moves STORY.mds into different sub-folders as you make changes.
    - Generate a Suite index - this is the FEATURE MATRIX!!!
    - Build a more complicated example, with multiple steps, and some sort of state
    - Make the fixture lookup use magic, so that we don't need to provide one. Probably class.forName() from some specified root package.
    - Make the HTML look prettier still (need some guidance with this one)
    - Document the stuff below (marked ***HERE***) in the README.md Mermaid diagrams
    - Why not save results in the repo?
    -

Tasks:
    - Blow up if a row of column headers is not in a <th> (i.e. it is in a <td>). Or just support both. Whatever is easiest.
    - Suite runs should fail if there are no tests
    - Support result lists demarcated with <br/>.
    - Find any TODO and JFDI
    - Create intermediate directories in output tree if they don't exist
    - Put basic docs in README.md: Motivation, Installation, Getting Started, Links to Examples
    - Put latest version and Gradle dependency in the README.md, using whatever auto magic Github provides
    - Include the default CSS theme as a prod resource, but only write it to the suite directory if it doesn't already exist
    - Include toggle.js as a prod resource, but only write it to the suite directory if it doesn't already exist
    - Use the -Xjdk-release Kotlin compile flag to better support my target minimum Java version (see https://jakewharton.com/kotlins-jdk-release-compatibility-flag/ )
    - Error: no tests in suite
    - Use Http4k to load props
    - Make Maven publishing a single script run
    - Load props from the environment
    - Push most stuff in RexSpecsTest into the specs themselves. Might mean they all need to pass.
    - Select from available input readers based on the file extension
    - HtmlFileInputReader: should only read files with .html extension.
    - JsonFileInputReader: should only read files with .json extension.
    - Write out some stats per test (pass/fail counts)
    - Write out some stats per suite (pass/fail counts)
    - Add versions to all dependencies and plugins
    -
 */

// ***HERE***
// SuiteRunner (built-in): moves through the list of specs identified by the InputReader, and executes them one-by-one
// SuiteRunner (built-in): performs tidy-ups by telling the OutputWriter to do pre-test housekeeping.
// InputReader: knows where to find specs, and how to read them into their JSON representation
// Specs: are identified in a tree structure (regardless of filesystem, DB, or whatever source)
// Specs: emit, or are composed of JSON, processed row by row (e.g. from HTML tables)
// SpecRunner (built-in): Reads JSON from the reader and sends it to the Connector
// SpecRunner (built-in): Receives a JSON result from the Connector
// SpecRunner (built-in): Sends both input and output to the OutputWriter
// HttpHandler: a type of ***Connector***, that accepts JSON, makes an API call, and translates the response back into JSON
// OutputWriter: outputs a decorated version of the input, highlighting expected vs actual results
// FixtureLookup: matches table names to test fixtures.
// Dependencies: InputReader, OutputWriter, FixtureLookup, HttpHandler

class RexSpecs {
    companion object {
        @JvmStatic fun runSuite(
            inputReader: InputReader,
            outputWriter: OutputWriter,
            fixtureLookup: FixtureLookup,
            connector: Connector
        ): ExecutedSuite {
            inputReader.prepareForInput()
            outputWriter.prepareForOutput()
            return ExecutedSuite(inputReader.specs().map {
                SpecRunner(it, fixtureLookup, connector).execute()
                    .also { spec -> outputWriter.writeSpecResults(spec) }
                    .also { spec -> if (spec.success()) succeeded(spec.identifier) else failed(spec.identifier) }
            })
        }
    }
}

@Serializable
sealed class Either<out L, out R> {
    // TODO - implement map(), mapLeft() and mapRight()

    @Serializable
    data class Left<out L>(val left: L) : Either<L, Nothing>()

    @Serializable
    data class Right<out R>(val right: R) : Either<Nothing, R>()
}

// TODO - Combine and simplify assumeLeft and assumeRight
fun <L, R> assumeLeft(value: Either<L, R>?): L =
    when (value) {
        is Either.Left -> value.left
        // TODO - log type information for L and R
        else -> throw RuntimeException("Expected Either.Left, but was ${value}")
    }

// TODO - Combine and simplify assumeLeft and assumeRight
fun <L, R> assumeRight(value: Either<L, R>?): R =
    when (value) {
        is Either.Right -> value.right
        // TODO - log type information for L and R
        else -> throw RuntimeException("Expected Either.Right, but was ${value}")
    }

// TODO - Make this typesafe and not awful
fun <T> lefts(inputs: Map<String, Either<String, T>>): Map<String, Either.Left<String>> {
    return inputs.filter { (_, v) -> v is Either.Left<String> } as Map<String, Either.Left<String>>
}

fun eithers(vararg strings: String): List<Either.Left<String>> = strings.map { Either.Left(it) }

@Serializable
data class TestRow(val inputParams: List<Either<String, TabularTest>>, val expectedResults: List<Either<String, TabularTest>>) {
    fun expectationCount() = expectedResults.size
}

data class ExecutedSuite(val executedSpecs: List<ExecutedSpec>) {
    fun success(): Boolean = executedSpecs.fold(true) { allGood, nextSpec -> allGood && nextSpec.success() }
}

data class ExecutedSpec(val identifier: String, val executedTests: List<ExecutedSpecComponent>) {
    fun success(): Boolean = executedTests.fold(true) { allGood, nextTable -> allGood && nextTable.success() }
}

data class ExecutedSpecComponent(val specComponent: SpecComponent, val actualRowResults: List<List<Either<String, ExecutedSpecComponent>>>) {
    fun success(): Boolean {
        return when (specComponent) {
            is TabularTest -> testSuccessful(specComponent)
            /* is GraphicalTest -> ... */
            /* is MermaidTest -> ... */
            else -> true
        }
    }

    // TODO: Find a fold() equivalent that returns immediately on the first false value
    private fun testSuccessful(tabularTest: TabularTest): Boolean {
        tabularTest.testRows
            .map { it.inputParams + it.expectedResults }
            .zip(actualRowResults)
            .forEach { (expected, actual) -> if (expected != actual) return false }

        return true
    }
}

class InvalidStartingState(message: String) : RuntimeException(message)
class InvalidStructure(message: String) : RuntimeException(message)

@Deprecated("Remove usages before commit", ReplaceWith(""))
fun <T> T.debugged() = also {
    println(it)
}

@Deprecated("Remove usages before commit", ReplaceWith(""))
fun <T> T.printed() = also(::println)

@Deprecated("Remove usages before commit", ReplaceWith(""))
fun <T> T.printed(blah: String) = also { println(blah) }