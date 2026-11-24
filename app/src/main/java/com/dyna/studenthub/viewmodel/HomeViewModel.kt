package com.dyna.studenthub.viewmodel

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dyna.studenthub.data.local.Product
import com.dyna.studenthub.data.local.ProductRepository
import com.dyna.studenthub.data.local.Task
import com.dyna.studenthub.data.local.TaskRepository
import com.dyna.studenthub.data.local.Transaction
import com.dyna.studenthub.data.local.TransactionRepository
import com.dyna.studenthub.data.local.TransactionType
import com.dyna.studenthub.domain.CompanionMood
import com.dyna.studenthub.domain.HomeCalculations
import com.dyna.studenthub.domain.MoneyCalculations
import com.dyna.studenthub.domain.ProfilePreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val loading: Boolean = true,
    val firstName: String = "",
    val savingsGoalCents: Long = 20_000L,
    val balanceCents: Long = 0L,
    val foodBudgetRemainingCents: Long = 20_000L,
    val upcomingTasks: List<Task> = emptyList(),
    val exceededCategories: Set<String> = emptySet(),
    val products: List<Product> = emptyList(),
    val companionMood: CompanionMood = CompanionMood.DEFAULT,
    val companionMessage: String = CompanionMood.DEFAULT.message,
    val companionId: String = "cat",
    val companionName: String = "Mochi",
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    transactions: TransactionRepository,
    tasks: TaskRepository,
    products: ProductRepository,
    private val preferences: DataStore<Preferences>,
) : ViewModel() {
    private val prefs = preferences.data.map { values ->
        val cId = values[ProfilePreferences.companionId] ?: "cat"
        val cAnimal = com.dyna.studenthub.domain.CompanionAnimal.fromId(cId)
        val cName = values[ProfilePreferences.companionName] ?: if (cAnimal == com.dyna.studenthub.domain.CompanionAnimal.CAT) "Mochi" else cAnimal.label
        HomePreferences(
            values[ProfilePreferences.firstName].orEmpty(),
            values[ProfilePreferences.savingsGoalCents] ?: 20_000L,
            cId,
            cName,
            values[ProfilePreferences.lastTaskCompletedAt] ?: 0L,
            ProfilePreferences.defaultLimits.mapValues { (category, default) ->
                values[ProfilePreferences.limits.getValue(category)] ?: default
            },
        )
    }
    val uiState: StateFlow<HomeUiState> = combine(
        transactions.observeAll(), tasks.observeAll(), products.observeAll(), prefs
    ) { values, taskValues, productValues, saved ->
        val monthTransactions = MoneyCalculations.currentMonthTransactions(values)
        val expenses = monthTransactions.filter { it.type == TransactionType.EXPENSE }
        val balance = MoneyCalculations.balance(monthTransactions)
        val exceeded = MoneyCalculations.exceededCategories(expenses, saved.limits)
        val goal = saved.savingsGoalCents.coerceAtLeast(0L)
        val baseMood = HomeCalculations.companionMood(
            hasExceededCategory = exceeded.isNotEmpty(),
            balanceCents = balance,
            savingsGoalCents = goal
        )
        val celebratedTask = System.currentTimeMillis() - saved.lastTaskCompletedAt in 0L..60_000L
        val mood = if (celebratedTask) CompanionMood.HAPPY else baseMood
        HomeUiState(
            loading = false,
            firstName = saved.firstName,
            savingsGoalCents = goal,
            balanceCents = balance,
            foodBudgetRemainingCents = ((saved.limits["Alimentation"] ?: ProfilePreferences.defaultLimits.getValue("Alimentation")) -
                expenses.filter { it.category == "Alimentation" }.sumOf(Transaction::amountCents) -
                productValues.sumOf { it.estimatedPriceCents * it.quantity }).coerceAtLeast(0L),
            upcomingTasks = HomeCalculations.upcomingTasks(taskValues, System.currentTimeMillis()),
            exceededCategories = exceeded,
            products = productValues,
            companionMood = mood,
            companionMessage = if (celebratedTask) "Bravo, ${saved.companionName} est fier de toi !" else mood.message,
            companionId = saved.companionId,
            companionName = saved.companionName,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    private data class HomePreferences(
        val firstName: String,
        val savingsGoalCents: Long,
        val companionId: String,
        val companionName: String,
        val lastTaskCompletedAt: Long,
        val limits: Map<String, Long>,
    )

    fun saveFirstName(value: String) = viewModelScope.launch { preferences.edit { it[ProfilePreferences.firstName] = value.trim() } }
    fun saveSavingsGoalCents(value: Long) = viewModelScope.launch { preferences.edit { it[ProfilePreferences.savingsGoalCents] = value.coerceAtLeast(0L) } }

    companion object {
        val FIRST_NAME = ProfilePreferences.firstName
        val SAVINGS_GOAL = ProfilePreferences.savingsGoalCents
    }
}
