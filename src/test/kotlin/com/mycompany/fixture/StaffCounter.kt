package com.mycompany.fixture

import com.rexspecs.*
import com.rexspecs.connectors.Connector
import com.rexspecs.fixture.Fixture
import com.rexspecs.specs.TabularTest

class StaffCounter : Fixture {

    override fun processInput(
        columnName: String,
        value: Either<String, TabularTest>,
        connector: Connector,
        nestingCallback: (TabularTest) -> ExecutedSpecComponent
    ): Either<String, ExecutedSpecComponent> {
        when (value) {
            is Either.Left -> {
                return value
            }
            is Either.Right -> {
                return Either.Right(nestingCallback(value.right))
            }
        }
    }

    override fun processResult(
        columnName: String,
        value: Either<String, TabularTest>,
        connector: Connector,
        nestingCallback: (TabularTest) -> ExecutedSpecComponent,
        rowDescriptor: RowDescriptor
    ): Either<String, ExecutedSpecComponent> {
        when (value) {
            is Either.Left -> {
                return value
            }
            is Either.Right -> {
                return Either.Right(nestingCallback(value.right))
            }
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
            val breakdown: DepartmentBreakdown = businessLogic(postings)

            return breakdown
        }

        return "Monkeys ate it again"
    }
}

class StaffDatabase: Fixture {

    override fun processInput(
        columnName: String,
        value: Either<String, TabularTest>,
        connector: Connector,
        nestingCallback: (TabularTest) -> ExecutedSpecComponent
    ): Either<String, ExecutedSpecComponent> {
        when (value) {
            is Either.Left -> {
                return value
            }
            is Either.Right -> {
                return Either.Right(nestingCallback(value.right))
            }
        }
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

    override fun execute(rowDescriptor: RowDescriptor, connector: Connector): Any {
        TODO("Not required - input only")
    }
}

data class DepartmentPostings(val department: String, val staffRoles: List<Pair<String, String>>)
data class DepartmentBreakdown(val department: String, val staffTally: Map<String, Int>)

fun businessLogic(departmentPostings: DepartmentPostings): DepartmentBreakdown {
    with (departmentPostings) {
        return staffRoles.fold(DepartmentBreakdown(department, emptyMap())) { acc, (role: String, _: String) ->
            acc.staffTally[role]?.let {
                acc.copy(staffTally = acc.staffTally.map { (k: String, v: Int)  ->
                    if (k == role) Pair(k, v+1) else Pair(k, v)
                }.toMap())
            } ?: acc
        }
    }
}

class StaffPivotTable: Fixture {

    override fun processInput(
        columnName: String,
        value: Either<String, TabularTest>,
        connector: Connector,
        nestingCallback: (TabularTest) -> ExecutedSpecComponent
    ): Either<String, ExecutedSpecComponent> {
        when (value) {
            is Either.Left -> {
                return value
            }
            is Either.Right -> {
                return Either.Right(nestingCallback(value.right))
            }
        }
    }

    override fun execute(rowDescriptor: RowDescriptor, connector: Connector): Any {
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