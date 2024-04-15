package com.rexspecs.fixture

import com.rexspecs.Either
import com.rexspecs.RowResult
import com.rexspecs.connectors.Connector
import com.rexspecs.specs.TabularTest

interface Fixture {
    fun processRow(inputs: Map<String, Either<String, TabularTest>>, connector: Connector): RowResult
}

abstract class FixtureRegistry(private vararg val fixtures: Pair<String, Fixture>) {
    fun index(): Map<String, Fixture> = fixtures.toMap()
}
