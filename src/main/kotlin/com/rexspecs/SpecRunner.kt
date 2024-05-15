package com.rexspecs

import com.rexspecs.connectors.Connector
import com.rexspecs.fixture.Fixture
import com.rexspecs.specs.Spec
import com.rexspecs.specs.SpecComponent
import com.rexspecs.specs.TabularTest

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

    private val nestingCallback: (TabularTest) -> ExecutedSpecComponent = { nested: TabularTest ->
        val descriptor = cleanRow(nested.inputColumns.size, nested.inputColumns, nested.expectationColumns)
        ExecutedSpecComponent(nested, executeTest(nested, index, descriptor))
    }

    private fun executeTest(
        tabularTest: TabularTest,
        index: FixtureLookup,
        rowDescriptor: RowDescriptor
    ): List<Map<String, Either<String, ExecutedSpecComponent>>> {
        return tabularTest.testRows
            .map { row: TestRow ->
                val firstColumnName = row.allTheParams.keys.toList().first()
                if (!index.containsKey(tabularTest.fixtureName)) {
                    mapOf(firstColumnName to Either.Left("Error: unrecognised fixture [${tabularTest.fixtureName}]"))
                } else {
                    // TODO - !!
                    val fixture: Fixture = index[tabularTest.fixtureName]!!
                    val columnValues: Map<String, Either<String, TabularTest>> = row.allTheParams

                    val processedRow: RowDescriptor = columnValues.toList()
                        .fold(rowDescriptor) { acc, (columnName, value: Either<String, TabularTest>) ->
                            if (acc.inputColumns.contains(columnName)) {
                                val inputResultAcc = acc + Pair(
                                    columnName,
                                    when (value) {
                                        is Either.Left -> value
                                        is Either.Right -> Either.Right(nestingCallback(value.right))
                                    }
                                )

                                // TODO - Better test, to check that all the input columns have been processed
                                if (inputResultAcc.inputResults.size == acc.inputColumns.size) {
                                    val execResult: Map<String, Either<String, ExecutedSpecComponent>> =
                                        fixture.execute(inputResultAcc, connector, columnValues)
                                    inputResultAcc + execResult
                                } else inputResultAcc
                            } else if (acc.expectationColumns.contains(columnName)) {
                                acc
                            } else {
                                throw RuntimeException("Column [${columnName}] is not in inputColumns or expectationColumns")
                            }
                        }

                    processedRow.allResults
                        ?: mapOf(firstColumnName to Either.Left("Error: no execution result for row in [${tabularTest.fixtureName}]"))
                }
            }
    }
}

data class RowDescriptor(
    val inputCount: Int,
    val inputColumns: List<String>,
    val expectationColumns: List<String>,
    val inputResults: Map<String, Either<String, ExecutedSpecComponent>>,
    val expectationResults: Map<String, Either<String, ExecutedSpecComponent>>,
    val allResults: Map<String, Either<String, ExecutedSpecComponent>>? = null
) {
    operator fun plus(cellResult: Pair<String, Either<String, ExecutedSpecComponent>>): RowDescriptor {
        return when {
            inputColumns.contains(cellResult.first) -> copy(inputResults = inputResults + cellResult, allResults = add(allResults, cellResult))
            expectationColumns.contains(cellResult.first) -> copy(expectationResults = expectationResults + cellResult, allResults = add(allResults, cellResult))
            else -> throw RuntimeException("Column [${cellResult.first}] is not in inputColumns or expectationColumns")
        }
    }

    operator fun plus(cellResults: Map<String, Either<String, ExecutedSpecComponent>>): RowDescriptor {
        return cellResults.entries.fold(this) { acc, (k, v) -> acc + Pair(k, v) }
    }

    private fun add(
        map: Map<String, Either<String, ExecutedSpecComponent>>?,
        pair: Pair<String, Either<String, ExecutedSpecComponent>>
    ): Map<String, Either<String, ExecutedSpecComponent>> {
        return map?.plus(pair) ?: mapOf(pair)
    }
}

fun cleanRow(inputCount: Int,inputColumns: List<String>, expectationColumns: List<String>): RowDescriptor {
    return RowDescriptor(inputCount, inputColumns, expectationColumns, emptyMap(), emptyMap())
}