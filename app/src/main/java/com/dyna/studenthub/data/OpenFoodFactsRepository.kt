package com.dyna.studenthub.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class ProductLookupResult(val barcode: String, val name: String?)
enum class ProductLookupStatus { FOUND, UNKNOWN, NETWORK_ERROR }

object OpenFoodFactsParser {
    private val nameKeys = listOf("product_name_fr", "product_name", "generic_name_fr", "generic_name")

    fun productName(json: String): String? {
        val parsed = runCatching {
            JSONObject(json).optJSONObject("product")
                ?.let { product ->
                    nameKeys.firstNotNullOfOrNull { key ->
                        product.optString(key, "").trim().takeIf { value -> value.isNotEmpty() }
                    }
                }
        }.getOrNull()
        if (parsed != null) return parsed

        return nameKeys.asSequence()
            .mapNotNull { key ->
                Regex("\"$key\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"")
                    .find(json)
                    ?.groupValues
                    ?.getOrNull(1)
                    ?.replace("\\\"", "\"")
                    ?.trim()
                    ?.takeIf { it.isNotEmpty() }
            }
            .firstOrNull()
    }
}

class OpenFoodFactsRepository @javax.inject.Inject constructor() {
    suspend fun findProduct(barcode: String): ProductLookupResult = withContext(Dispatchers.IO) {
        val connection = (URL("https://world.openfoodfacts.org/api/v2/product/$barcode.json")
            .openConnection() as HttpURLConnection).apply {
            connectTimeout = 5_000
            readTimeout = 5_000
            requestMethod = "GET"
            setRequestProperty("User-Agent", "StudentHub/1.0 (Android)")
        }
        try {
            if (connection.responseCode !in 200..299) {
                return@withContext ProductLookupResult(barcode, null)
            }
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            ProductLookupResult(barcode, OpenFoodFactsParser.productName(body))
        } finally {
            connection.disconnect()
        }
    }
}
