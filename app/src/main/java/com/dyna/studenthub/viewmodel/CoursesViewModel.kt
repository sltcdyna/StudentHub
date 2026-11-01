package com.dyna.studenthub.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dyna.studenthub.data.local.Product
import com.dyna.studenthub.data.local.ProductRepository
import com.dyna.studenthub.data.local.Transaction
import com.dyna.studenthub.data.local.TransactionRepository
import com.dyna.studenthub.data.local.TransactionType
import com.dyna.studenthub.domain.GroceryCalculations
import com.dyna.studenthub.domain.ProfilePreferences
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
import kotlinx.coroutines.withTimeout
import com.dyna.studenthub.data.ProductLookupResult

data class CoursesUiState(
    val products: List<Product> = emptyList(),
    val basketTotalCents: Long = 0,
    val foodBudgetRemainingCents: Long = 20_000,
    val scannedBarcode: String? = null,
    val lookupMessage: String? = null,
)

@HiltViewModel
class CoursesViewModel @Inject constructor(
    private val products: ProductRepository,
    private val transactions: TransactionRepository,
    private val openFoodFacts: com.dyna.studenthub.data.OpenFoodFactsRepository,
    preferences: DataStore<Preferences>,
) : ViewModel() {
    val uiState: StateFlow<CoursesUiState> = combine(products.observeAll(), transactions.observeAll(), preferences.data) { items, history, prefs ->
        val basketTotal = GroceryCalculations.basketTotalCents(items)
        val foodLimit = prefs[ProfilePreferences.limits.getValue("Alimentation")]
            ?: ProfilePreferences.defaultLimits.getValue("Alimentation")
        CoursesUiState(
            products = items,
            basketTotalCents = basketTotal,
            foodBudgetRemainingCents = (foodLimit - history.filter { it.type == TransactionType.EXPENSE && it.category == "Alimentation" }.sumOf { it.amountCents } - basketTotal)
                .coerceAtLeast(0),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CoursesUiState())

    fun addProduct(name: String, quantity: Int, estimatedPriceCents: Long) = viewModelScope.launch(Dispatchers.IO) {
        if (name.isBlank() || quantity <= 0 || estimatedPriceCents <= 0L) return@launch
        products.insert(Product(name = name.trim(), quantity = quantity, estimatedPriceCents = estimatedPriceCents, checked = false))
    }

    fun deleteProduct(product: Product) = viewModelScope.launch(Dispatchers.IO) { products.delete(product) }

    fun setChecked(product: Product, checked: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        products.setChecked(product, checked)
    }

    fun finishShopping() = viewModelScope.launch(Dispatchers.IO) {
        val total = GroceryCalculations.basketTotalCents(uiState.value.products)
        if (total > 0) {
            transactions.insert(Transaction(
                type = TransactionType.EXPENSE,
                category = "Alimentation",
                label = "Courses",
                amountCents = total,
                date = System.currentTimeMillis(),
            ))
        }
        products.deleteAll()
    }

    fun lookupBarcode(barcode: String, onResult: (ProductLookupResult?) -> Unit) = viewModelScope.launch {
        runCatching { withTimeout(5_000) { openFoodFacts.findProduct(barcode) } }
            .onSuccess(onResult)
            .onFailure { onResult(null) }
    }
}
