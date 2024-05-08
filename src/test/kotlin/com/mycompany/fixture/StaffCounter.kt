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

    override fun execute(rowDescriptor: RowDescriptor, connector: Connector): Any {
        val dept = assumeLeft(rowDescriptor.inputResults["Department"])
        rowDescriptor.inputResults["Staff"]?.let { staffDB: Either<String, ExecutedSpecComponent> ->
            val staffDbStrings: ExecutedSpecComponent = assumeRight(staffDB)
            val listList = staffDbStrings.actualRowResults.map { row ->
                val (x, y) = row.map { assumeLeft(it) }
                Pair(x, y)
            }

            val postings = DepartmentPostings(dept, listList)

            return calculateDepartmentBreakdown(postings)
        }

        // TODO - Better null check above, so this can die
        return "Monkeys ate it again"
    }
}

class StaffDatabase: Fixture {

    override fun execute(rowDescriptor: RowDescriptor, connector: Connector): Any {
        TODO("Not required - input only")
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

data class DepartmentPostings(val department: String, val staffRoles: List<Pair<String, String>>)
data class DepartmentBreakdown(val department: String, val staffTally: Map<String, Int>)

fun calculateDepartmentBreakdown(departmentPostings: DepartmentPostings): DepartmentBreakdown =
    with (departmentPostings) {
        return staffRoles.fold(DepartmentBreakdown(department, emptyMap())) { acc, (role: String, _: String) ->
            acc.staffTally[role]?.let {
                acc.copy(staffTally = acc.staffTally.map { (k: String, v: Int)  ->
                    if (k == role) Pair(k, v+1) else Pair(k, v)
                }.toMap())
            } ?: acc
        }
    }

class StaffPivotTable: Fixture {

    override fun execute(rowDescriptor: RowDescriptor, connector: Connector): Any {
//        println("rowDescriptor = ${rowDescriptor}")
        return "StaffPivotTable.execute"
    }

    override fun processResult(
        columnName: String,
        value: Either<String, TabularTest>,
        connector: Connector,
        nestingCallback: (TabularTest) -> ExecutedSpecComponent,
        rowDescriptor: RowDescriptor
    ): Either<String, ExecutedSpecComponent> {
        return Either.Left("StaffPivotTable.processResult")
    }
}