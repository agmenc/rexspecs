package com.rexspecs

import com.rexspecs.connectors.Connector
import com.rexspecs.specs.Spec
import com.rexspecs.specs.SpecComponent

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
        val fixture = index[tabularTest.fixtureName]!!

        return tabularTest.testRows
            .map { row -> fixture.processRow(zipToMap(tabularTest, row), connector) }
    }

    private fun zipToMap(tabularTest: TabularTest, row: TestRow): Map<String, String> {
        tabularTest.columnNames.zip(row.inputParams)

        // TODO: This. Properly.
        return mapOf(
            tabularTest.columnNames[0] to row.inputParams[0],
            tabularTest.columnNames[1] to row.inputParams[1],
            tabularTest.columnNames[2] to row.inputParams[2]
        )
    }
}
