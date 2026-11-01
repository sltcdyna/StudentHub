package com.dyna.studenthub

import com.dyna.studenthub.data.local.RecipeIngredient
import com.dyna.studenthub.domain.MealCalculations
import org.junit.Assert.assertEquals
import org.junit.Test

class MealCalculationsTest {
    @Test
    fun costPerPortionSumsIngredientsAndDividesByPortions() {
        val ingredients = listOf(
            RecipeIngredient(recipeId = 1, name = "Riz", priceCents = 250),
            RecipeIngredient(recipeId = 1, name = "Poulet", priceCents = 550),
        )
        assertEquals(400L, MealCalculations.costPerPortionCents(ingredients, 2))
    }

    @Test
    fun invalidPortionsReturnZero() {
        assertEquals(0L, MealCalculations.costPerPortionCents(emptyList(), 0))
    }
}
