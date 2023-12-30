package com.rexspecs.specs

import com.rexspecs.htmlToTables
import com.rexspecs.outputs.convertTableToTest
import org.jsoup.Jsoup

interface Spec {
    fun components(): List<SpecComponent>

    @Deprecated("Should just be another component")
    val title: String

    @Deprecated("Iterate through the components() instead")
    fun guts(): String
}

data class HackyHtmlSpec(val innards: String): Spec {
    // TODO: Use the InputReader to create the Spec with a List<SpecComponent>
    override fun components(): List<SpecComponent> {
        return htmlToTables(Jsoup.parse(guts()))
            .map { convertTableToTest(it) }
    }

    @Deprecated("Should just be another component")
    override val title: String = "Monkeys"

    @Deprecated("Iterate through the components() instead")
    override fun guts(): String {
        return innards
    }
}

interface SpecComponent {
}

