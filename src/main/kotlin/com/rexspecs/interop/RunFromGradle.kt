package com.rexspecs.interop

import com.rexspecs.RexSpecs
import com.rexspecs.connectors.Connector
import com.rexspecs.fixture.FixtureRegistry
import com.rexspecs.inputs.HtmlFileInputReader
import com.rexspecs.outputs.HtmlFileOutputWriter
import com.rexspecs.utils.RexSpecProperties
import com.rexspecs.utils.RexSpecPropertiesLoader

fun RexSpecs.Companion.executeSuiteHtml(props: RexSpecProperties = RexSpecPropertiesLoader.properties()) {
    runSuite(
        HtmlFileInputReader(props.rexspecsDirectory),
        HtmlFileOutputWriter(props.rexspecsDirectory),
        magicUp<FixtureRegistry>(props.fixtureRegistry).index(),
        magicUp<Connector>(props.connector)
    )
}
