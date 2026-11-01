package com.dyna.studenthub.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dyna.studenthub.data.local.Product
import com.dyna.studenthub.data.local.ProductRepository
import com.dyna.studenthub.data.local.Recipe
import com.dyna.studenthub.data.local.RecipeIngredient
import com.dyna.studenthub.data.local.RecipeIngredientRepository
import com.dyna.studenthub.data.local.RecipeRepository
import com.dyna.studenthub.data.local.TransactionRepository
import com.dyna.studenthub.domain.MealCalculations
import com.dyna.studenthub.domain.GroceryCalculations
import com.dyna.studenthub.domain.*
import com.dyna.studenthub.data.local.RecommendationHistory
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RecipeItem(val recipe: Recipe, val ingredients: List<RecipeIngredient>) {
    val costPerPortionCents get() = MealCalculations.costPerPortionCents(ingredients, recipe.portions)
}
data class MealsUiState(
    val recipes: List<RecipeItem> = emptyList(),
    val spentCents: Long = 0,
    val budgetCents: Long = 20_000,
    val maxCostCents: Long? = null,
    val maxTimeMinutes: Int? = null,
    val diet: Diet = Diet.TOUT,
    val recommendations: List<RecipeItem> = emptyList(),
)

@HiltViewModel
class MealsViewModel @Inject constructor(
    private val recipes: RecipeRepository,
    private val ingredients: RecipeIngredientRepository,
    private val products: ProductRepository,
    private val transactions: TransactionRepository,
    private val recommendationHistory: com.dyna.studenthub.data.local.RecommendationHistoryRepository,
    preferences: DataStore<Preferences>,
) : ViewModel() {
    private val maxCost = kotlinx.coroutines.flow.MutableStateFlow<Long?>(null)
    private val maxTime = kotlinx.coroutines.flow.MutableStateFlow<Int?>(null)
    private val diet = kotlinx.coroutines.flow.MutableStateFlow(Diet.TOUT)
    private var suggestionSeed = System.currentTimeMillis() / 86_400_000L
    private val recipeData = combine(recipes.observeAll(), ingredients.observeAll(), recommendationHistory.observeRecent(), maxCost) { allRecipes, allIngredients, history, limit ->
        Quad(allRecipes, allIngredients, history, limit)
    }
    val uiState: StateFlow<MealsUiState> = combine(
        recipeData, transactions.observeAll(), maxTime, diet, preferences.data,
    ) { data, transactionsList, time, selectedDiet, prefs ->
        val allRecipes = data.recipes
        val allIngredients = data.ingredients
        val history = data.history
        val limit = data.limit
        val items = allRecipes.map { recipe ->
            RecipeItem(recipe, allIngredients.filter { it.recipeId == recipe.id })
        }.filter { limit == null || it.costPerPortionCents <= limit }
        val spent = transactionsList.filter {
            it.type == com.dyna.studenthub.data.local.TransactionType.EXPENSE && it.category == "Alimentation"
        }.sumOf { it.amountCents }
        val candidates = items.map { RecommendationCandidate(it.recipe, it.ingredients) }
        val recs = RecommendationEngine.recommend(candidates, history.map { it.recipeId }.toSet(), limit, time, selectedDiet, suggestionSeed)
            .map { candidate -> items.first { it.recipe.id == candidate.recipe.id } }
        val budget = prefs[ProfilePreferences.limits.getValue("Alimentation")]
            ?: ProfilePreferences.defaultLimits.getValue("Alimentation")
        MealsUiState(items, spent, budget, limit, time, selectedDiet, recs)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MealsUiState())

    private data class Quad(val recipes: List<Recipe>, val ingredients: List<RecipeIngredient>, val history: List<RecommendationHistory>, val limit: Long?)

    fun setMaxCost(value: String) {
        maxCost.value = value.replace(',', '.').toDoubleOrNull()?.takeIf { it >= 0 }?.let { (it * 100).toLong() }
    }
    fun setMaxTime(value: String) { maxTime.value = value.toIntOrNull()?.takeIf { it > 0 } }
    fun setDiet(value: Diet) { diet.value = value }
    fun otherSuggestion() {
        suggestionSeed = System.nanoTime()
        viewModelScope.launch(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            uiState.value.recommendations.forEachIndexed { index, item ->
                recommendationHistory.insert(RecommendationHistory(recipeId = item.recipe.id, proposedDate = now, order = index.toLong()))
            }
            recommendationHistory.trim()
        }
    }
    fun toggleFavorite(item: RecipeItem) = viewModelScope.launch(Dispatchers.IO) {
        recipes.setFavorite(item.recipe, !item.recipe.favorite)
    }
    fun addToShopping(item: RecipeItem) = viewModelScope.launch(Dispatchers.IO) {
        item.ingredients.forEach { ingredient ->
            products.insert(Product(name = ingredient.name, quantity = 1, estimatedPriceCents = ingredient.priceCents, checked = false))
        }
    }
    fun addRecipe(name: String, time: Int, portions: Int, values: List<Pair<String, Long>>) {
        if (name.isBlank() || time <= 0 || portions <= 0 || values.none { it.first.isNotBlank() && it.second >= 0 }) return
        viewModelScope.launch(Dispatchers.IO) {
            val id = recipes.insert(Recipe(name = name.trim(), timeMinutes = time, portions = portions))
            values.filter { it.first.isNotBlank() && it.second >= 0 }.forEach {
                ingredients.insert(RecipeIngredient(recipeId = id, name = it.first.trim(), priceCents = it.second))
            }
        }
    }
}
