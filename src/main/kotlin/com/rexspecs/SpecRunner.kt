package com.rexspecs

import com.rexspecs.connectors.Connector
import com.rexspecs.fixture.Fixture
import com.rexspecs.specs.Spec
import com.rexspecs.specs.SpecComponent
import com.rexspecs.specs.TabularTest
import com.rexspecs.utils.*

class SpecRunner(
    private val spec: Spec,
    // TODO: Pass in the Fixture, not the whole lookup
    private val index: FixtureLookup,
    private val connector: Connector
) {
    fun execute(): ExecutedSpec = ExecutedSpec(
        spec.identifier,
        executeComponents(spec.components)
    )

    private fun executeComponents(specComponents: List<SpecComponent>): List<ExecutedSpecComponent> {
        return specComponents.map { component ->
            when (component) {
                is TabularTest -> nestingCallback(component)
                else -> ExecutedSpecComponent(component, emptyList())
            }
        }
    }

    private fun nestingCallback(nested: TabularTest, parentColumn: String? = null): ExecutedSpecComponent {
        val descriptor = RowDescriptor(nested.inputColumns.size, nested.inputColumns, nested.expectationColumns, emptyMap(), parentColumn)
        return ExecutedSpecComponent(nested, executeTest(nested, index, descriptor))
    }

    private fun executeTest(
        tabularTest: TabularTest,
        index: FixtureLookup,
        rowDescriptor: RowDescriptor
    ): List<Map<String, Either<String, ExecutedSpecComponent>>> {
        val fixture: Fixture? = index[tabularTest.fixtureName] ?: index[rowDescriptor.parentColumn]
        return tabularTest.testRows
            .map { row: TestRow ->
                fixture?.let {
                    val processedRow: RowDescriptor = row.allTheParams.toList()
                        .fold(rowDescriptor) { acc, (columnName, value: Either<String, TabularTest>) ->
                            if (acc.inputColumns.contains(columnName)) {
                                val inputResultAcc = acc + Pair(
                                    columnName,
                                    value.mapRight { nestingCallback(it, columnName) }
                                )

                                if (inputResultAcc.inputsComplete()) {
                                    inputResultAcc + fixture.execute(inputResultAcc, connector, row.allTheParams)
                                } else inputResultAcc
                            } else if (acc.expectationColumns.contains(columnName)) {
                                // TODO - Can't we just treat outputs the same as inputs, if they have access to the execution results?
                                acc
                            } else {
                                throw RuntimeException("Column [${columnName}] is not in inputColumns or expectationColumns\n inputColumns: ${acc.inputColumns}\n expectationColumns: ${acc.expectationColumns}")
                            }
                        }

                    processedRow.resultsForThisRow
                } ?: unrecognisedFixture(rowDescriptor, tabularTest)
            }
    }

    private fun unrecognisedFixture(rowDescriptor: RowDescriptor, tabularTest: TabularTest): Map<String, Either.Left<String>> =
        mapOf(rowDescriptor.inputColumns.first() to Either.Left("Error: unrecognised fixture [${tabularTest.fixtureName}]"))
}

data class RowDescriptor(
    val inputCount: Int,
    val inputColumns: List<String>,
    val expectationColumns: List<String>,
    val resultsForThisRow: Map<String, Either<String, ExecutedSpecComponent>>,
    val parentColumn: String? = null
) {
    operator fun plus(cellResult: Pair<String, Either<String, ExecutedSpecComponent>>): RowDescriptor = when {
        inputColumns.contains(cellResult.first) -> copy(resultsForThisRow = cellResult + resultsForThisRow)
        expectationColumns.contains(cellResult.first) -> copy(resultsForThisRow = cellResult + resultsForThisRow)
        else -> throw RuntimeException("Column [${cellResult.first}] is not in inputColumns or expectationColumns")
    }

    operator fun plus(cellResults: Map<String, Either<String, ExecutedSpecComponent>>): RowDescriptor {
        return cellResults.entries.fold(this) { acc, (k, v) -> acc + Pair(k, v) }
    }
}

fun RowDescriptor.inputsComplete(): Boolean = resultsForThisRow.size == inputColumns.size

fun RowDescriptor.tableRowsFor(name: String): List<Map<String, Either<String, ExecutedSpecComponent>>>? =
    this.resultsForThisRow[name]?.let { table: Either<String, ExecutedSpecComponent> ->
        assumeRight(table).resultsForAllRows
    }