package com.dyna.studenthub

import com.dyna.studenthub.data.OpenFoodFactsParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OpenFoodFactsParserTest {
    @Test fun validResponseReturnsFrenchName() {
        assertEquals(
            "Biscuit chocolat",
            OpenFoodFactsParser.productName("""{"product":{"product_name_fr":"Biscuit chocolat"}}"""),
        )
    }

    @Test fun unknownProductReturnsNull() {
        assertNull(OpenFoodFactsParser.productName("""{"status":0,"product":{}}"""))
    }

    @Test fun emptyResponseReturnsNull() {
        assertNull(OpenFoodFactsParser.productName(""))
    }
}
