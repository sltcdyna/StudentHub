package com.dyna.studenthub.domain

import com.dyna.studenthub.data.local.RecipeIngredient

object MealCalculations {
    fun totalIngredientCents(ingredients: List<RecipeIngredient>): Long =
        ingredients.sumOf { it.priceCents.coerceAtLeast(0) }

    fun costPerPortionCents(ingredients: List<RecipeIngredient>, portions: Int): Long =
        if (portions > 0) totalIngredientCents(ingredients) / portions else 0L
}
