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
        val descriptor = cleanRow(nested.inputColumns, nested.expectationColumns)
        ExecutedSpecComponent(nested, executeTest(nested, index, descriptor))
    }

    private fun executeTest(
        tabularTest: TabularTest,
        index: FixtureLookup,
        rowDescriptor: RowDescriptor
    ): List<List<Either<String, ExecutedSpecComponent>>> {
//        println("Running TabularTest: ${tabularTest.fixtureName}")
        return tabularTest.testRows
            .map { row: TestRow ->
                if (!index.containsKey(tabularTest.fixtureName)) {
                    listOf(Either.Left("Error: unrecognised fixture [${tabularTest.fixtureName}]"))
                } else {
                    // TODO - !!
                    val fixture: Fixture = index[tabularTest.fixtureName]!!
                    val columnValues: Map<String, Either<String, TabularTest>> = zipToMap(tabularTest, row)

                    val processedRow: RowDescriptor = columnValues.toList()
                        .fold(rowDescriptor) { acc, (columnName, value: Either<String, TabularTest>) ->
                            if (acc.inputColumns.contains(columnName)) {
//                                println("Processing input ${columnName}")
                                val inputResultAcc = acc + Pair(
                                    columnName,
                                    fixture.processInput(columnName, value, connector, nestingCallback)
                                )

                                // TODO - Better test, to check that all the input columns have been processed
                                if (inputResultAcc.inputResults.size == acc.inputColumns.size) {
//                                    println("Executing after column [${columnName}] with rowDescriptor = ${inputResultAcc}")

                                    // TODO - Don't need executionResult, just make these the expectationResults, all in a dump here, and can the call to Fixture.processResult()
                                    val executionResult: Map<String, Either<String, ExecutedSpecComponent>> = fixture.execute(inputResultAcc, connector)
                                    inputResultAcc.copy(executionResult = executionResult)
                                } else inputResultAcc
                            } else if (acc.expectationColumns.contains(columnName)) {
                                val columnResult = acc.executionResult?.get(columnName)
                                    ?: Either.Left("No result for column $columnName")
                                acc + Pair(columnName, columnResult)
                            } else {
                                throw RuntimeException("Column [${columnName}] is not in inputColumns or expectationColumns")
                            }
                        }

                    processedRow.inputResults.values.toList() + processedRow.expectationResults.values.toList()
                }
            }
    }

    private fun zipToMap(tabularTest: TabularTest, row: TestRow): Map<String, Either<String, TabularTest>> {
        return tabularTest.inputColumns.zip(row.inputParams).toMap() + tabularTest.expectationColumns.zip(row.expectedResults).toMap()
    }
}

data class RowDescriptor(
    val inputColumns: List<String>,
    val expectationColumns: List<String>,
    val inputResults: Map<String, Either<String, ExecutedSpecComponent>>,
    val expectationResults: Map<String, Either<String, ExecutedSpecComponent>>,
    val executionResult: Map<String, Either<String, ExecutedSpecComponent>>? = null
) {
    operator fun plus(pair: Pair<String, Either<String, ExecutedSpecComponent>>): RowDescriptor {
        return when {
            inputColumns.contains(pair.first) -> copy(inputResults = inputResults + pair)
            expectationColumns.contains(pair.first) -> copy(expectationResults = expectationResults + pair)
            else -> throw RuntimeException("Column [${pair.first}] is not in inputColumns or expectationColumns")
        }
    }

}

fun cleanRow(inputColumns: List<String>, expectationColumns: List<String>): RowDescriptor {
    return RowDescriptor(inputColumns, expectationColumns, emptyMap(), emptyMap())
}