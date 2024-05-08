package com.mycompany.fixture

import com.rexspecs.*
import com.rexspecs.connectors.Connector
import com.rexspecs.fixture.Fixture
import com.rexspecs.specs.TabularTest

class StaffCounter : Fixture {

    override fun processResult(
        columnName: String,
        value: Either<String, TabularTest>,
        connector: Connector,
        nestingCallback: (TabularTest) -> ExecutedSpecComponent,
        rowDescriptor: RowDescriptor
    ): Either<String, ExecutedSpecComponent> {
        return when (value) {
            is Either.Left -> value
            is Either.Right -> Either.Right(nestingCallback(value.right))
        }
    }

    override fun execute(rowDescriptor: RowDescriptor, connector: Connector): Map<String, Either<String, ExecutedSpecComponent>> {
        val dept = assumeLeft(rowDescriptor.inputResults["Department"])
        return rowDescriptor.inputResults["Staff"]?.let { staffDB: Either<String, ExecutedSpecComponent> ->
            val staffDbStrings: ExecutedSpecComponent = assumeRight(staffDB)
            val staffRoles = staffDbStrings.actualRowResults.map { row ->
                val (x, y) = row.map { assumeLeft(it) }
                Posting(x, y)
            }

            val calculateDepartmentBreakdown: DepartmentBreakdown = calculateDepartmentBreakdown(DepartmentPostings(dept, staffRoles))

            mapOf(
                "Breakdown" to Either.Left("Need to convert a tally into a nested table structure")
            )
        } ?: mapOf("Breakdown" to Either.Left("Monkeys ate it again"))
    }
}

class StaffDatabase: Fixture {

    override fun execute(rowDescriptor: RowDescriptor, connector: Connector): Map<String, Either<String, ExecutedSpecComponent>> {
        return mapOf(
            "Staff" to Either.Left("StaffDatabase.execute is not yet implemented. Should this be a null op?")
        )
    }

    override fun processResult(
        columnName: String,
        value: Either<String, TabularTest>,
        connector: Connector,
        nestingCallback: (TabularTest) -> ExecutedSpecComponent,
        rowDescriptor: RowDescriptor
    ): Either<String, ExecutedSpecComponent> {
        TODO("Not required - input only")
    }
}

data class Posting(val name: String, val role: String)
data class DepartmentPostings(val department: String, val staffRolls: List<Posting>)
data class DepartmentBreakdown(val department: String, val staffTally: Map<String, Int>)

fun calculateDepartmentBreakdown(departmentPostings: DepartmentPostings): DepartmentBreakdown =
    with (departmentPostings) {
        return staffRolls.fold(DepartmentBreakdown(department, emptyMap())) { acc, (role: String, _: String) ->
            acc.staffTally[role]?.let {
                acc.copy(staffTally = acc.staffTally.map { (k: String, v: Int)  ->
                    if (k == role) Pair(k, v+1) else Pair(k, v)
                }.toMap())
            } ?: acc
        }
    }

class StaffPivotTable: Fixture {

    override fun execute(rowDescriptor: RowDescriptor, connector: Connector): Map<String, Either<String, ExecutedSpecComponent>> {
        println("StaffPivotTable.execute() ==> rowDescriptor = ${rowDescriptor}")
        return mapOf(
            "StaffPivotTable" to Either.Left("StaffPivotTable.execute is also not yet implemented")
        )
    }

    override fun processResult(
        columnName: String,
        value: Either<String, TabularTest>,
        connector: Connector,
        nestingCallback: (TabularTest) -> ExecutedSpecComponent,
        rowDescriptor: RowDescriptor
    ): Either<String, ExecutedSpecComponent> {
        return when (value) {
            is Either.Left -> value
            is Either.Right -> Either.Right(nestingCallback(value.right))
        }
    }
}