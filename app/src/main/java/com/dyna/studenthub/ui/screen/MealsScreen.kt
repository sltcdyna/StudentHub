package com.dyna.studenthub.ui.screen

import androidx.compose.foundation.layout.Arrangement
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dyna.studenthub.viewmodel.MealsViewModel
import com.dyna.studenthub.data.TheMealDbMeal
import java.text.NumberFormat
import java.util.Locale

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material3.FilterChip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Search
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest

private fun euros(cents: Long) = NumberFormat.getCurrencyInstance(Locale.FRANCE).format(cents / 100.0)

private val mealCategories = listOf(
    "Tout" to null,
    "Poulet" to "Chicken",
    "Bœuf" to "Beef",
    "Pâtes" to "Pasta",
    "Poisson" to "Seafood",
    "Végétarien" to "Vegetarian",
    "Vegan" to "Vegan",
    "Desserts" to "Dessert",
    "Petit-déjeuner" to "Breakfast",
    "Agneau" to "Lamb",
    "Porc" to "Pork",
    "Entrées" to "Starter",
    "Accompagnements" to "Side",
    "Autres" to "Miscellaneous",
    "Chèvre" to "Goat",
)
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MealsScreen(
    onCoursesClick: () -> Unit,
    viewModel: MealsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val mealDbState by viewModel.mealDbState.collectAsState()
    val mealDetails by viewModel.mealDetailsState.collectAsState()
    var mealSearch by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf("Tout") }
    val context = LocalContext.current

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
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Découvrir des recettes", style = MaterialTheme.typography.titleLarge)
                Text("Photos, ingrédients et instructions par TheMealDB", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = mealSearch,
                        onValueChange = {
                            mealSearch = it
                            if (it.isNotBlank()) selectedCategory = ""
                        },
                        label = { Text("Plat ou ingrédient (ex. pâtes, poisson, curry)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            selectedCategory = ""
                            viewModel.searchTheMealDb(mealSearch)
                        }),
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = {
                        selectedCategory = ""
                        viewModel.searchTheMealDb(mealSearch)
                    }, enabled = mealSearch.isNotBlank()) {
                        Icon(Icons.Rounded.Search, contentDescription = "Rechercher une recette")
                    }
                }
                Text("Par catégorie", style = MaterialTheme.typography.titleSmall)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    mealCategories.forEach { (label, apiCategory) ->
                            FilterChip(
                                selected = selectedCategory == label,
                                onClick = {
                                    mealSearch = ""
                                    selectedCategory = label
                                    if (apiCategory == null) viewModel.browsePopularCategories()
                                    else viewModel.browseCategory(label, apiCategory)
                                },
                                label = { Text(label) },
                            )
                    }
                }
                if (mealDbState.isLoading && mealDbState.meals.isNotEmpty()) {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                }
            }
        }
        if (mealDbState.isLoading && mealDbState.meals.isEmpty()) item {
            Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.Center) {
                CircularProgressIndicator()
            }
        }
        item { Text(mealDbState.query.ifBlank { "Toutes les idées" }, style = MaterialTheme.typography.titleLarge) }
        mealDbState.errorMessage?.let { message -> item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(message, color = MaterialTheme.colorScheme.onErrorContainer)
                    TextButton(onClick = {
                        if (mealSearch.isNotBlank()) {
                            viewModel.searchTheMealDb(mealSearch)
                        } else {
                        val apiCategory = mealCategories.firstOrNull { it.first == selectedCategory }?.second
                        if (apiCategory == null) viewModel.browsePopularCategories()
                        else viewModel.browseCategory(selectedCategory, apiCategory)
                        }
                    }) { Text("Réessayer") }
                }
            }
        } }
        if (!mealDbState.isLoading && mealDbState.errorMessage == null && mealDbState.meals.isEmpty()) item {
            Text("Aucune recette trouvée. Essaie une autre catégorie ou un autre terme.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        items(mealDbState.meals, key = { "themealdb-${it.id}" }) { meal ->
            MealDbRecipeCard(meal, onClick = { viewModel.openMeal(meal) })
        }
        if (mealDbState.meals.isNotEmpty()) item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Recettes et photos :", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton(onClick = {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.themealdb.com/")))
                }) { Text("TheMealDB") }
                if (mealDbState.offlineCache) Text("· hors ligne", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            val progress = when {
                state.budgetCents > 0L ->
                    (state.spentCents.toDouble() / state.budgetCents).toFloat().coerceIn(0f, 1f)
                state.spentCents > 0L -> 1f
                else -> 0f
            }
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer), shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Budget repas", style = MaterialTheme.typography.titleLarge)
                    Text("${euros(state.spentCents)} dépensés / ${euros(state.budgetCents)} plafond")
                    LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.secondary)
                }
            }
        }
        item { OutlinedButton(onClick = onCoursesClick, modifier = Modifier.fillMaxWidth()) { Text("Voir ma liste de courses") } }
    }
    if (mealDetails.isLoading || mealDetails.errorMessage != null || mealDetails.meal != null) {
        AlertDialog(
            onDismissRequest = viewModel::closeMealDetails,
            title = { Text(mealDetails.meal?.name ?: "Recette") },
            text = {
                when {
                    mealDetails.isLoading -> Row(Modifier.fillMaxWidth().padding(20.dp), horizontalArrangement = Arrangement.Center) {
                        CircularProgressIndicator()
                    }
                    mealDetails.errorMessage != null -> Text(mealDetails.errorMessage!!, color = MaterialTheme.colorScheme.error)
                    mealDetails.meal != null -> {
                        val meal = mealDetails.meal!!
                        Column(
                            Modifier.fillMaxWidth().heightIn(max = 520.dp).verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            MealPhoto(meal.detailImageUrl, "Photo de ${meal.name}", Modifier.fillMaxWidth().height(190.dp))
                            val metadata = listOf(meal.category, meal.area).filter(String::isNotBlank).joinToString(" · ")
                            if (metadata.isNotBlank()) Text(metadata, color = MaterialTheme.colorScheme.secondary)
                            if (meal.tags.isNotEmpty()) Text(meal.tags.joinToString(" · "), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Ingrédients", style = MaterialTheme.typography.titleMedium)
                            meal.ingredients.forEach { ingredient ->
                                Text(listOf(ingredient.measure, ingredient.name).filter(String::isNotBlank).joinToString("  "))
                            }
                            Text("Préparation", style = MaterialTheme.typography.titleMedium)
                            Text(meal.instructions.ifBlank { "Les instructions ne sont pas disponibles pour cette recette." })
                            Text("Source des données et photos : TheMealDB", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = viewModel::closeMealDetails) { Text("Fermer") } },
            dismissButton = {
                Row {
                    mealDetails.meal?.sourceUrl?.let { url ->
                        TextButton(onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }) { Text("Source") }
                    }
                    mealDetails.meal?.youtubeUrl?.let { url ->
                        TextButton(onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }) { Text("Vidéo") }
                    }
                }
            },
        )
    }
}

@Composable
private fun MealDbRecipeCard(meal: TheMealDbMeal, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(22.dp),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            MealPhoto(meal.previewImageUrl, "Photo de ${meal.name}", Modifier.fillMaxWidth().height(174.dp))
            Text(meal.name, style = MaterialTheme.typography.titleMedium)
            val metadata = listOf(meal.category, meal.area).filter(String::isNotBlank).joinToString(" · ")
            if (metadata.isNotBlank()) Text(metadata, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (meal.tags.isNotEmpty()) Text(meal.tags.take(3).joinToString(" · "), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (meal.hasFullDetails && meal.ingredients.isNotEmpty()) {
                Text("${meal.ingredients.size} ingrédients", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
            }
            TextButton(onClick = onClick) { Text("Voir la recette") }
        }
    }
}

@Composable
private fun MealPhoto(url: String?, description: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val shape = RoundedCornerShape(18.dp)
    if (url.isNullOrBlank()) {
        Box(modifier.clip(shape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.Restaurant, contentDescription = description, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    var painterState by remember(url) { mutableStateOf<AsyncImagePainter.State>(AsyncImagePainter.State.Empty) }
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context).data(url).crossfade(true).build(),
        onState = { painterState = it },
    )
    Box(modifier.clip(shape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
        if (painterState is AsyncImagePainter.State.Error) {
            Icon(Icons.Rounded.Restaurant, contentDescription = description, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Image(painter = painter, contentDescription = description, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            if (painterState is AsyncImagePainter.State.Loading || painterState is AsyncImagePainter.State.Empty) {
                CircularProgressIndicator(Modifier.size(28.dp), strokeWidth = 2.dp)
            }
        }
    }
}
