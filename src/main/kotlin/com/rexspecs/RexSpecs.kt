package com.rexspecs

import com.rexspecs.connectors.Connector
import com.rexspecs.fixture.Fixture
import com.rexspecs.inputs.InputReader
import com.rexspecs.outputs.OutputWriter
import com.rexspecs.specs.SpecComponent
import com.rexspecs.specs.TabularTest
import kotlinx.serialization.Serializable

typealias FixtureLookup = Map<String, Fixture>

/*

Benders:
    - Make the HTML look prettier still (need some guidance with this one)
    - Build a more complicated example, with multiple steps, and some sort of state
    - Create a separate test directory structure for directly-called targets, so that we can show different kinds of test suites
    - Find a better way to demarcate input params from expected results (probably a CSS class)
    - Make the fixture lookup use magic, so that we don't need to provide one. Probably class.forName() from some specified root package.
    - Document the stuff below (marked ***HERE***) in the README.md Mermaid diagrams
    -

Tasks:
    - Find any TODO and JFDI
    - Put an input/output toggle link on each spec
    - Remove console noise while running the specs
    - HtmlFileInputReader: should only read files with .html extension
    - Write out some stats per test (pass/fail counts)
    - Error: source directory does not exist
    - Error: target directory does not exist
    - Error: no tests in suite
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
fun runSuite(
    inputReader: InputReader,
    outputWriter: OutputWriter,
    fixtureLookup: FixtureLookup,
    connector: Connector
): ExecutedSuite {
    outputWriter.cleanTargetDir()
    return ExecutedSuite(inputReader.specs()
        .map { SpecRunner(it, fixtureLookup, connector).execute() })
        .also { executedSuite -> outputWriter.writeSpecResults(executedSuite.firstSpec()) }
        .also { executedSuite -> println("RexSpecs: ${if (executedSuite.success()) "SUCCESS" else "FAILURE"}") }
}



@Serializable
data class TestRow(val inputParams: List<String>, val expectedResult: RowResult) {
    fun cells() = expectedResult.cells()
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
    fun firstSpec(): ExecutedSpec = executedSpecs.first()
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