@file:UseSerializers(EitherSerializer::class)
package com.rexspecs.inputs

import com.rexspecs.InvalidStartingState
import com.rexspecs.specs.Spec
import com.rexspecs.specs.TabularTest
import com.rexspecs.specs.Title
import com.rexspecs.specs.httpCalculationTest
import com.rexspecs.utils.EitherSerializer
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.io.path.pathString

class JsonFileInputReaderTest {
    @Test
    fun `Can convert JSON String to TabularTest`() {
        val someJson = """
            {
                "fixtureName": "Calculator",
                "inputColumns": ["First Param", "Operator", "Second Param"],
                "expectationColumns": ["HTTP Response", "Result"],
                "testRows": [
                  { 
                    "inputCount": 3,
                    "inputParams": [{ "Left": "7" }, { "Left":"+"}, {"Left":"8"}],
                    "allTheParams":{"First Param":{"Left":"7"},"Operator":{"Left":"+"},"Second Param":{"Left":"8"},"HTTP Response":{"Left":"200"},"Result":{"Left":"15"}}
                  },
                  {
                    "inputCount": 3,
                    "inputParams": [{ "Left": "7" }, { "Left":"x"}, {"Left":"8"}],
                    "allTheParams":{"First Param":{"Left":"7"},"Operator":{"Left":"x"},"Second Param":{"Left":"8"},"HTTP Response":{"Left":"201"},"Result":{"Left":"56"}}
                  }
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
                { 
                  "inputCount": 3,
                  "inputParams": [{ "Left": "7" }, { "Left":"+"}, {"Left":"8"}],
                  "allTheParams":{"First Param":{"Left":"7"},"Operator":{"Left":"+"},"Second Param":{"Left":"8"},"HTTP Response":{"Left":"200"},"Result":{"Left":"15"}}
                },
                {
                  "inputCount": 3,
                  "inputParams": [{ "Left": "7" }, { "Left":"x"}, {"Left":"8"}],
                  "allTheParams":{"First Param":{"Left":"7"},"Operator":{"Left":"x"},"Second Param":{"Left":"8"},"HTTP Response":{"Left":"201"},"Result":{"Left":"56"}}
                }
              ]
            }
          ]
        }
    """.trimIndent()

    private val minifiedRawJson = """{"identifier":"JsonExample.monkeys","components":[{"type":"com.rexspecs.specs.Title","title":"An Acceptance Test"},{"type":"com.rexspecs.TabularTest","fixtureName":"Calculator","inputColumns":["First Param","Operator","Second Param"],"expectationColumns":["HTTP Response","Result"],"testRows":[{"inputCount":3,"inputParams":[{"Left":"7"},{"Left":"+"},{"Left":"8"}],"allTheParams":{"First Param":{"Left":"7"},"Operator":{"Left":"+"},"Second Param":{"Left":"8"},"HTTP Response":{"Left":"200"},"Result":{"Left":"15"}}},{"inputCount":3,"inputParams":[{"Left":"7"},{"Left":"x"},{"Left":"8"}],"allTheParams":{"First Param":{"Left":"7"},"Operator":{"Left":"x"},"Second Param":{"Left":"8"},"HTTP Response":{"Left":"201"},"Result":{"Left":"56"}}}]}]}"""

    @Test
    fun `Specs are Serialisable and Deserialisable`() {
        val spec = Spec("JsonExample.monkeys", listOf(Title("An Acceptance Test"), httpCalculationTest))

        val deserialised = Json.decodeFromString<Spec>(rawJson)
        assertEquals(spec, deserialised)

        val serialised = Json.encodeToString(spec)
        assertEquals(minifiedRawJson, serialised)
    }

    @Test
    fun `DirectoryManager barfs when the source root doesn't exist`() {
        assertThrows<InvalidStartingState>("Cannot find Rexspecs directory [potato]") {
            JsonFileInputReader("potato").prepareForInput()
        }
    }

    @Test
    fun `DirectoryManager barfs when the specs folder doesn't exist`() {
        val tempRexSpecsDir = createTempDirectory().pathString
        Assertions.assertFalse(File(tempRexSpecsDir, "specs").exists())

        assertThrows<InvalidStartingState>("Cannot find Rexspecs source directory [$tempRexSpecsDir/specs]") {
            JsonFileInputReader(tempRexSpecsDir).prepareForInput()
        }
    }
}
