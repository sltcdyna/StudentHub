package com.dyna.studenthub.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dyna.studenthub.data.local.Transaction
import com.dyna.studenthub.data.local.TransactionRepository
import com.dyna.studenthub.data.local.TransactionType
import com.dyna.studenthub.domain.MoneyCalculations
import com.dyna.studenthub.domain.ProfilePreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MoneyUiState(
    val transactions: List<Transaction> = emptyList(),
    val balanceCents: Long = 0,
    val weeklyBudgetCents: Long = 0,
    val exceededCategories: Set<String> = emptySet(),
    val categoryTotals: Map<String, Long> = emptyMap(),
)

@HiltViewModel
class MoneyViewModel @Inject constructor(
    private val repository: TransactionRepository,
    preferences: DataStore<Preferences>,
) : ViewModel() {
    val uiState: StateFlow<MoneyUiState> = kotlinx.coroutines.flow.combine(repository.observeAll(), preferences.data) { values, prefs ->
        val expenses = values.filter { it.type == TransactionType.EXPENSE }
        val limits = ProfilePreferences.defaultLimits.mapValues { (category, default) ->
            prefs[ProfilePreferences.limits.getValue(category)] ?: default
        }
        MoneyUiState(
            transactions = values,
            balanceCents = MoneyCalculations.balance(values),
            weeklyBudgetCents = MoneyCalculations.weeklyBudget(MoneyCalculations.balance(values)),
            exceededCategories = MoneyCalculations.exceededCategories(expenses, limits),
            categoryTotals = expenses.groupBy { it.category }.mapValues { it.value.sumOf(Transaction::amountCents) },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MoneyUiState())

    fun addTransaction(type: TransactionType, category: String, label: String, amountCents: Long, date: Long) {
        if (category.isBlank() || label.isBlank() || amountCents <= 0L || date <= 0L) return
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(Transaction(type = type, category = category, label = label, amountCents = amountCents, date = date))
        }
    }

    companion object {
        val DEFAULT_LIMITS = ProfilePreferences.defaultLimits
    }
}
