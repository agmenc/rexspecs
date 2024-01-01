package com.rexspecs

import com.rexspecs.specs.Spec
import com.rexspecs.specs.SpecComponent
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import java.nio.ByteBuffer

class SpecRunner(
    val spec: Spec,
    val index: FixtureLookup,

    // Use Fixture to convert a SpecComponent into something suitable for whichever Connector is provided (type match)
    // SpecComponent -> [Fixture] -> inputs -> [Connector] -> outputs -> [Fixture] -> RowResult
    // TODO: Replace with Connector
    val httpHandler: HttpHandler
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
            .map { row -> fixture.processRow(zipToMap(tabularTest, row), httpHandler) }
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

    // TODO: Conversion from an HTTP Response to a RowResult belongs in the Connector
    private fun toRowResults(response: Response): RowResult {
        // TODO: Move HTTP gubbins elsewhere
        return RowResult(response.status.code.toString(), toByteArray(response.body.payload).toString(Charsets.UTF_8))
    }

    // TODO: Kill: duplicated in FixtureExamples.kt
    // Horrible mutating Java. Note that:
    //  - get() actually does a set() on the parameter
    //  - rewind() is necessary if we are re-using the response
    private fun toByteArray(byteBuf: ByteBuffer): ByteArray {
        val byteArray = ByteArray(byteBuf.capacity())
        byteBuf.get(byteArray)
        byteBuf.rewind()
        return byteArray
    }
}