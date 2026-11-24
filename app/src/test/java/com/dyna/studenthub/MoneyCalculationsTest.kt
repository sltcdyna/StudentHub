package com.dyna.studenthub

import com.dyna.studenthub.data.local.Transaction
import com.dyna.studenthub.data.local.TransactionType
import com.dyna.studenthub.domain.Money
import com.dyna.studenthub.domain.MoneyCalculations
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MoneyCalculationsTest {
    private fun tx(type: TransactionType, amount: Long, category: String = "Alimentation") =
        Transaction(type = type, category = category, label = "test", amountCents = amount, date = 0)

    @Test fun balanceSubtractsExpenses() {
        assertEquals(7_500L, MoneyCalculations.balance(listOf(tx(TransactionType.REVENUE, 10_000), tx(TransactionType.EXPENSE, 2_500))))
    }

    @Test fun weeklyBudgetUsesFourPointThreeFourFiveWeeks() {
        assertEquals(2_301L, MoneyCalculations.weeklyBudget(10_000))
    }

    @Test fun alertStartsAtEightyPercentOfLimit() {
        assertTrue("Alimentation" in MoneyCalculations.exceededCategories(listOf(tx(TransactionType.EXPENSE, 8_000)), mapOf("Alimentation" to 10_000)))
    }

    @Test fun remainingWeeksInMonthReturnsPositiveValue() {
        assertTrue(MoneyCalculations.remainingWeeksInMonth() >= 1.0)
    }

    @Test fun euroAmountsAreConvertedToExactCents() {
        assertEquals(115L, Money.parseEuroToCents("1,15"))
        assertEquals(1L, Money.parseEuroToCents("0.01"))
    }

    @Test fun amountsThatCannotBeRepresentedInCentsAreRejected() {
        assertNull(Money.parseEuroToCents("1.001"))
        assertNull(Money.parseEuroToCents("-1"))
        assertNull(Money.parseEuroToCents("999999999999999999999999"))
    }
}
