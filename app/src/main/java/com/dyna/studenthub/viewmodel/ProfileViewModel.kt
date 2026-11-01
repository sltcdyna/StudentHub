package com.dyna.studenthub.viewmodel

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dyna.studenthub.data.local.Product
import com.dyna.studenthub.data.local.ProductRepository
import com.dyna.studenthub.data.local.Task
import com.dyna.studenthub.data.local.TaskRepository
import com.dyna.studenthub.data.local.TaskType
import com.dyna.studenthub.data.local.Transaction
import com.dyna.studenthub.data.local.TransactionRepository
import com.dyna.studenthub.data.local.TransactionType
import com.dyna.studenthub.domain.ProfilePreferences
import com.dyna.studenthub.domain.ProfileValidation
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProfileUiState(
    val firstName: String = "",
    val savingsGoalCents: Long = 20_000L,
    val limits: Map<String, Long> = ProfilePreferences.defaultLimits,
    val remindersEnabled: Boolean = false,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val preferences: DataStore<Preferences>,
    private val transactions: TransactionRepository,
    private val products: ProductRepository,
    private val tasks: TaskRepository,
) : ViewModel() {
    val uiState: StateFlow<ProfileUiState> = preferences.data.map { prefs ->
        ProfileUiState(
            firstName = prefs[ProfilePreferences.firstName].orEmpty(),
            savingsGoalCents = prefs[ProfilePreferences.savingsGoalCents] ?: 20_000L,
            limits = ProfilePreferences.defaultLimits.mapValues { (category, default) ->
                prefs[ProfilePreferences.limits.getValue(category)] ?: default
            },
            remindersEnabled = prefs[ProfilePreferences.remindersEnabled] ?: false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())

    fun saveFirstName(value: String) = viewModelScope.launch {
        val name = value.trim()
        if (name.isNotEmpty()) preferences.edit { it[ProfilePreferences.firstName] = name }
    }

    fun saveSavingsGoal(value: String): Boolean {
        val cents = ProfileValidation.parseCents(value) ?: return false
        viewModelScope.launch { preferences.edit { it[ProfilePreferences.savingsGoalCents] = cents } }
        return true
    }

    fun saveLimit(category: String, value: String): Boolean {
        val key = ProfilePreferences.limits[category] ?: return false
        val cents = ProfileValidation.parseCents(value) ?: return false
        viewModelScope.launch { preferences.edit { it[key] = cents } }
        return true
    }

    fun setRemindersEnabled(enabled: Boolean) = viewModelScope.launch {
        preferences.edit { it[ProfilePreferences.remindersEnabled] = enabled }
    }

    fun resetDemoData() = viewModelScope.launch(Dispatchers.IO) {
        transactions.deleteAll()
        products.deleteAll()
        tasks.deleteAll()
        val now = System.currentTimeMillis()
        transactions.insertAll(
            listOf(
                Transaction(type = TransactionType.REVENUE, category = "Salaire", label = "Salaire", amountCents = 95_000L, date = now),
                Transaction(type = TransactionType.REVENUE, category = "APL / Aides", label = "APL", amountCents = 20_000L, date = now),
                Transaction(type = TransactionType.REVENUE, category = "Autres revenus", label = "Autres revenus", amountCents = 5_000L, date = now),
                Transaction(type = TransactionType.EXPENSE, category = "Logement", label = "Logement", amountCents = 52_000L, date = now),
                Transaction(type = TransactionType.EXPENSE, category = "Transport", label = "Transport", amountCents = 7_000L, date = now),
                Transaction(type = TransactionType.EXPENSE, category = "Alimentation", label = "Alimentation", amountCents = 18_000L, date = now),
                Transaction(type = TransactionType.EXPENSE, category = "Loisirs", label = "Loisirs", amountCents = 12_000L, date = now),
                Transaction(type = TransactionType.EXPENSE, category = "Autres", label = "Autres", amountCents = 6_300L, date = now),
            ),
        )
        products.insert(Product(name = "Pâtes", quantity = 2, estimatedPriceCents = 250L, checked = false))
        tasks.insert(Task(type = TaskType.HOMEWORK, subject = "Mathématiques", title = "Série d'exercices", dueDate = now + TimeUnit.DAYS.toMillis(2), done = false))
        tasks.insert(Task(type = TaskType.EXAM, subject = "Droit", title = "Réviser le partiel", dueDate = now + TimeUnit.DAYS.toMillis(5), done = false))
        tasks.insert(Task(type = TaskType.HOMEWORK, subject = "Anglais", title = "Présentation orale", dueDate = now + TimeUnit.DAYS.toMillis(8), done = false))
        preferences.edit {
            it[ProfilePreferences.savingsGoalCents] = 20_000L
            it[ProfilePreferences.remindersEnabled] = false
            ProfilePreferences.defaultLimits.forEach { (category, value) ->
                it[ProfilePreferences.limits.getValue(category)] = value
            }
        }
    }
}
