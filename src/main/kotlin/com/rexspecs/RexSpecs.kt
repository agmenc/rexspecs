package com.rexspecs

import com.rexspecs.connectors.Connector
import com.rexspecs.fixture.Fixture
import com.rexspecs.inputs.InputReader
import com.rexspecs.outputs.OutputWriter
import com.rexspecs.specs.SpecComponent

typealias FixtureLookup = Map<String, Fixture>

/*
Benders:
    - Find any TODO and JFDI
    - Document the stuff below in the README.md Mermaid diagrams
    - Create a separate test for directly-called targets: just remove the HTTP expectations
    - Error: source directory does not exist
    - Error: target directory does not exist
    - Error: no tests in suite
    - Make the fixture lookup use magic, so that we don't need to provide one. Probably class.forName() from some specified root package.
    -
    -
    -
 */

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

data class TabularTest(val fixtureName: String, val columnNames: List<String>, val testRows: List<TestRow>): SpecComponent

data class TestRow(val inputParams: List<String>, val expectedResult: RowResult) {
    fun cells() = expectedResult.cells()
}

// TODO: Make resultValues an array, so RowResult can just be a data class. The boilerplate looks ugly.
class RowResult(vararg val resultValues: String) {
    fun cells() = resultValues.size

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RowResult) return false

        if (!resultValues.contentEquals(other.resultValues)) return false

        return true
    }

    override fun hashCode(): Int {
        return resultValues.contentHashCode()
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