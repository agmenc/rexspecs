package com.rexspecs.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

internal class PropertiesTest {

    @Test
    fun `Can load the target path from a props file in the test resources directory`() {
        assertEquals("suites/http_examples", RexSpecPropertiesLoader.properties().rexspecsDirectory)
    }

    @Test
    fun `We get default values when the props file is missing`() {
        assertEquals("George", loadMeUp("missing.props").properties().firstName)
    }

    @Test
    fun `We get a mix of default values and real values when the props file is sparsely populated`() {
        assertEquals("George", loadMeUp("sparse.props").properties().firstName)
        assertEquals("Elsa's Kopje", loadMeUp("sparse.props").properties().favouriteHolidayDestination)
    }

    @Test
    fun `Can find props in silly places`() {
        assertEquals("Zendaya", loadMeUp("http_examples/secret/nested.props").properties().firstName)
    }

    data class SomeOtherProperties(val firstName: String, val favouriteHolidayDestination: String)

    private fun loadMeUp(propsFilePath: String): PropertiesLoader<SomeOtherProperties> {
        class SomeOtherPropertiesLoader : PropertiesLoader<SomeOtherProperties>(propsFilePath) {
            override fun buildProps(): SomeOtherProperties = SomeOtherProperties(
                prop("first.name", "George"),
                prop("favourite.holiday.destination", "Butlins")
            )
        }

        return SomeOtherPropertiesLoader()
    }

    @Test
    @Disabled
    fun `Load props from the environment`() { TODO("This thing") }

}