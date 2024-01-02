package com.rexspecs.inputs

import com.rexspecs.RowResult
import com.rexspecs.TabularTest
import com.rexspecs.TestRow
import com.rexspecs.specs.Spec
import kotlinx.serialization.json.Json

class JsonFileInputReader: InputReader {
    override fun specs(): List<Spec> {
        return listOf(
            Spec(
                "test",
                listOf(
                    TabularTest(
                        "test",
                        listOf("col1", "col2", "col3"),
                        listOf(
                            TestRow(
                                listOf("test", "test", "test"),
                                RowResult.from("test", "test", "test")
                            )
                        )
                    )
                )
            )
        )
    }

    fun convertJsonToTabularTest(json: String): TabularTest {
        return Json.decodeFromString<TabularTest>(json)
    }

}