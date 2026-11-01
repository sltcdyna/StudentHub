package com.dyna.studenthub.ui.screen

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.TextButton
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
import com.dyna.studenthub.viewmodel.HomeViewModel
import java.text.NumberFormat
import java.util.Locale

private val Blue = Color(0xFF4F7CF0)
private val Green = Color(0xFF1FB57A)
private val Coral = Color(0xFFF26B5B)
private fun euros(cents: Long) = NumberFormat.getCurrencyInstance(Locale.FRANCE).format(cents / 100.0)

@Composable
fun HomeScreen(
    onStudiesClick: () -> Unit,
    onMoneyClick: () -> Unit,
    onMealsClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    var goalDialog by rememberSaveable { mutableStateOf(false) }
    var nameDialog by rememberSaveable { mutableStateOf(false) }
    if (!state.loading && state.firstName.isBlank()) nameDialog = true
    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 16.dp).imePadding(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        userScrollEnabled = true,
    ) {
        item {
            Text(if (state.firstName.isBlank()) "Bonjour 👋" else "Bonjour ${state.firstName} 👋", style = MaterialTheme.typography.headlineMedium)
            Text("Bonne journée ! Tu gères !", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SummaryCard("Devoirs / examens", "${state.upcomingTasks.size}", "7 prochains jours", Blue, onStudiesClick, Modifier.weight(1f))
                SummaryCard("Solde disponible", euros(state.balanceCents), "", Green, onMoneyClick, Modifier.weight(1f))
                SummaryCard("Budget repas restant", euros(state.foodBudgetRemainingCents), "", Coral, onMealsClick, Modifier.weight(1f))
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Prochains devoirs", style = MaterialTheme.typography.titleLarge)
                TextButton(onClick = onStudiesClick) { Text("Voir tout") }
            }
        }
        if (state.upcomingTasks.isEmpty()) item { EmptyState(stringResource(R.string.empty_upcoming_tasks)) }
        items(state.upcomingTasks.take(2), key = { it.id }) { task ->
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.padding(14.dp)) {
                    Text(task.title, style = MaterialTheme.typography.titleMedium)
                    Text("${if (task.type == TaskType.EXAM) "Examen" else "Devoir"} · ${task.subject}")
                }
            }
        }
        item {
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFE6F7EF))) {
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
                        color = Green,
                    )
                    Text("${(HomeCalculations.objectiveProgress(state.balanceCents, state.savingsGoalCents) * 100).toInt()} % atteint")
                }
            }
        }
        if (state.exceededCategories.isNotEmpty()) item {
            Card(onClick = onMoneyClick, modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFFDECEC))) {
                Text("Attention : ${state.exceededCategories.joinToString()} dépasse 80 % du plafond.", Modifier.padding(16.dp), color = Color(0xFFE5484D))
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
                    OutlinedTextField(name, { name = it }, label = { Text("Ton prénom") }, singleLine = true)
                }
            },
            confirmButton = { Button(enabled = name.isNotBlank(), onClick = { viewModel.saveFirstName(name); nameDialog = false }) { Text("Commencer") } },
        )
    }
    if (goalDialog) GoalDialog(state.savingsGoalCents, { goalDialog = false }, viewModel::saveSavingsGoalCents)
}

@Composable
private fun SummaryCard(title: String, value: String, subtitle: String, color: Color, onClick: () -> Unit, modifier: Modifier) {
    Card(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = color.copy(alpha = .12f))) {
        Column(Modifier.padding(10.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.titleMedium, color = color)
            if (subtitle.isNotBlank()) Text(subtitle, style = MaterialTheme.typography.labelSmall)
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
                OutlinedTextField(value, { value = it }, label = { Text("Montant en euros") }, singleLine = true)
            }
        },
        confirmButton = { TextButton(onClick = {
            val parsed = value.replace(',', '.').toDoubleOrNull()
            if (parsed == null || parsed < 0) {
                error = true
            } else {
                onSave((parsed * 100).toLong())
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
