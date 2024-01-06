package com.rexspecs.interop

import com.rexspecs.RexSpecs
import com.rexspecs.connectors.DirectConnector
import com.rexspecs.fixture.FixtureRegistry
import com.rexspecs.inputs.SingleHtmlFileInputReader
import com.rexspecs.outputs.HtmlFileOutputWriter
import com.rexspecs.utils.RexSpecPropertiesLoader
import kotlin.reflect.full.createInstance

fun RexSpecs.Companion.executeSingleHtmlFile(filePath: String) {
    val props = RexSpecPropertiesLoader.properties()

    // TODO: source FixtureRegistry implementation from props
    val regClass = Class.forName("com.mycompany.fixture.MyFixtureRegistry").kotlin

    val registry: FixtureRegistry = regClass.createInstance() as FixtureRegistry

    runSuite(
        SingleHtmlFileInputReader(filePath),
        HtmlFileOutputWriter(props.targetPath),
        registry.index(),

        // TODO source Connector implementation from props
        DirectConnector()
    )
}