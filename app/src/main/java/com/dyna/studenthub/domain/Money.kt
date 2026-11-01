package com.dyna.studenthub.domain

import com.dyna.studenthub.data.local.Transaction
import com.dyna.studenthub.data.local.TransactionType
import kotlin.math.roundToLong

object MoneyCalculations {
    fun balance(transactions: List<Transaction>): Long =
        transactions.sumOf { if (it.type == TransactionType.REVENUE) it.amountCents else -it.amountCents }

    fun weeklyBudget(balanceCents: Long, weeks: Double = 4.345): Long =
        (balanceCents.toDouble() / weeks).roundToLong().coerceAtLeast(0)

    fun categoryUsage(expenses: List<Transaction>, limits: Map<String, Long>): Map<String, Float> =
        expenses.filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .mapValues { (category, values) ->
                val limit = limits[category] ?: 0L
                if (limit <= 0) 0f else values.sumOf { it.amountCents }.toFloat() / limit
            }

    fun exceededCategories(expenses: List<Transaction>, limits: Map<String, Long>): Set<String> =
        categoryUsage(expenses, limits).filterValues { it >= .8f }.keys
}
