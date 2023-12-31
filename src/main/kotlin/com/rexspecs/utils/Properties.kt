package com.rexspecs.utils

import java.net.URL
import java.util.*

// This parent class is so that we can easily test some corner cases, e.g. missing files, props, etc
abstract class PropertiesLoader<T>(private val propsFilePath: String) {
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

    private fun printClassPath() {
        val resource: URL? = this::class.java.classLoader.getResource("rexspec.props")
        println("Path to resource: $resource")
    }
}

object RexSpecPropertiesLoader: PropertiesLoader<RexSpecProperties>("rexspec.props") {
    override fun buildProps(): RexSpecProperties = RexSpecProperties(
        prop("rexspecs.directory", "rexspecs/"),
        prop("host", "http://localhost"),
        prop("port", "80").toInt()
    )
}

data class RexSpecProperties(val targetPath: String, val host: String, val port: Int)