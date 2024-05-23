package com.rexspecs.fixture

import com.rexspecs.utils.Either
import com.rexspecs.ExecutedSpecComponent
import com.rexspecs.RowDescriptor
import com.rexspecs.connectors.Connector
import com.rexspecs.specs.TabularTest
import com.rexspecs.utils.assumeLeft
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

fun <T> Map<String, Either<String, ExecutedSpecComponent>>.extract(
    paramName: String,
    extractor: (String) -> T
): T? {
    // TODO - Safeify this by removing assumeLeft
    return extractor(assumeLeft(this[paramName]))
}

fun missingFieldError(
    expectationColumn: String,
    missingFieldColumn: String
): Map<String, Either<String, ExecutedSpecComponent>> =
    mapOf(
        expectationColumn to Either.Left("Missing input field: $missingFieldColumn")
    )

abstract class FixtureRegistry(private vararg val fixtures: Pair<String, Fixture>) {
    fun index(): Map<String, Fixture> = fixtures.toMap()
}
