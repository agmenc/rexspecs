package com.rexspecs

import com.rexspecs.connectors.Connector
import com.rexspecs.fixture.Fixture
import com.rexspecs.inputs.InputReader
import com.rexspecs.outputs.OutputWriter
import com.rexspecs.specs.SpecComponent
import com.rexspecs.specs.TabularTest
import com.rexspecs.utils.failed
import com.rexspecs.utils.succeeded
import kotlinx.serialization.Serializable

typealias FixtureLookup = Map<String, Fixture>

/*
Benders:
    - TODO: Wire it in to something real.
    - Nested tables
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
    - Suite runs should fail if there are no tests
    - Support result lists demarcated with <br/>.
    - Find any TODO and JFDI
    - Create intermediate directories in output tree if they don't exist
    - Put basic docs in README.md: Motivation, Installation, Getting Started, Links to Examples
    - Put latest version and Gradle dependency in the README.md, using whatever auto magic Github provides
    - Include the default CSS theme as a prod resource, but only write it to the suite directory if it doesn't already exist
    - Include toggle.js as a prod resource, but only write it to the suite directory if it doesn't already exist
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
data class TestRow(val inputParams: List<String>, val expectedResult: RowResult) {
    fun expectationCount() = expectedResult.cells()
}

@Serializable
data class RowResult(val resultValues: List<String>) {
    fun cells() = resultValues.size

    companion object {
        operator fun invoke(vararg resultValues: String) = RowResult(resultValues.toList())
    }
}

data class ExecutedSuite(val executedSpecs: List<ExecutedSpec>) {
    fun success(): Boolean = executedSpecs.fold(true) { allGood, nextSpec -> allGood && nextSpec.success() }
}

data class ExecutedSpec(val identifier: String, val executedTests: List<ExecutedSpecComponent>) {
    fun success(): Boolean = executedTests.fold(true) { allGood, nextTable -> allGood && nextTable.success() }
}

data class ExecutedSpecComponent(val specComponent: SpecComponent, val actualRowResults: List<RowResult>) {
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
            .map { it.expectedResult }
            .zip(actualRowResults)
            .forEach { (expected, actual) -> if (expected != actual) return false }

        return true
    }
}

class InvalidStartingState(message: String) : RuntimeException(message)