package com.dyna.studenthub

import com.dyna.studenthub.data.local.Product
import com.dyna.studenthub.domain.GroceryCalculations
import org.junit.Assert.assertEquals
import org.junit.Test

class GroceryCalculationsTest {
    private fun product(name: String, quantity: Int, cents: Long, checked: Boolean = false) =
        Product(name = name, quantity = quantity, estimatedPriceCents = cents, checked = checked)

    @Test fun totalAfterAddingProducts() {
        assertEquals(850L, GroceryCalculations.basketTotalCents(listOf(product("Lait", 2, 250), product("Pain", 1, 350))))
    }

    @Test fun totalAfterDeletingProduct() {
        assertEquals(500L, GroceryCalculations.basketTotalCents(listOf(product("Lait", 2, 250))))
    }

    @Test fun checkedProductIsRemovedFromBasketTotal() {
        assertEquals(
            500L,
            GroceryCalculations.basketTotalCents(
                listOf(
                    product("Lait", 2, 250),
                    product("Pain", 1, 350, checked = true),
                ),
            ),
        )
    }
}
