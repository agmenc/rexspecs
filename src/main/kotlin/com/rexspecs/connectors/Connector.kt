package com.rexspecs.connectors

import com.rexspecs.ExecutedSpecComponent
import com.rexspecs.specs.SpecComponent

typealias Connector = (request: SpecComponent) -> ExecutedSpecComponent