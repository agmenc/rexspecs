package com.rexspecs.specs

interface Spec {
    fun components(): List<SpecComponent>

    @Deprecated("Should just be another component")
    val title: String

    @Deprecated("Iterate through the components() instead")
    fun guts(): String
}

data class HackyHtmlSpec(val innards: String, val compies: List<SpecComponent>): Spec {
    override fun components(): List<SpecComponent> = compies

    @Deprecated("Should just be another component")
    override val title: String = "Monkeys"

    @Deprecated("Iterate through the components() instead")
    override fun guts(): String {
        return innards
    }
}

interface SpecComponent {
}

