package com.rexspecs.interop

import com.rexspecs.RexSpecs
import com.rexspecs.connectors.Connector
import com.rexspecs.fixture.FixtureRegistry
import com.rexspecs.inputs.SingleHtmlFileInputReader
import com.rexspecs.outputs.HtmlFileOutputWriter
import com.rexspecs.utils.RexSpecProperties
import com.rexspecs.utils.RexSpecPropertiesLoader
import kotlin.reflect.full.createInstance

// TODO: Use ServiceLoader to find all Fixtures
// See example: https://github.com/binkley/kotlin-serviceloader/blob/master/kotlin-serviceloader-sample/src/main/resources/META-INF/services/demo.Foo
fun RexSpecs.Companion.executeSingleHtmlFile(filePath: String, props: RexSpecProperties = RexSpecPropertiesLoader.properties()) {

    // TODO: Split out multiple test suites, based on their various types, as per props files
    runSuite(
        SingleHtmlFileInputReader(filePath, props.rexspecsDirectory),
        HtmlFileOutputWriter(props.rexspecsDirectory),
        magicUp<FixtureRegistry>(props.fixtureRegistry).index(),
        magicUp<Connector>(props.connector)
    )
}

inline fun <reified T> magicUp(fullyQualifiedName: String): T {
    val regClass = Class.forName(fullyQualifiedName).kotlin
    return regClass.createInstance() as T
}