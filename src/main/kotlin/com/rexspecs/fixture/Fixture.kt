package com.rexspecs.fixture

import com.rexspecs.RowResult
import com.rexspecs.connectors.Connector

interface Fixture {
    fun processRow(inputs: Map<String, String>, connector: Connector): RowResult
}

abstract class FixtureRegistry(private vararg val fixtures: Pair<String, Fixture>) {
    fun index(): Map<String, Fixture> = fixtures.toMap()
}
