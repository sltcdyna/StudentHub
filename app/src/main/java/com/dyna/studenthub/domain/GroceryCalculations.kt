package com.dyna.studenthub.domain

import com.dyna.studenthub.data.local.Product

object GroceryCalculations {
    fun totalCents(products: List<Product>): Long =
        products.sumOf { it.estimatedPriceCents * it.quantity }

    /** The basket contains products which have not yet been checked off. */
    fun basketTotalCents(products: List<Product>): Long =
        products.filterNot(Product::checked).sumOf { it.estimatedPriceCents * it.quantity }

    fun foodBudgetRemainingCents(
        transactions: List<com.dyna.studenthub.data.local.Transaction>,
        budgetCents: Long = 20_000L,
    ): Long {
        val spent = transactions
            .filter { it.type == com.dyna.studenthub.data.local.TransactionType.EXPENSE && it.category == "Alimentation" }
            .sumOf { it.amountCents }
        return (budgetCents - spent).coerceAtLeast(0)
    }
}
