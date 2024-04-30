package com.mycompany.fixture

import com.rexspecs.Either
import com.rexspecs.ExecutedSpecComponent
import com.rexspecs.assumeLeft
import com.rexspecs.assumeRight
import com.rexspecs.connectors.Connector
import com.rexspecs.connectors.DirectConnector
import com.rexspecs.fixture.Fixture
import com.rexspecs.specs.TabularTest

class StaffCounter : Fixture {
    override fun processRow(
        inputs: Map<String, Either<String, TabularTest>>,
        connector: Connector,
        nestingCallback: (TabularTest) -> ExecutedSpecComponent
    ): List<Either<String, ExecutedSpecComponent>> =
        when (connector) {
            is DirectConnector -> {
                val cock: String = assumeLeft(inputs["Department"])
                val bull: TabularTest = assumeRight(inputs["Staff"])
                val executedSpecComponent: ExecutedSpecComponent = nestingCallback(bull)

                // TODO - This bit next: populate the result table
                val result = interpret(executedSpecComponent)
                val postings = DepartmentPostings(cock, result)
                val breakdown = businessLogic(postings)

                listOf(Either.Left("Monkeys ate my code"))
            }

            else -> throw RuntimeException("Unsupported connector: $connector")
        }
}

class StaffDatabase: Fixture {
    override fun processRow(
        inputs: Map<String, Either<String, TabularTest>>,
        connector: Connector,
        nestingCallback: (TabularTest) -> ExecutedSpecComponent
    ): List<Either<String, ExecutedSpecComponent>> {
        return emptyList()
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

// TODO - Make a companion on StaffDatabase?
fun interpret(executedSpecComponent: ExecutedSpecComponent): List<Pair<String, String>> {
    return emptyList<Pair<String, String>>()
}

class StaffPivotTable: Fixture {
    override fun processRow(
        inputs: Map<String, Either<String, TabularTest>>,
        connector: Connector,
        nestingCallback: (TabularTest) -> ExecutedSpecComponent
    ): List<Either<String, ExecutedSpecComponent>> {
        return emptyList()
    }
}