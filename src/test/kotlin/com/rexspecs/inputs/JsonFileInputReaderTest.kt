package com.rexspecs.inputs

import com.rexspecs.specs.Spec
import com.rexspecs.specs.TabularTest
import com.rexspecs.specs.Title
import com.rexspecs.specs.httpCalculationTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class JsonFileInputReaderTest {
    @Test
    fun `Can convert JSON String to TabularTest`() {
        val someJson = """
            {
                "fixtureName": "Calculator",
                "inputColumns": ["First Param", "Operator", "Second Param"],
                "expectationColumns": ["HTTP Response", "Result"],
                "testRows": [
                    { "inputParams": ["7", "+", "8"], "expectedResult": { "resultValues": ["200", "15"] }},
                    { "inputParams": ["7", "x", "8"], "expectedResult": { "resultValues": ["201", "56"] }}
                ]
            }
        """.trimIndent()

        // TODO: TabularTest.fromJsonString(...) in com.rexspecs.Json
        val row: TabularTest = JsonFileInputReader("rexspecs").convertJsonToTabularTest(someJson)

        assertEquals(httpCalculationTest, row)
    }

    private val rawJson = """
        {
          "identifier": "JsonExample.monkeys",
          "components": [
            {
              "type": "com.rexspecs.specs.Title",
              "title": "An Acceptance Test"
            },
            {
              "type": "com.rexspecs.TabularTest",
              "fixtureName": "Calculator",
              "inputColumns": ["First Param", "Operator", "Second Param"],
              "expectationColumns": ["HTTP Response", "Result"],
              "testRows": [
                { "inputParams": ["7", "+", "8"], "expectedResult": {"resultValues": ["200", "15"]} },
                { "inputParams": ["7", "x", "8"], "expectedResult": {"resultValues": ["201", "56"]} }
              ]
            }
          ]
        }
    """.trimIndent()

    private val minifiedRawJson = """{"identifier":"JsonExample.monkeys","components":[{"type":"com.rexspecs.specs.Title","title":"An Acceptance Test"},{"type":"com.rexspecs.TabularTest","fixtureName":"Calculator","inputColumns":["First Param","Operator","Second Param"],"expectationColumns":["HTTP Response","Result"],"testRows":[{"inputParams":["7","+","8"],"expectedResult":{"resultValues":["200","15"]}},{"inputParams":["7","x","8"],"expectedResult":{"resultValues":["201","56"]}}]}]}"""

    @Test
    fun `Specs are Serialisable and Deserialisable`() {
        val spec = Spec("JsonExample.monkeys", listOf(Title("An Acceptance Test"), httpCalculationTest))

        val deserialised = Json.decodeFromString<Spec>(rawJson)
        assertEquals(spec, deserialised)

        val serialised = Json.encodeToString(spec)
        assertEquals(minifiedRawJson, serialised)
    }
}
