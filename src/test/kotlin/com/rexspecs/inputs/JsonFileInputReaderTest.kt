package com.rexspecs.inputs

import com.rexspecs.TabularTest
import com.rexspecs.specs.httpCalculationTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class JsonFileInputReaderTest {
    @Test
    fun `Can convert JSON String to TabularTest`() {
        val someJson = """
            {
                "fixtureName": "Calculator",
                "columnNames": ["First Param", "Operator", "Second Param", "HTTP Response", "Result"],
                "testRows": [
                    { "inputParams": ["7", "+", "8"], "expectedResult": { "resultValues": ["200", "15"] }},
                    { "inputParams": ["7", "x", "8"], "expectedResult": { "resultValues": ["201", "56"] }}
                ]
            }
        """.trimIndent()

        // TODO: TabularTest.fromJsonString(...) in com.rexspecs.Json
        val row: TabularTest = JsonFileInputReader().convertJsonToTabularTest(someJson)

        assertEquals(httpCalculationTest, row)
    }
}