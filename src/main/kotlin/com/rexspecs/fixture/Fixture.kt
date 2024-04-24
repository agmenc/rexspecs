package com.rexspecs.fixture

import com.rexspecs.Either
import com.rexspecs.ExecutedSpecComponent
import com.rexspecs.connectors.Connector
import com.rexspecs.specs.TabularTest

interface Fixture {
    fun processRow(
        inputs: Map<String, Either<String, TabularTest>>,
        connector: Connector,
        nestingCallback: (TabularTest) -> ExecutedSpecComponent
    ): List<Either<String, ExecutedSpecComponent>>
}

abstract class FixtureRegistry(private vararg val fixtures: Pair<String, Fixture>) {
    fun index(): Map<String, Fixture> = fixtures.toMap()
}
