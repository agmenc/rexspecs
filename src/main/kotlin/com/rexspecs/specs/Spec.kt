package com.rexspecs.specs

interface Spec {
    fun components(): Iterator<SpecComponent>

    val title: String

    @Deprecated("Iterate through the components() instead")
    fun guts(): String
}

data class HackyHtmlSpec(val innards: String): Spec {
    override fun components(): Iterator<SpecComponent> {
        return emptyList<SpecComponent>().iterator()
    }

    override val title: String
        get() = "An Acceptance Test"

    @Deprecated("Iterate through the components() instead")
    override fun guts(): String {
        return innards
    }
}

interface SpecComponent {
}