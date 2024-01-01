package com.rexspecs.fixture

import com.rexspecs.RowResult
import org.http4k.core.HttpHandler

interface Fixture {
    fun processRow(inputs: Map<String, String>, connector: HttpHandler): RowResult
    // TODO: fun processRow(input: Map<String, String>, connector: Connector): RowResult
}
