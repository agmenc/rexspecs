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
        val dept = assumeLeft(rowDescriptor.inputResults["Department"])
        return rowDescriptor.inputResults["Staff"]?.let { staffDB: Either<String, ExecutedSpecComponent> ->
            val staffDbStrings: ExecutedSpecComponent = assumeRight(staffDB)
            val staffRoles = staffDbStrings.actualRowResults.map { row: Map<String, Either<String, ExecutedSpecComponent>> ->
                val name = row["Name"]?.let { assumeLeft(it) } ?: "No name in cell"
                val role = row["Role"]?.let { assumeLeft(it) } ?: "No role in cell"
                Posting(name, role)
            }

            val calculatedDepartmentBreakdown: DepartmentBreakdown =
                calculateDepartmentBreakdown(DepartmentPostings(dept, staffRoles))

            // TODO - Don't need DepartmentPostings or DepartmentBreakdown, just tally up each role
            val specComp = ExecutedSpecComponent(
                assumeRight(columnValues["Breakdown"]),
                calculatedDepartmentBreakdown.staffTally.entries.map { (role: String, tally: Int) ->
                    mapOf(role to Either.Left(tally.toString()))
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

data class Posting(val name: String, val role: String)
data class DepartmentPostings(val department: String, val staffRolls: List<Posting>)
data class DepartmentBreakdown(val department: String, val staffTally: Map<String, Int>)

fun calculateDepartmentBreakdown(departmentPostings: DepartmentPostings): DepartmentBreakdown {
    with(departmentPostings) {
        return staffRolls.fold(DepartmentBreakdown(department, emptyMap())) { acc, (name: String, role: String) ->
            acc.staffTally[role]?.let {
                acc.copy(staffTally = acc.staffTally.map { (tallyRole: String, tallyCount: Int) ->
                    if (tallyRole == role) Pair(tallyRole, tallyCount + 1) else Pair(tallyRole, tallyCount)
                }.toMap())
            } ?: acc.copy(staffTally = acc.staffTally + Pair(role, 1))
        }
    }
}

class StaffPivotTable: Fixture {

    override fun execute(
        rowDescriptor: RowDescriptor,
        connector: Connector,
        columnValues: Map<String, Either<String, TabularTest>>
    ): Map<String, Either<String, ExecutedSpecComponent>> {
        println("StaffPivotTable.execute() ==> rowDescriptor = ${rowDescriptor}")
        return mapOf(
            "StaffPivotTable" to Either.Left("StaffPivotTable.execute is also not yet implemented")
        )
    }

}