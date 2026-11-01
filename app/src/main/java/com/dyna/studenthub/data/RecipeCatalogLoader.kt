package com.dyna.studenthub.data

import android.content.Context
import com.dyna.studenthub.data.local.Recipe
import com.dyna.studenthub.data.local.RecipeIngredient
import org.json.JSONArray

object RecipeCatalogLoader {
    data class Entry(val recipe: Recipe, val ingredients: List<RecipeIngredient>)
    fun load(context: Context): List<Entry> = runCatching {
        val json = context.assets.open("recipes_catalog.json").bufferedReader().use { it.readText() }
        val array = JSONArray(json)
        (0 until array.length()).map { index ->
            val o = array.getJSONObject(index)
            val tags = o.optJSONArray("tags")?.let { a -> (0 until a.length()).joinToString(",") { a.optString(it) } } ?: ""
            val recipe = Recipe(catalogId = o.getString("id"), name = o.optString("name", o.optString("nom")), timeMinutes = o.getInt("timeMinutes"),
                portions = o.getInt("portions"), category = o.optString("category", "Plat"),
                cuisine = o.optString("cuisine", "Française"), tags = tags)
            val ingredients = o.optJSONArray("ingredients")?.let { a ->
                (0 until a.length()).map { i ->
                    val item = a.getJSONObject(i)
                    RecipeIngredient(recipeId = 0, name = item.getString("name"),
                        quantity = item.optString("quantity", ""), priceCents = item.getLong("priceCents"))
                }
            } ?: emptyList()
            Entry(recipe, ingredients)
        }
    }.getOrDefault(emptyList())
}
