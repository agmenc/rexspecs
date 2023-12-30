package com.rexspecs.specs

interface Spec {
    fun components(): Iterator<SpecComponent>

    @Deprecated("Should just be another component")
    val title: String

    @Deprecated("Iterate through the components() instead")
    fun guts(): String
}

data class HackyHtmlSpec(val innards: String): Spec {
    override fun components(): Iterator<SpecComponent> {
        return emptyList<SpecComponent>().iterator()
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

