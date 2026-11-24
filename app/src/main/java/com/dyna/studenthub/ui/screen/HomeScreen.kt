package com.dyna.studenthub.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.dyna.studenthub.R
import androidx.hilt.navigation.compose.hiltViewModel
import com.dyna.studenthub.data.local.TaskType
import com.dyna.studenthub.domain.HomeCalculations
import com.dyna.studenthub.domain.Money
import com.dyna.studenthub.viewmodel.HomeViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.text.style.TextOverflow

private fun euros(cents: Long) = NumberFormat.getCurrencyInstance(Locale.FRANCE).format(cents / 100.0)

@Composable
fun HomeScreen(
    onStudiesClick: () -> Unit,
    onMoneyClick: () -> Unit,
    onMealsClick: () -> Unit,
    onProfileClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    var goalDialog by rememberSaveable { mutableStateOf(false) }
    var nameDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.loading, state.firstName) {
        if (!state.loading && state.firstName.isBlank()) {
            nameDialog = true
        }
    }
    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 16.dp).imePadding(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        userScrollEnabled = true,
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        SimpleDateFormat("EEEE d MMMM", Locale.FRANCE).format(Date())
                            .replaceFirstChar { it.uppercase(Locale.FRANCE) },
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                    Text(
                        if (state.firstName.isBlank()) "Bonjour 👋" else "Bonjour ${state.firstName} 👋",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Text("Une journée à la fois, tu avances.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(
                    onClick = onProfileClick,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(18.dp)),
                ) {
                    Icon(
                        Icons.Rounded.Person,
                        contentDescription = "Ouvrir mon profil",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }
        }
        item {
            CompanionView(
                mood = state.companionMood,
                message = state.companionMessage,
                companionId = state.companionId,
                companionName = state.companionName,
            )
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SummaryCard("Solde disponible", euros(state.balanceCents), "", MaterialTheme.colorScheme.secondary, onMoneyClick, Modifier.weight(1f))
                    SummaryCard("Budget repas", euros(state.foodBudgetRemainingCents), "restant", MaterialTheme.colorScheme.secondary, onMealsClick, Modifier.weight(1f))
                }
                SummaryCard(
                    "Devoirs et examens à venir",
                    "${state.upcomingTasks.size}",
                    "échéances non terminées",
                    MaterialTheme.colorScheme.secondary,
                    onStudiesClick,
                    Modifier.fillMaxWidth(),
                )
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Accès rapide", style = MaterialTheme.typography.titleLarge)
                    Text("Tes essentiels", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DashboardShortcut("Études", Icons.AutoMirrored.Rounded.MenuBook, MaterialTheme.colorScheme.primaryContainer, onStudiesClick, Modifier.weight(1f))
                    DashboardShortcut("Budget", Icons.Rounded.AccountBalanceWallet, MaterialTheme.colorScheme.tertiaryContainer, onMoneyClick, Modifier.weight(1f))
                    DashboardShortcut("Repas", Icons.Rounded.Restaurant, MaterialTheme.colorScheme.secondaryContainer, onMealsClick, Modifier.weight(1f))
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Prochaines échéances", style = MaterialTheme.typography.titleLarge)
                TextButton(onClick = onStudiesClick) { Text("Voir tout") }
            }
        }
        if (state.upcomingTasks.isEmpty()) item {
            EmptyState(stringResource(R.string.empty_upcoming_tasks))
        }
        items(state.upcomingTasks.take(3), key = { it.id }) { task ->
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(14.dp)) {
                    Text(task.title, style = MaterialTheme.typography.titleMedium)
                    Text("${if (task.type == TaskType.EXAM) "Examen" else "Devoir"} · ${task.subject}")
                }
            }
        }
        item {
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                Column(Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Mes objectifs", style = MaterialTheme.typography.titleLarge)
                        TextButton(onClick = { goalDialog = true }) { Text("Modifier") }
                    }
                    Text("Objectif épargne mensuel : ${euros(state.savingsGoalCents)}")
                    Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                        progress = { HomeCalculations.objectiveProgress(state.balanceCents, state.savingsGoalCents) },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.secondary,
                    )
                    Text("${(HomeCalculations.objectiveProgress(state.balanceCents, state.savingsGoalCents) * 100).toInt()} % atteint")
                }
            }
        }
        if (state.exceededCategories.isNotEmpty()) item {
            Card(onClick = onMoneyClick, modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Text("Attention : ${state.exceededCategories.joinToString()} dépasse 80 % du plafond.", Modifier.padding(16.dp), color = MaterialTheme.colorScheme.error)
            }
        }
    }
    if (nameDialog) {
        var name by rememberSaveable { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Bienvenue sur StudentHub") },
            text = {
                Column(Modifier.imePadding().verticalScroll(rememberScrollState())) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Ton prénom") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = { Button(enabled = name.isNotBlank(), onClick = { viewModel.saveFirstName(name); nameDialog = false }) { Text("Commencer") } },
        )
    }
    if (goalDialog) GoalDialog(state.savingsGoalCents, { goalDialog = false }, viewModel::saveSavingsGoalCents)
}

@Composable
private fun DashboardShortcut(
    label: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = .72f)),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(vertical = 13.dp, horizontal = 8.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun SummaryCard(title: String, value: String, subtitle: String, color: Color, onClick: () -> Unit, modifier: Modifier) {
    Card(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = color.copy(alpha = .12f))) {
        Column(Modifier.padding(10.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(value, style = MaterialTheme.typography.titleLarge, color = color, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (subtitle.isNotBlank()) Text(subtitle, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun GoalDialog(current: Long, onDismiss: () -> Unit, onSave: (Long) -> Unit) {
    var value by rememberSaveable { mutableStateOf("%.2f".format(Locale.FRANCE, current / 100.0).replace(',', '.')) }
    var error by rememberSaveable { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Objectif mensuel") },
        text = {
            Column(Modifier.imePadding().verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text("Montant en euros") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = { TextButton(onClick = {
            val parsed = Money.parseEuroToCents(value)
            if (parsed == null) {
                error = true
            } else {
                onSave(parsed)
                onDismiss()
            }
        }) { Text("Enregistrer") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } },
    )
    if (error) {
        AlertDialog(
            onDismissRequest = { error = false },
            title = { Text("Objectif invalide") },
            text = { Text(stringResource(R.string.invalid_goal)) },
            confirmButton = { TextButton(onClick = { error = false }) { Text("OK") } },
        )
    }
}
