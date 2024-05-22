package com.rexspecs.fixture

import com.rexspecs.utils.Either
import com.rexspecs.ExecutedSpecComponent
import com.rexspecs.RowDescriptor
import com.rexspecs.connectors.Connector
import com.rexspecs.specs.TabularTest
import com.rexspecs.utils.assumeRight

interface Fixture {
    fun execute(
        rowDescriptor: RowDescriptor,
        connector: Connector,
        expectedColumnValues: Map<String, Either<String, TabularTest>>
    ): Map<String, Either<String, ExecutedSpecComponent>>
}

fun createNestedTable(
    name: String,
    expectedColumnValues: Map<String, Either<String, TabularTest>>,
    withActualRows: () -> List<Map<String, Either<String, ExecutedSpecComponent>>>
): Map<String, Either<String, ExecutedSpecComponent>> = mapOf(
    name to Either.Right(ExecutedSpecComponent(assumeRight(expectedColumnValues[name]), withActualRows()))
)

abstract class FixtureRegistry(private vararg val fixtures: Pair<String, Fixture>) {
    fun index(): Map<String, Fixture> = fixtures.toMap()
}
