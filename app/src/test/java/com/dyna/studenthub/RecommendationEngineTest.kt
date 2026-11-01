package com.dyna.studenthub

import com.dyna.studenthub.data.local.Recipe
import com.dyna.studenthub.data.local.RecipeIngredient
import com.dyna.studenthub.domain.*
import org.junit.Assert.*
import org.junit.Test

class RecommendationEngineTest {
    private fun c(id: Long, category: String, tags: String = "") = RecommendationCandidate(
        Recipe(id, "R$id", 20, 2, category, "FR", tags), listOf(RecipeIngredient(recipeId = id, name = "x", priceCents = 100)))
    @Test fun filtersAndVariesCategories() {
        val result = RecommendationEngine.recommend(listOf(c(1,"A"), c(2,"B"), c(3,"C"), c(4,"A")), emptySet(), 100, 30, Diet.TOUT, 1)
        assertEquals(3, result.size); assertEquals(3, result.map { it.recipe.id }.toSet().size)
        assertEquals(3, result.map { it.recipe.category }.toSet().size)
    }
    @Test fun excludesHistoryWhenPossibleAndDiet() {
        val result = RecommendationEngine.recommend(listOf(c(1,"A","porc"), c(2,"B","végétarien"), c(3,"C","végétarien"), c(4,"D","végétarien")), setOf(2L), null, null, Diet.VEGETARIEN, 2)
        assertFalse(result.any { it.recipe.id == 2L })
    }

    @Test fun respectsBudgetAndTimeWithoutDuplicates() {
        val result = RecommendationEngine.recommend(
            listOf(
                c(1, "A").copy(recipe = Recipe(1, "Cheap", 15, 1, "A", "FR", "")),
                c(2, "B").copy(recipe = Recipe(2, "Expensive", 15, 1, "B", "FR", "")),
                c(3, "C").copy(recipe = Recipe(3, "Slow", 45, 1, "C", "FR", "")),
                c(4, "D").copy(recipe = Recipe(4, "Valid", 20, 1, "D", "FR", "")),
            ),
            emptySet(),
            maxBudget = 100,
            maxTime = 30,
            diet = Diet.TOUT,
            seed = 7,
        )
        assertTrue(result.all { it.costPerPortionCents <= 100 && it.recipe.timeMinutes <= 30 })
        assertEquals(result.size, result.map { it.recipe.id }.toSet().size)
    }
}
