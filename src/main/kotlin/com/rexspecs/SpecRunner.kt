package com.rexspecs

import com.rexspecs.Either.Left
import com.rexspecs.connectors.Connector
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
                is TabularTest -> ExecutedSpecComponent(component, executeTest(component, index))
                else -> ExecutedSpecComponent(component, emptyList())
            }
        }
    }

    private val nestingCallback: (TabularTest) -> ExecutedSpecComponent = { nested: TabularTest ->
        ExecutedSpecComponent(nested, executeTest(nested, index))
    }

    private fun executeTest(
        tabularTest: TabularTest,
        index: FixtureLookup
    ): List<List<Either<String, ExecutedSpecComponent>>> {
        if (tabularTest.fixtureName == "Bird Counter") {
            println("Debug here")
        }

        return tabularTest.testRows
            .map { row: TestRow ->
                (index[tabularTest.fixtureName]
                    ?.processRow(zipToMap(tabularTest, row), connector, nestingCallback)
                    ?: listOf(Left("Error: unrecognised fixture [${tabularTest.fixtureName}]")))
            }
    }

    private fun zipToMap(tabularTest: TabularTest, row: TestRow): Map<String, Either<String, TabularTest>> {
        return tabularTest.inputColumns.zip(row.inputParams).toMap()
    }
}