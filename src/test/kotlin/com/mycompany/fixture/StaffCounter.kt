package com.mycompany.fixture

import com.rexspecs.*
import com.rexspecs.connectors.Connector
import com.rexspecs.fixture.Fixture
import com.rexspecs.specs.TabularTest

class StaffCounter : Fixture {

    override fun execute(
        rowDescriptor: RowDescriptor,
        connector: Connector,
        columnValues: Map<String, Either<String, TabularTest>>
    ): Map<String, Either<String, ExecutedSpecComponent>> {
        return rowDescriptor.allResults["Staff"]?.let { staffDB: Either<String, ExecutedSpecComponent> ->
            val lookup: Map<String, Int> =
                assumeRight(staffDB).actualRowResults.fold(emptyMap()) { acc, row ->
                    val grade = row["Grade"]?.let { assumeLeft(it) } ?: "No grade in table row"
                    acc + (grade to (acc[grade]?.plus(1) ?: 1))
                }

            val specComp = ExecutedSpecComponent(
                assumeRight(columnValues["Breakdown"]),
                lookup.map { (type: String, tally: Int) ->
                    mapOf(
                        "Type" to Either.Left(type),
                        "Tally" to Either.Left(tally.toString())
                    )
                }
            )

            mapOf(
                "Breakdown" to Either.Right(specComp)
            )
        } ?: mapOf("Breakdown" to Either.Left("Monkeys ate it again"))
    }
}

class StaffDatabase: Fixture {

    override fun execute(
        rowDescriptor: RowDescriptor,
        connector: Connector,
        columnValues: Map<String, Either<String, TabularTest>>
    ): Map<String, Either<String, ExecutedSpecComponent>> {
        return emptyMap()
    }

}
