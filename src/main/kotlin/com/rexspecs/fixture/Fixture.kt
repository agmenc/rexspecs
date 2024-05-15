package com.rexspecs.fixture

import com.rexspecs.Either
import com.rexspecs.ExecutedSpecComponent
import com.rexspecs.RowDescriptor
import com.rexspecs.connectors.Connector
import com.rexspecs.specs.TabularTest

interface Fixture {
    fun execute(
        rowDescriptor: RowDescriptor,
        connector: Connector,
        columnValues: Map<String, Either<String, TabularTest>>
    ): Map<String, Either<String, ExecutedSpecComponent>>
}

abstract class FixtureRegistry(private vararg val fixtures: Pair<String, Fixture>) {
    fun index(): Map<String, Fixture> = fixtures.toMap()
}
