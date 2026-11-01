package com.dyna.studenthub.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.dyna.studenthub.R
import com.dyna.studenthub.data.local.Transaction
import com.dyna.studenthub.data.local.TransactionType
import com.dyna.studenthub.viewmodel.MoneyViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Green = Color(0xFF2E7D5B)
private val Categories = listOf("Alimentation", "Logement", "Transport", "Loisirs", "Autres")
private fun euros(cents: Long): String = NumberFormat.getCurrencyInstance(Locale.FRANCE).format(cents / 100.0)

@Composable
fun MoneyScreen(viewModel: MoneyViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var showBalance by rememberSaveable { mutableStateOf(true) }
    var dialog by rememberSaveable { mutableStateOf(false) }
    val visible = when (tab) {
        1 -> state.transactions.filter { it.type == TransactionType.EXPENSE }
        2 -> state.transactions.filter { it.type == TransactionType.REVENUE }
        else -> state.transactions
    }
    Scaffold(floatingActionButton = {
        FloatingActionButton(onClick = { dialog = true }, containerColor = Green, contentColor = Color.White) {
            Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.add_operation_description))
        }
    }) { padding ->
        LazyColumn(
            Modifier.padding(padding).padding(horizontal = 16.dp).imePadding(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 112.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            userScrollEnabled = true,
        ) {
            item {
                Text("Argent", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(top = 12.dp))
                TabRow(selectedTabIndex = tab) {
                    listOf("Vue d'ensemble", "Dépenses", "Revenus").forEachIndexed { index, title ->
                        Tab(index == tab, { tab = index }, text = { Text(title) })
                    }
                }
            }
            item {
                Card(colors = CardDefaults.cardColors(containerColor = Green), shape = RoundedCornerShape(20.dp)) {
                    Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Solde disponible", color = Color.White.copy(alpha = .8f))
                            Text(if (showBalance) euros(state.balanceCents) else "••••", color = Color.White, style = MaterialTheme.typography.headlineLarge)
                        }
                        IconButton({ showBalance = !showBalance }) {
                            Icon(
                                if (showBalance) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                                if (showBalance) stringResource(R.string.hide_balance_description) else stringResource(R.string.show_balance_description),
                                tint = Color.White,
                            )
                        }
                    }
                }
            }
            item {
                Card {
                    Column(Modifier.padding(16.dp)) {
                        Text("Budget hebdo recommandé", style = MaterialTheme.typography.titleMedium)
                        Text("${euros(state.weeklyBudgetCents)} / semaine", style = MaterialTheme.typography.headlineSmall, color = Green)
                    }
                }
            }
            if (tab == 0) {
                item { SpendingChart(state.categoryTotals) }
            }
            if (state.exceededCategories.isNotEmpty()) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE4E1))) {
                        Text("Attention : ${state.exceededCategories.joinToString()} dépasse 80% du plafond.", Modifier.padding(16.dp), color = Color(0xFF9B2C2C))
                    }
                }
            }
            if (tab == 0) {
                item { TransactionSection("Mes revenus", state.transactions.filter { it.type == TransactionType.REVENUE }) }
                item { TransactionSection("Mes dépenses", state.transactions.filter { it.type == TransactionType.EXPENSE }) }
            } else {
                val sectionType = if (tab == 2) TransactionType.REVENUE else TransactionType.EXPENSE
                item { TransactionSection(if (tab == 2) "Mes revenus" else "Mes dépenses", visible) }
            }
        }
    }
    if (dialog) AddTransactionDialog({ dialog = false }, viewModel::addTransaction)
}

@Composable
private fun TransactionSection(title: String, transactions: List<Transaction>) {
    Column(
        Modifier.imePadding(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(euros(transactions.sumOf { it.amountCents }), color = Green)
        }
        if (transactions.isEmpty()) {
            EmptyState(stringResource(R.string.empty_transactions))
        } else {
            transactions.forEach { TransactionRow(it) }
        }
    }
}

@Composable private fun TransactionRow(transaction: Transaction) {
    val positive = transaction.type == TransactionType.REVENUE
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(40.dp).background(if (positive) Color(0xFFDDF3E8) else Color(0xFFFFE4E1), RoundedCornerShape(20.dp)), contentAlignment = Alignment.Center) {
            Text(if (positive) "↗" else "↘", color = if (positive) Green else Color(0xFFC44D58))
        }
        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) { Text(transaction.label); Text(transaction.category, style = MaterialTheme.typography.bodySmall, color = Color.Gray) }
        Text("${if (positive) "+" else "-"}${euros(transaction.amountCents)}", color = if (positive) Green else MaterialTheme.colorScheme.onSurface)
    }
}

@Composable private fun SpendingChart(totals: Map<String, Long>) {
    val colors = listOf(Color(0xFF2E7D5B), Color(0xFFE39B44), Color(0xFF5B8DEF), Color(0xFFB56BD5), Color(0xFFCC5B5B))
    val total = totals.values.sum()
    if (total == 0L) return
    Card {
        Column(Modifier.padding(16.dp)) {
            Text("Répartition des dépenses", style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(150.dp), contentAlignment = Alignment.Center) {
                    Canvas(Modifier.fillMaxSize().padding(12.dp)) {
                        var start = -90f
                        totals.values.forEachIndexed { i, value ->
                            val sweep = value.toFloat() / total * 360f
                            drawArc(colors[i % colors.size], start, sweep, false, style = Stroke(22f, cap = StrokeCap.Butt)); start += sweep
                        }
                    }
                    Text(euros(total), style = MaterialTheme.typography.titleMedium)
                }
                Column { totals.entries.forEachIndexed { i, (name, value) -> Text("● $name  ${(value * 100 / total)}%", color = colors[i % colors.size]) } }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTransactionDialog(
    onDismiss: () -> Unit,
    onAdd: (TransactionType, String, String, Long, Long) -> Unit,
) {
    var type by rememberSaveable { mutableStateOf(TransactionType.EXPENSE) }
    var category by rememberSaveable { mutableStateOf(Categories.first()) }
    var label by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableStateOf("") }
    var dateMillis by rememberSaveable { mutableLongStateOf(System.currentTimeMillis()) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf(false) }
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Ajouter une opération") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row { FilterChip(type == TransactionType.EXPENSE, { type = TransactionType.EXPENSE }, { Text("Dépense") }); Spacer(Modifier.width(8.dp)); FilterChip(type == TransactionType.REVENUE, { type = TransactionType.REVENUE }, { Text("Revenu") }) }
            OutlinedTextField(label, { label = it }, label = { Text("Libellé") }, singleLine = true)
            OutlinedTextField(category, { category = it }, label = { Text("Catégorie") }, singleLine = true)
            OutlinedTextField(amount, { amount = it }, label = { Text("Montant en euros") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
            OutlinedButton(onClick = { showDatePicker = true }) {
                Text("Date : ${dateFormatter.format(Date(dateMillis))}")
            }
        }
    }, confirmButton = { TextButton(onClick = {
        val value = amount.replace(',', '.').toDoubleOrNull()
        if (label.isBlank() || value == null || value <= 0 || dateMillis <= 0) {
            error = true
        } else {
            onAdd(type, category, label, (value * 100).toLong(), dateMillis)
            onDismiss()
        }
    }) { Text("Ajouter") } }, dismissButton = { TextButton(onDismiss) { Text("Annuler") } })
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { dateMillis = it }
                    showDatePicker = false
                }) { Text("Valider") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Annuler") } },
        ) {
            DatePicker(state = datePickerState)
        }
    }
    if (error) {
        AlertDialog(
            onDismissRequest = { error = false },
            title = { Text("Opération invalide") },
            text = { Text(stringResource(R.string.invalid_amount)) },
            confirmButton = { TextButton(onClick = { error = false }) { Text("OK") } },
        )
    }
}
