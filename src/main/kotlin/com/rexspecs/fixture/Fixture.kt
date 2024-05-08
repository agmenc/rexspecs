package com.rexspecs.fixture

import com.rexspecs.Either
import com.rexspecs.ExecutedSpecComponent
import com.rexspecs.RowDescriptor
import com.rexspecs.connectors.Connector
import com.rexspecs.specs.TabularTest

interface Fixture {
    fun processInput(
        columnName: String,
        value: Either<String, TabularTest>,
        connector: Connector,
        nestingCallback: (TabularTest) -> ExecutedSpecComponent
    ): Either<String, ExecutedSpecComponent> {
        return when (value) {
            is Either.Left -> value
            is Either.Right -> Either.Right(nestingCallback(value.right))
        }
    }

    fun processResult(
        columnName: String,
        value: Either<String, TabularTest>,
        connector: Connector,
        nestingCallback: (TabularTest) -> ExecutedSpecComponent,
        rowDescriptor: RowDescriptor
    ): Either<String, ExecutedSpecComponent>

    fun execute(rowDescriptor: RowDescriptor, connector: Connector): Map<String, Either<String, ExecutedSpecComponent>>
}

abstract class FixtureRegistry(private vararg val fixtures: Pair<String, Fixture>) {
    fun index(): Map<String, Fixture> = fixtures.toMap()
}
