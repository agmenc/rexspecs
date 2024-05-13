package com.rexspecs.fixture

import com.mycompany.fixture.StaffCounter
import com.mycompany.fixture.StaffPivotTable
import com.mycompany.fixture.StaffDatabase
import com.rexspecs.RexSpecs
import com.rexspecs.connectors.DirectConnector
import com.rexspecs.directProps
import com.rexspecs.inputs.SingleHtmlFileInputReader
import com.rexspecs.inputs.sanified
import com.rexspecs.outputs.HtmlFileOutputWriter
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class NestedTablesTest {
    @Test
    fun `Nested tables are processed as input and result data`() {
        RexSpecs.runSuite(
            SingleHtmlFileInputReader("Nested Tables.html", directProps.rexspecsDirectory),
            HtmlFileOutputWriter(directProps.rexspecsDirectory),
            mapOf(
                "Staff Counter" to StaffCounter(),
                "Staff" to StaffDatabase(),
                "Breakdown" to StaffPivotTable()
            ),
            DirectConnector()
        )

        Assertions.assertEquals(
            sanified("src/test/resources/direct_examples/Nested Tables.html"),
            sanified("suites/direct_examples/results/Nested Tables.html")
        )
    }
}