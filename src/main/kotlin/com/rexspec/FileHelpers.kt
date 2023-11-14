package com.rexspec

import java.io.File
import java.net.URL
import java.util.*

// TODO: try File(where).readText() and see if it can see the same resources
//          ==> It uses a different (better) root file location, so need to make that work somehow
// Look for userDir
fun fileAsString(filePath: String) = {}::class.java.getResource(filePath).readText()

fun writeFile(what: String, where: String) {
    println("Writing to file: $where")
    File(where).writeText(what)
}

data class RexSpecProperties(val targetPath: String)

// Extracted a parent class so that we can easily test some corner cases, e.g. missing files, props, etc
abstract class PropertiesLoader<T>(val propsFilePath: String) {
    private val properties = Properties()

    fun prop(key: String, default: String): String = properties.getOrElse(key) { default } as String

    fun properties(): T {
        val maybePropsStream = this::class.java.classLoader.getResourceAsStream(propsFilePath)
        maybePropsStream?.use { properties.apply { load(it) } } ?: debugMissingFile()
        return buildProps()
    }

    abstract fun buildProps(): T

    private fun debugMissingFile() {
        println("RexSpec: no rexspec.props configuration file found in classpath; using defaults")
        printClassPath()
    }

    fun printClassPath() {
        val resource: URL = this::class.java.classLoader.getResource("rexspec.props")
        println("Path to resource: $resource")
    }
}

object RexSpecPropertiesLoader: PropertiesLoader<RexSpecProperties>("rexspec.props") {
    override fun buildProps(): RexSpecProperties = RexSpecProperties(prop("target.path", "../"))
}

