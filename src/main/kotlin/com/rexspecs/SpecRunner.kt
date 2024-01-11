package com.rexspecs

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

    private fun executeTest(tabularTest: TabularTest, index: FixtureLookup): List<RowResult> {

        return tabularTest.testRows
            .map { row ->
                index[tabularTest.fixtureName]
                    ?. processRow(zipToMap(tabularTest, row), connector)
                    ?: RowResult("Error: unrecognised fixture [${tabularTest.fixtureName}]")
            }
    }

    private fun zipToMap(tabularTest: TabularTest, row: TestRow): Map<String, String> {
        tabularTest.inputColumns.zip(row.inputParams)

        // TODO: This. Properly.
        return mapOf(
            tabularTest.inputColumns[0] to row.inputParams[0],
            tabularTest.inputColumns[1] to row.inputParams[1],
            tabularTest.inputColumns[2] to row.inputParams[2]
        )
    }
}
