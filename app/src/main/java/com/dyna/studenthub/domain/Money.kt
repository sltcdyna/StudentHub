package com.dyna.studenthub.domain

import com.dyna.studenthub.data.local.Transaction
import com.dyna.studenthub.data.local.TransactionType
import java.math.BigDecimal
import kotlin.math.roundToLong

object Money {
    fun parseEuroToCents(value: String): Long? = try {
        BigDecimal(value.trim().replace(',', '.'))
            .movePointRight(2)
            .longValueExact()
            .takeIf { it >= 0L }
    } catch (_: NumberFormatException) {
        null
    } catch (_: ArithmeticException) {
        null
    }
}

object MoneyCalculations {
    fun currentMonthTransactions(
        transactions: List<Transaction>,
        nowMillis: Long = System.currentTimeMillis(),
    ): List<Transaction> {
        val start = java.util.Calendar.getInstance().apply {
            timeInMillis = nowMillis
            set(java.util.Calendar.DAY_OF_MONTH, 1)
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val startMillis = start.timeInMillis
        start.add(java.util.Calendar.MONTH, 1)
        val endMillis = start.timeInMillis
        return transactions.filter { it.date >= startMillis && it.date < endMillis }
    }

    fun balance(transactions: List<Transaction>): Long =
        transactions.sumOf { if (it.type == TransactionType.REVENUE) it.amountCents else -it.amountCents }

    fun remainingWeeksInMonth(nowMillis: Long = System.currentTimeMillis()): Double {
        val cal = java.util.Calendar.getInstance().apply { timeInMillis = nowMillis }
        val dayOfMonth = cal.get(java.util.Calendar.DAY_OF_MONTH)
        val maxDays = cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
        val remainingDays = (maxDays - dayOfMonth + 1).coerceAtLeast(1)
        return (remainingDays / 7.0).coerceAtLeast(1.0)
    }

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
