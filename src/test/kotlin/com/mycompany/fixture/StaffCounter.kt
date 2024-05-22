package com.mycompany.fixture

import com.rexspecs.ExecutedSpecComponent
import com.rexspecs.RowDescriptor
import com.rexspecs.connectors.Connector
import com.rexspecs.fixture.Fixture
import com.rexspecs.fixture.createNestedTable
import com.rexspecs.specs.TabularTest
import com.rexspecs.tableRowsFor
import com.rexspecs.utils.Either
import com.rexspecs.utils.assumeLeft

class StaffCounter : Fixture {

    override fun execute(
        rowDescriptor: RowDescriptor,
        connector: Connector,
        expectedColumnValues: Map<String, Either<String, TabularTest>>
    ): Map<String, Either<String, ExecutedSpecComponent>> {
        // TODO - parseString("Department")

        // Get the rows from the Staff table, which was an input column in the main table.
        val tableRows = rowDescriptor.tableRowsFor("Staff")
            ?: return mapOf("Breakdown" to Either.Left("Can't find data from Staff table"))

        // Generate a lookup table of the grades and their respective counts.
        val lookup: Map<String, Int> = tableRows.fold(emptyMap(), ::tallyUpGrades)

        // Build the output table (the "actuals") that will be compared to the expected table.
        return createNestedTable("Breakdown", expectedColumnValues) {
            lookup.map { (type: String, tally: Int) ->
                mapOf(
                    "Type" to Either.Left(type),
                    "Tally" to Either.Left(tally.toString())
                )
            }
        }
    }

    private fun tallyUpGrades(
        tallyAccumulator: Map<String, Int>,
        tableRow: Map<String, Either<String, ExecutedSpecComponent>>
    ): Map<String, Int> {
        val grade = tableRow["Grade"]?.let { assumeLeft(it) } ?: "No grade in table row"
        return tallyAccumulator + (grade to (tallyAccumulator[grade]?.plus(1) ?: 1))
    }
}

// TODO - kill
class StaffDatabase: Fixture {

    override fun execute(
        rowDescriptor: RowDescriptor,
        connector: Connector,
        expectedColumnValues: Map<String, Either<String, TabularTest>>
    ): Map<String, Either<String, ExecutedSpecComponent>> {
        return emptyMap()
    }
}
