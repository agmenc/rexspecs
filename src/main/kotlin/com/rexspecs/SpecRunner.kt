package com.rexspecs

import com.rexspecs.connectors.Connector
import com.rexspecs.fixture.Fixture
import com.rexspecs.specs.Spec
import com.rexspecs.specs.SpecComponent
import com.rexspecs.specs.TabularTest
import com.rexspecs.utils.Either
import com.rexspecs.utils.identity
import com.rexspecs.utils.mapBoth
import com.rexspecs.utils.plus

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


        // TODO - introduce my own scope variable for blatting through null checks
        val fixture: Fixture? = when {
            index[tabularTest.fixtureName] != null -> index[tabularTest.fixtureName]!!
            rowDescriptor.parentColumn != null -> index[rowDescriptor.parentColumn]!!
            else -> null
        }

        /*
        chain 1: tabularTest.fixtureName AND index[tabularTest.fixtureName] AND-not-null
        chain 2: rowDescriptor.parentColumn AND index[rowDescriptor.parentColumn] AND-not-null

        chain 1 orThen chain 2 orThen ...

        orThen is nullable receiver, returning identity or executing the next block
         */

        // TODO - Write something that lets Elvis take a block
        return tabularTest.testRows
            .map { row: TestRow ->
                fixture?.let {
                    val processedRow: RowDescriptor = row.allTheParams.toList()
                        .fold(rowDescriptor) { acc, (columnName, value: Either<String, TabularTest>) ->
                            if (acc.inputColumns.contains(columnName)) {
                                val inputResultAcc = acc + Pair(
                                    columnName,
                                    value.mapBoth(::identity) { nestingCallback(it, columnName) }
                                )

                                if (inputResultAcc.inputsComplete()) {
                                    val execResult: Map<String, Either<String, ExecutedSpecComponent>> =
                                        fixture.execute(inputResultAcc, connector, row.allTheParams)
                                    inputResultAcc + execResult

                                } else inputResultAcc
                            } else if (acc.expectationColumns.contains(columnName)) {
                                // TODO - Can't we just treat outputs the same as inputs, if they have access to the execution results?
                                acc
                            } else {
                                throw RuntimeException("Column [${columnName}] is not in inputColumns or expectationColumns\n inputColumns: ${acc.inputColumns}\n expectationColumns: ${acc.expectationColumns}")
                            }
                        }

                    processedRow.allResults
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
    val allResults: Map<String, Either<String, ExecutedSpecComponent>>,
    val parentColumn: String? = null
) {
    operator fun plus(cellResult: Pair<String, Either<String, ExecutedSpecComponent>>): RowDescriptor = when {
        inputColumns.contains(cellResult.first) -> copy(allResults = cellResult + allResults)
        expectationColumns.contains(cellResult.first) -> copy(allResults = cellResult + allResults)
        else -> throw RuntimeException("Column [${cellResult.first}] is not in inputColumns or expectationColumns")
    }

    operator fun plus(cellResults: Map<String, Either<String, ExecutedSpecComponent>>): RowDescriptor {
        return cellResults.entries.fold(this) { acc, (k, v) -> acc + Pair(k, v) }
    }
}

fun RowDescriptor.inputsComplete(): Boolean = allResults.size == inputColumns.size

fun cleanRow(inputCount: Int,inputColumns: List<String>, expectationColumns: List<String>): RowDescriptor {
    return RowDescriptor(inputCount, inputColumns, expectationColumns, emptyMap())
}