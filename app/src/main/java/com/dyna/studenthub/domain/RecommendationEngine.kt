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
        val valid = all.filter { c ->
            (maxBudget == null || c.costPerPortionCents <= maxBudget) &&
                (maxTime == null || c.recipe.timeMinutes <= maxTime) &&
                when (diet) {
                    Diet.TOUT -> true
                    Diet.VEGETARIEN -> c.recipe.tags.split(",").none { it.equals("porc", true) || it.equals("poulet", true) || it.equals("poisson", true) || it.equals("viande", true) }
                    Diet.SANS_PORC -> !c.recipe.tags.split(",").any { it.equals("porc", true) }
                }
        }
        val fresh = valid.filterNot { it.recipe.id in history }.ifEmpty { valid }
        val shuffled = fresh.shuffled(Random(seed))
        val chosen = mutableListOf<RecommendationCandidate>()
        shuffled.sortedBy { it.recipe.category }.forEach { item ->
            if (chosen.size < 3 && chosen.none { selected -> selected.recipe.category == item.recipe.category }) chosen += item
        }
        if (chosen.size < 3) chosen += shuffled.filterNot { it in chosen }.take(3 - chosen.size)
        return chosen
    }
}
