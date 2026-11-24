package com.dyna.studenthub.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dyna.studenthub.data.local.TransactionRepository
import com.dyna.studenthub.data.local.TransactionType
import com.dyna.studenthub.domain.MoneyCalculations
import com.dyna.studenthub.domain.ProfilePreferences
import com.dyna.studenthub.data.TheMealDbMeal
import com.dyna.studenthub.data.TheMealDbRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MealsUiState(
    val spentCents: Long = 0,
    val budgetCents: Long = 20_000,
)

data class TheMealDbUiState(
    val query: String = "",
    val meals: List<TheMealDbMeal> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val offlineCache: Boolean = false,
)

data class MealDetailsUiState(
    val meal: TheMealDbMeal? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class MealsViewModel @Inject constructor(
    private val transactions: TransactionRepository,
    private val mealDb: TheMealDbRepository,
    preferences: DataStore<Preferences>,
) : ViewModel() {
    private val mutableMealDbState = kotlinx.coroutines.flow.MutableStateFlow(TheMealDbUiState())
    val mealDbState: StateFlow<TheMealDbUiState> = mutableMealDbState
    private val mutableMealDetailsState = kotlinx.coroutines.flow.MutableStateFlow(MealDetailsUiState())
    val mealDetailsState: StateFlow<MealDetailsUiState> = mutableMealDetailsState
    private var mealSearchJob: kotlinx.coroutines.Job? = null
    private var mealDetailsJob: kotlinx.coroutines.Job? = null

    init {
        browsePopularCategories()
    }

    fun browsePopularCategories() = browseRecipes("Tout") { mealDb.browsePopularCategories() }

    fun browseCategory(label: String, apiCategory: String) =
        browseRecipes(label) { mealDb.browseCategory(apiCategory) }

    private fun browseRecipes(label: String, load: suspend () -> List<TheMealDbMeal>) {
        mealSearchJob?.cancel()
        mealSearchJob = viewModelScope.launch {
            mutableMealDbState.value = TheMealDbUiState(query = label, isLoading = true)
            try {
                val meals = load()
                mutableMealDbState.value = TheMealDbUiState(
                    query = label,
                    meals = meals,
                    errorMessage = if (meals.isEmpty()) "Aucune recette disponible pour cette catégorie." else null,
                )
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                mutableMealDbState.value = TheMealDbUiState(
                    query = label,
                    errorMessage = "Connexion impossible. Vérifie Internet puis réessaie.",
                )
            }
        }
    }

    fun openMeal(meal: TheMealDbMeal) {
        mealDetailsJob?.cancel()
        if (meal.hasFullDetails) {
            mutableMealDetailsState.value = MealDetailsUiState(meal = meal)
            return
        }
        mealDetailsJob = viewModelScope.launch {
            mutableMealDetailsState.value = MealDetailsUiState(isLoading = true)
            try {
                val fullMeal = mealDb.lookup(meal.id)
                mutableMealDetailsState.value = if (fullMeal != null) {
                    MealDetailsUiState(meal = fullMeal)
                } else {
                    MealDetailsUiState(errorMessage = "Les détails de cette recette sont indisponibles.")
                }
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                mutableMealDetailsState.value = MealDetailsUiState(errorMessage = "Impossible de charger la recette. Réessaie.")
            }
        }
    }

    fun closeMealDetails() {
        mealDetailsJob?.cancel()
        mutableMealDetailsState.value = MealDetailsUiState()
    }
    val uiState: StateFlow<MealsUiState> = combine(transactions.observeAll(), preferences.data) { transactionsList, prefs ->
        val spent = MoneyCalculations.currentMonthTransactions(transactionsList)
            .filter { it.type == TransactionType.EXPENSE && it.category == "Alimentation" }
            .sumOf { it.amountCents }
        val budget = prefs[ProfilePreferences.limits.getValue("Alimentation")]
            ?: ProfilePreferences.defaultLimits.getValue("Alimentation")
        MealsUiState(spentCents = spent, budgetCents = budget)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MealsUiState())
    fun searchTheMealDb(query: String = mutableMealDbState.value.query) {
        val normalized = query.trim()
        if (normalized.isBlank()) return
        mealSearchJob?.cancel()
        mealSearchJob = viewModelScope.launch {
            mutableMealDbState.value = mutableMealDbState.value.copy(
                query = normalized,
                isLoading = true,
                errorMessage = null,
            )
            try {
                val result = mealDb.search(normalized)
                mutableMealDbState.value = TheMealDbUiState(
                    query = normalized,
                    meals = result.meals,
                    offlineCache = result.fromCache,
                )
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                mutableMealDbState.value = TheMealDbUiState(
                    query = normalized,
                    errorMessage = "Connexion impossible. Vérifie Internet puis réessaie.",
                )
            }
        }
    }
}
