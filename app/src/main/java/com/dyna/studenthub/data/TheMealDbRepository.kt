package com.dyna.studenthub.data

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.Normalizer
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class TheMealDbIngredient(val name: String, val measure: String)

data class TheMealDbMeal(
    val id: String,
    val name: String,
    val thumbnailUrl: String?,
    val instructions: String,
    val ingredients: List<TheMealDbIngredient>,
    val category: String,
    val area: String,
    val tags: List<String>,
    val sourceUrl: String?,
    val youtubeUrl: String?,
    val hasFullDetails: Boolean = true,
) {
    val previewImageUrl: String?
        get() = thumbnailUrl?.let { url -> if (url.endsWith("/small")) url else "$url/small" }

    val detailImageUrl: String?
        get() = thumbnailUrl?.let { url ->
            val uri = Uri.parse(url)
            val path = uri.path.orEmpty()
            if (path.endsWith(".jpg", ignoreCase = true) || path.endsWith(".png", ignoreCase = true)) {
                uri.buildUpon().path("$path/large").build().toString()
            } else url
        }
}

data class TheMealDbSearchResult(val meals: List<TheMealDbMeal>, val fromCache: Boolean)

class TheMealDbRepository @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val cache = context.getSharedPreferences("themealdb_search_cache", Context.MODE_PRIVATE)

    suspend fun search(query: String): TheMealDbSearchResult = withContext(Dispatchers.IO) {
        val normalized = query.trim().lowercase()
        require(normalized.isNotEmpty())
        val cacheKey = normalized.hashCode().toUInt().toString(16)
        val payloadKey = "payload_$cacheKey"
        val savedAtKey = "saved_at_$cacheKey"
        val cachedJson = cache.getString(payloadKey, null)
        val savedAt = cache.getLong(savedAtKey, 0L)
        if (cachedJson != null && System.currentTimeMillis() - savedAt < CACHE_TTL_MS) {
            return@withContext TheMealDbSearchResult(parseMeals(cachedJson), fromCache = true)
        }

        try {
            val terms = listOf(query.trim(), translateFrenchFoodTerms(query)).distinct()
            var body = "{\"meals\":null}"
            var meals = emptyList<TheMealDbMeal>()
            for (term in terms) {
                body = fetchByName(term)
                meals = parseMeals(body)
                if (meals.isNotEmpty()) break
            }
            storeCache(payloadKey, savedAtKey, body)
            TheMealDbSearchResult(meals, fromCache = false)
        } catch (error: Exception) {
            if (cachedJson != null) {
                TheMealDbSearchResult(parseMeals(cachedJson), fromCache = true)
            } else {
                throw error
            }
        }
    }

    suspend fun browseCategory(category: String): List<TheMealDbMeal> = withContext(Dispatchers.IO) {
        fetchCategory(category)
    }

    suspend fun browsePopularCategories(): List<TheMealDbMeal> = withContext(Dispatchers.IO) {
        val categories = listOf("Beef", "Chicken", "Dessert", "Pasta", "Seafood", "Vegetarian")
        coroutineScope {
            categories.map { category ->
                async(Dispatchers.IO) {
                    try {
                        fetchCategory(category).take(14)
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (_: Exception) {
                        emptyList()
                    }
                }
            }.awaitAll().flatten().distinctBy(TheMealDbMeal::id)
        }
    }

    suspend fun lookup(id: String): TheMealDbMeal? = withContext(Dispatchers.IO) {
        val json = fetch("lookup.php?i=${URLEncoder.encode(id, Charsets.UTF_8.name())}")
        parseMeals(json).firstOrNull()
    }

    private fun fetchCategory(category: String): List<TheMealDbMeal> {
        val key = "category_${category.lowercase().hashCode().toUInt().toString(16)}"
        val cached = readCache(key)
        val json = cached ?: fetch("filter.php?c=${URLEncoder.encode(category, Charsets.UTF_8.name())}").also { storeCache(key, it) }
        val meals = JSONObject(json).optJSONArray("meals") ?: return emptyList()
        return (0 until meals.length()).mapNotNull { index ->
            val meal = meals.optJSONObject(index) ?: return@mapNotNull null
            val id = meal.optString("idMeal").trim().takeIf(String::isNotEmpty) ?: return@mapNotNull null
            val name = meal.optString("strMeal").trim().takeIf(String::isNotEmpty) ?: return@mapNotNull null
            val photo = meal.optString("strMealThumb").trim().takeIf(::isTheMealDbPhoto)
            TheMealDbMeal(
                id = id, name = name, thumbnailUrl = photo, instructions = "", ingredients = emptyList(),
                category = category, area = "", tags = emptyList(), sourceUrl = null, youtubeUrl = null,
                hasFullDetails = false,
            )
        }
    }

    private fun fetchByName(query: String): String {
        return fetch("search.php?s=${URLEncoder.encode(query, Charsets.UTF_8.name())}")
    }

    private fun fetch(path: String): String {
        val connection = (URL("$BASE_URL/$path").openConnection() as HttpURLConnection).apply {
            connectTimeout = 8_000
            readTimeout = 10_000
            requestMethod = "GET"
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "StudentHub/1.0 (Android; recipe search)")
        }
        return try {
            if (connection.responseCode !in 200..299) error("TheMealDB HTTP ${connection.responseCode}")
            connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }

    private fun readCache(key: String): String? {
        val savedAt = cache.getLong("saved_at_$key", 0L)
        if (System.currentTimeMillis() - savedAt >= CACHE_TTL_MS) return null
        return cache.getString("payload_$key", null)
    }

    private fun storeCache(key: String, body: String) {
        cache.edit().putString("payload_$key", body).putLong("saved_at_$key", System.currentTimeMillis()).apply()
    }

    private fun translateFrenchFoodTerms(query: String): String {
        val normalized = Normalizer.normalize(query.lowercase(), Normalizer.Form.NFD)
            .replace("\\p{Mn}+".toRegex(), "")
        val translations = mapOf(
            "poulet" to "chicken", "poulets" to "chicken", "pate" to "pasta", "pates" to "pasta",
            "boeuf" to "beef", "porc" to "pork", "poisson" to "fish", "saumon" to "salmon",
            "crevette" to "shrimp", "crevettes" to "shrimp", "oeuf" to "egg", "oeufs" to "eggs",
            "legume" to "vegetable", "legumes" to "vegetables", "vegetarien" to "vegetarian",
            "vegetarienne" to "vegetarian", "gateau" to "cake", "dessert" to "dessert",
            "salade" to "salad", "soupe" to "soup", "agneau" to "lamb", "riz" to "rice",
            "canard" to "duck", "dinde" to "turkey", "fromage" to "cheese", "creme" to "cream",
            "pommes" to "apple", "pomme" to "apple", "pain" to "bread", "patate" to "potato",
            "pommes de terre" to "potato",
        )
        return normalized.split(Regex("\\s+")).joinToString(" ") { word -> translations[word] ?: word }
    }

    private fun storeCache(payloadKey: String, savedAtKey: String, body: String) {
        val now = System.currentTimeMillis()
        val editor = cache.edit().putString(payloadKey, body).putLong(savedAtKey, now)
        val entries = cache.all.keys.filter { it.startsWith("saved_at_") }
            .mapNotNull { key -> cache.getLong(key, 0L).takeIf { it > 0L }?.let { key to it } }
            .sortedByDescending { it.second }
        entries.drop(MAX_CACHED_QUERIES - 1).forEach { (timestampKey, _) ->
            val stalePayloadKey = timestampKey.removePrefix("saved_at_").let { "payload_$it" }
            editor.remove(timestampKey).remove(stalePayloadKey)
        }
        editor.apply()
    }

    private fun parseMeals(json: String): List<TheMealDbMeal> {
        val meals = JSONObject(json).optJSONArray("meals") ?: return emptyList()
        return (0 until meals.length()).mapNotNull { index ->
            val meal = meals.optJSONObject(index) ?: return@mapNotNull null
            val id = meal.optString("idMeal").trim().takeIf(String::isNotEmpty) ?: return@mapNotNull null
            val name = meal.optString("strMeal").trim().takeIf(String::isNotEmpty) ?: return@mapNotNull null
            val photo = meal.optString("strMealThumb").trim().takeIf(::isTheMealDbPhoto)
            val ingredients = (1..20).mapNotNull { ingredientIndex ->
                val ingredient = meal.optString("strIngredient$ingredientIndex").trim()
                val measure = meal.optString("strMeasure$ingredientIndex").trim()
                ingredient.takeIf(String::isNotEmpty)?.let { TheMealDbIngredient(it, measure) }
            }
            val tags = meal.optString("strTags").takeUnless { it.equals("null", ignoreCase = true) }
                .orEmpty().split(',').map { it.trim() }.filter(String::isNotEmpty)
            TheMealDbMeal(
                id = id,
                name = name,
                thumbnailUrl = photo,
                instructions = meal.optString("strInstructions").trim(),
                ingredients = ingredients,
                category = meal.optString("strCategory").trim(),
                area = meal.optString("strArea").trim(),
                tags = tags,
                sourceUrl = meal.optString("strSource").trim().takeIf { it.startsWith("https://") },
                youtubeUrl = meal.optString("strYoutube").trim().takeIf { it.startsWith("https://") },
            )
        }
    }

    private fun isTheMealDbPhoto(value: String): Boolean = runCatching {
        val uri = Uri.parse(value)
        uri.scheme == "https" && uri.host == "www.themealdb.com" &&
            uri.path.orEmpty().startsWith("/images/media/meals/")
    }.getOrDefault(false)

    private companion object {
        const val BASE_URL = "https://www.themealdb.com/api/json/v1/1"
        const val CACHE_TTL_MS = 24L * 60L * 60L * 1000L
        const val MAX_CACHED_QUERIES = 12
    }
}
