package com.dyna.studenthub.domain

import com.dyna.studenthub.data.local.Recipe
import com.dyna.studenthub.data.local.RecipeIngredient
import kotlin.random.Random

enum class Diet { TOUT, VEGETARIEN, SANS_PORC }
data class RecommendationCandidate(val recipe: Recipe, val ingredients: List<RecipeIngredient>) {
    val costPerPortionCents get() = MealCalculations.costPerPortionCents(ingredients, recipe.portions)
}
object RecommendationEngine {
    fun recommend(all: List<RecommendationCandidate>, history: Set<Long>, maxBudget: Long?, maxTime: Int?,
                  diet: Diet, seed: Long): List<RecommendationCandidate> {
        val nonVegKeywords = setOf("porc", "poulet", "poisson", "viande", "bœuf", "boeuf", "jambon", "lardon", "bacon", "saucisse", "thon", "saumon", "crevette", "dinde", "veau", "agneau")
        val porkKeywords = setOf("porc", "jambon", "lardon", "bacon", "saucisse")

        val valid = all.filter { c ->
            val matchesBudget = maxBudget == null || c.costPerPortionCents <= maxBudget
            val matchesTime = maxTime == null || c.recipe.timeMinutes <= maxTime
            val tags = c.recipe.tags.split(",").map { it.trim().lowercase() }
            val ingNames = c.ingredients.map { it.name.lowercase() }

            val matchesDiet = when (diet) {
                Diet.TOUT -> true
                Diet.VEGETARIEN -> {
                    val hasTagMeat = tags.any { tag -> nonVegKeywords.any { tag.contains(it) } }
                    val hasIngMeat = ingNames.any { ing -> nonVegKeywords.any { ing.contains(it) } }
                    !hasTagMeat && !hasIngMeat
                }
                Diet.SANS_PORC -> {
                    val hasTagPork = tags.any { tag -> porkKeywords.any { tag.contains(it) } }
                    val hasIngPork = ingNames.any { ing -> porkKeywords.any { ing.contains(it) } }
                    !hasTagPork && !hasIngPork
                }
            }
            matchesBudget && matchesTime && matchesDiet
        }
        val fresh = valid.filterNot { it.recipe.id in history }.ifEmpty { valid }
        val shuffled = fresh.shuffled(Random(seed))
        val chosen = mutableListOf<RecommendationCandidate>()
        shuffled.forEach { item ->
            if (chosen.size < 3 && chosen.none { selected -> selected.recipe.category == item.recipe.category }) {
                chosen += item
            }
        }
        if (chosen.size < 3) {
            chosen += shuffled.filterNot { it in chosen }.take(3 - chosen.size)
        }
        return chosen
    }
}
