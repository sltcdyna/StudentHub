package com.dyna.studenthub.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.Delete
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
import com.dyna.studenthub.domain.Money
import com.dyna.studenthub.domain.TaskCalculations
import com.dyna.studenthub.viewmodel.MoneyViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val ExpenseCategories = listOf("Alimentation", "Logement", "Transport", "Loisirs", "Autres")
private val RevenueCategories = listOf("Salaire", "APL / Aides", "Autres revenus")
private fun euros(cents: Long): String = NumberFormat.getCurrencyInstance(Locale.FRANCE).format(cents / 100.0)

@Composable
fun MoneyScreen(viewModel: MoneyViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var showBalance by rememberSaveable { mutableStateOf(true) }
    var dialog by rememberSaveable { mutableStateOf(false) }
    var transactionToDelete by remember { mutableStateOf<Transaction?>(null) }
    val visible = when (tab) {
        1 -> state.transactions.filter { it.type == TransactionType.EXPENSE }
        2 -> state.transactions.filter { it.type == TransactionType.REVENUE }
        else -> state.transactions
    }
    Scaffold(floatingActionButton = {
        FloatingActionButton(onClick = { dialog = true }, containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary) {
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
                Column(Modifier.padding(top = 8.dp, bottom = 4.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("Mon argent", style = MaterialTheme.typography.headlineMedium)
                    Text("Un budget plus clair, sans prise de tête.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                TabRow(selectedTabIndex = tab) {
                    listOf("Vue d'ensemble", "Dépenses", "Revenus").forEachIndexed { index, title ->
                        Tab(
                            selected = index == tab,
                            onClick = { tab = index },
                            selectedContentColor = MaterialTheme.colorScheme.secondary,
                            unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            text = { Text(title) },
                        )
                    }
                }
            }
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(24.dp),
                ) {
                    Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Solde disponible", color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = .78f))
                            Text(
                                if (showBalance) euros(state.balanceCents) else "••••",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.headlineLarge,
                            )
                        }
                        IconButton({ showBalance = !showBalance }) {
                            Icon(
                                if (showBalance) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                                if (showBalance) stringResource(R.string.hide_balance_description) else stringResource(R.string.show_balance_description),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                }
            }
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Ton repère pour la semaine", style = MaterialTheme.typography.titleMedium)
                        Text("${euros(state.weeklyBudgetCents)} / semaine", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
            if (tab == 0) {
                item { SpendingChart(state.categoryTotals) }
            }
            if (state.exceededCategories.isNotEmpty()) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                        Text("Attention : ${state.exceededCategories.joinToString()} dépasse 80% du plafond.", Modifier.padding(16.dp), color = MaterialTheme.colorScheme.error)
                    }
                }
            }
            if (tab == 0) {
                item { TransactionSection("Derniers revenus", state.transactions.filter { it.type == TransactionType.REVENUE }.take(4), { transactionToDelete = it }) }
                item { TransactionSection("Dernières dépenses", state.transactions.filter { it.type == TransactionType.EXPENSE }.take(5), { transactionToDelete = it }) }
            } else {
                item { TransactionSection(if (tab == 2) "Mes revenus" else "Mes dépenses", visible, { transactionToDelete = it }) }
            }
        }
    }
    if (dialog) AddTransactionDialog({ dialog = false }, viewModel::addTransaction)
    transactionToDelete?.let { transaction ->
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = { Text("Supprimer cette opération ?") },
            text = { Text("${transaction.label} · ${euros(transaction.amountCents)}") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteTransaction(transaction)
                    transactionToDelete = null
                }) { Text("Supprimer") }
            },
            dismissButton = { TextButton(onClick = { transactionToDelete = null }) { Text("Annuler") } },
        )
    }
}

@Composable
private fun TransactionSection(title: String, transactions: List<Transaction>, onDelete: (Transaction) -> Unit) {
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
            Text(euros(transactions.sumOf { it.amountCents }), color = MaterialTheme.colorScheme.secondary)
        }
        if (transactions.isEmpty()) {
            EmptyState(stringResource(R.string.empty_transactions))
        } else {
            transactions.forEach { transaction -> TransactionRow(transaction, { onDelete(transaction) }) }
        }
    }
}

@Composable private fun TransactionRow(transaction: Transaction, onDelete: () -> Unit) {
    val positive = transaction.type == TransactionType.REVENUE
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(40.dp).background(if (positive) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(20.dp)), contentAlignment = Alignment.Center) {
            Text(if (positive) "↗" else "↘", color = if (positive) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error)
        }
        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) { Text(transaction.label); Text(transaction.category, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        Text("${if (positive) "+" else "-"}${euros(transaction.amountCents)}", color = if (positive) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface)
        IconButton(onClick = onDelete) { Icon(Icons.Rounded.Delete, contentDescription = "Supprimer ${transaction.label}", tint = MaterialTheme.colorScheme.error) }
    }
}

@Composable private fun SpendingChart(totals: Map<String, Long>) {
    val colors = listOf(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.onSurfaceVariant)
    val total = totals.values.sum()
    if (total == 0L) return
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun AddTransactionDialog(
    onDismiss: () -> Unit,
    onAdd: (TransactionType, String, String, Long, Long) -> Unit,
) {
    var type by rememberSaveable { mutableStateOf(TransactionType.EXPENSE) }
    var category by rememberSaveable { mutableStateOf(ExpenseCategories.first()) }
    var label by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableStateOf("") }
    var dateMillis by rememberSaveable { mutableLongStateOf(System.currentTimeMillis()) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf(false) }
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE) }

    val currentCategories = if (type == TransactionType.EXPENSE) ExpenseCategories else RevenueCategories

    AlertDialog(onDismissRequest = onDismiss, title = { Text("Ajouter une opération") }, text = {
        Column(
            Modifier.imePadding().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row {
                FilterChip(
                    selected = type == TransactionType.EXPENSE,
                    onClick = {
                        type = TransactionType.EXPENSE
                        category = ExpenseCategories.first()
                    },
                    label = { Text("Dépense") }
                )
                Spacer(Modifier.width(8.dp))
                FilterChip(
                    selected = type == TransactionType.REVENUE,
                    onClick = {
                        type = TransactionType.REVENUE
                        category = RevenueCategories.first()
                    },
                    label = { Text("Revenu") }
                )
            }
            OutlinedTextField(label, { label = it }, label = { Text("Libellé") }, singleLine = true)

            Text("Catégorie :", style = MaterialTheme.typography.labelMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                currentCategories.forEach { cat ->
                    FilterChip(
                        selected = category == cat,
                        onClick = { category = cat },
                        label = { Text(cat) }
                    )
                }
            }

            OutlinedTextField(amount, { amount = it }, label = { Text("Montant en euros") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
            OutlinedButton(onClick = { showDatePicker = true }) {
                Text("Date : ${dateFormatter.format(Date(dateMillis))}")
            }
        }
    }, confirmButton = { TextButton(onClick = {
        val amountCents = Money.parseEuroToCents(amount)
        if (label.isBlank() || amountCents == null || amountCents <= 0L || dateMillis <= 0) {
            error = true
        } else {
            onAdd(type, category, label, amountCents, dateMillis)
            onDismiss()
        }
    }) { Text("Ajouter") } }, dismissButton = { TextButton(onDismiss) { Text("Annuler") } })
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = TaskCalculations.localDayToDatePickerMillis(dateMillis),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        dateMillis = TaskCalculations.datePickerMillisToLocalDay(it)
                    }
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
