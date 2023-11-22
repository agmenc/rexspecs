package com.rexspec

import org.jsoup.Jsoup
import java.io.File
import java.net.URL
import java.util.*

fun fileAsString(filePath: String) = File(filePath).readText()

fun writeFile(what: String, where: String) = File(where).writeText(what)

fun htmlSanitised(contents: String): String = Jsoup.parse(contents).outerHtml()

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
    override fun buildProps(): RexSpecProperties = RexSpecProperties(prop("target.path", "rexspecs/"))
}

