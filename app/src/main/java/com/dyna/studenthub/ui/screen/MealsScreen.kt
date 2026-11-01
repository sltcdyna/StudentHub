package com.dyna.studenthub.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.dyna.studenthub.R
import com.dyna.studenthub.viewmodel.MealsViewModel
import com.dyna.studenthub.viewmodel.RecipeItem
import com.dyna.studenthub.domain.Diet
import java.text.NumberFormat
import java.util.Locale

private val Orange = Color(0xFFE87832)
private fun euros(cents: Long) = NumberFormat.getCurrencyInstance(Locale.FRANCE).format(cents / 100.0)
private fun Diet.label() = when (this) {
    Diet.TOUT -> "Tout"
    Diet.VEGETARIEN -> "Végétarien"
    Diet.SANS_PORC -> "Sans porc"
}

@Composable
fun MealsScreen(
    onCoursesClick: () -> Unit,
    viewModel: MealsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    var showForm by rememberSaveable { mutableStateOf(false) }
    var details by rememberSaveable { mutableStateOf<RecipeItem?>(null) }
    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 16.dp).imePadding(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        userScrollEnabled = true,
    ) {
        item {
            Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Repas", style = MaterialTheme.typography.headlineMedium)
                TextButton(onClick = onCoursesClick) { Text("Courses") }
            }
        }
        item {
            val progress = (state.spentCents.toFloat() / state.budgetCents).coerceIn(0f, 1f)
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF0E6))) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Budget repas", style = MaterialTheme.typography.titleLarge)
                    Text("${euros(state.spentCents)} dépensés / ${euros(state.budgetCents)} plafond")
                    LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth(), color = Orange)
                }
            }
        }
        item {
            OutlinedTextField(
                value = state.maxCostCents?.let { "%.2f".format(Locale.FRANCE, it / 100.0) } ?: "",
                onValueChange = viewModel::setMaxCost,
                label = { Text("Coût maximum par portion (€)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            OutlinedTextField(
                value = state.maxTimeMinutes?.toString() ?: "",
                onValueChange = viewModel::setMaxTime,
                label = { Text("Temps maximum (minutes)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Diet.values().forEach { diet ->
                    OutlinedButton(onClick = { viewModel.setDiet(diet) }) {
                        Text(if (diet == state.diet) "✓ ${diet.label()}" else diet.label())
                    }
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Plats recommandés", style = MaterialTheme.typography.titleLarge)
                TextButton(onClick = viewModel::otherSuggestion) { Text("Autre suggestion") }
            }
        }
        if (state.recommendations.isEmpty()) {
            item { EmptyState(stringResource(R.string.empty_recommendations)) }
        }
        items(state.recommendations, key = { "recommendation-${it.recipe.id}" }) { item ->
            RecipeCard(item, { details = item }, { viewModel.addToShopping(item) }, { viewModel.toggleFavorite(item) })
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Recettes", style = MaterialTheme.typography.titleLarge)
                TextButton(onClick = { showForm = true }) { Text("+ Créer") }
            }

        }
        if (state.recipes.isEmpty()) {
            item { EmptyState(stringResource(R.string.empty_recipes)) }
        }
        items(state.recipes, key = { it.recipe.id }) { item ->
            RecipeCard(item, { details = item }, { viewModel.addToShopping(item) }, { viewModel.toggleFavorite(item) })
        }
        item { OutlinedButton(onClick = onCoursesClick, modifier = Modifier.fillMaxWidth()) { Text("Voir ma liste de courses") } }
    }
    if (showForm) RecipeFormDialog({ showForm = false }, viewModel::addRecipe)
    details?.let { item ->
        AlertDialog(
            onDismissRequest = { details = null },
            title = { Text(item.recipe.name) },
            text = { Text(item.ingredients.joinToString("\n") { "${it.name} — ${euros(it.priceCents)}" }) },
            confirmButton = { TextButton(onClick = { details = null }) { Text("Fermer") } },
        )
    }
}

@Composable
private fun RecipeCard(item: RecipeItem, onView: () -> Unit, onShopping: () -> Unit, onFavorite: () -> Unit) {
    Card {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text(item.recipe.name, style = MaterialTheme.typography.titleMedium)
            Text("${euros(item.costPerPortionCents)} / portion · ${item.recipe.timeMinutes} min · ${item.recipe.portions} portions")
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(onClick = onView) { Text("Voir recette") }
                TextButton(onClick = onShopping) { Text("Ajouter aux courses") }
                TextButton(onClick = onFavorite) { Text(if (item.recipe.favorite) "♥ Favori" else "♡ Favoris") }
            }
        }
    }
}

@Composable
private fun RecipeFormDialog(onDismiss: () -> Unit, onAdd: (String, Int, Int, List<Pair<String, Long>>) -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var time by rememberSaveable { mutableStateOf("") }
    var portions by rememberSaveable { mutableStateOf("") }
    var ingredients by rememberSaveable(
        stateSaver = listSaver<List<Pair<String, String>>, String>(
            save = { values -> values.flatMap { listOf(it.first, it.second) } },
            restore = { values -> values.chunked(2).map { it[0] to it[1] } },
        ),
    ) { mutableStateOf(listOf("" to "")) }
    var error by rememberSaveable { mutableStateOf(false) }
    val valid = name.isNotBlank() && (time.toIntOrNull() ?: 0) > 0 && (portions.toIntOrNull() ?: 0) > 0 &&
        ingredients.any { it.first.isNotBlank() && (it.second.replace(',', '.').toDoubleOrNull() ?: -1.0) >= 0 }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Créer une recette") },
        text = {
            Column(
                Modifier.imePadding().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                OutlinedTextField(name, { name = it }, label = { Text("Nom") }, singleLine = true)
                OutlinedTextField(time, { time = it }, label = { Text("Temps (min)") }, singleLine = true)
                OutlinedTextField(portions, { portions = it }, label = { Text("Portions") }, singleLine = true)
                ingredients.forEachIndexed { index, pair ->
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        OutlinedTextField(pair.first, { value -> ingredients = ingredients.toMutableList().also { it[index] = value to pair.second } }, label = { Text("Ingrédient") }, modifier = Modifier.weight(1f), singleLine = true)
                        OutlinedTextField(pair.second, { value -> ingredients = ingredients.toMutableList().also { it[index] = pair.first to value } }, label = { Text("€") }, modifier = Modifier.weight(.6f), singleLine = true)
                    }
                }
                TextButton(onClick = { ingredients = ingredients + ("" to "") }) { Text("+ Ingrédient") }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val timeValue = time.toIntOrNull()
                val portionsValue = portions.toIntOrNull()
                if (!valid || timeValue == null || portionsValue == null) {
                    error = true
                } else {
                    onAdd(name, timeValue, portionsValue, ingredients.mapNotNull { (n, p) ->
                        p.replace(',', '.').toDoubleOrNull()?.takeIf { it >= 0 }?.let { n to (it * 100).toLong() }
                    })
                    onDismiss()
                }
            }) { Text("Créer") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } },
    )
    if (error) {
        AlertDialog(
            onDismissRequest = { error = false },
            title = { Text("Recette invalide") },
            text = { Text(stringResource(R.string.invalid_recipe)) },
            confirmButton = { TextButton(onClick = { error = false }) { Text("OK") } },
        )
    }
}
