package com.dyna.studenthub.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Search
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.res.stringResource
import com.dyna.studenthub.R
import com.dyna.studenthub.data.local.Task
import com.dyna.studenthub.data.local.TaskType
import com.dyna.studenthub.viewmodel.StudiesViewModel
import com.dyna.studenthub.domain.TaskCalculations
import java.text.DateFormat
import java.util.Date

@Composable
fun StudiesScreen(viewModel: StudiesViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    var showForm by rememberSaveable { mutableStateOf(false) }
    var search by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf(0) }
    var taskToDelete by remember { mutableStateOf<Task?>(null) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }
    val visibleTasks = remember(state.tasks, search, filter) {
        state.tasks.filter { task ->
            val matchesSearch = search.isBlank() ||
                task.title.contains(search.trim(), ignoreCase = true) ||
                task.subject.contains(search.trim(), ignoreCase = true)
            val matchesFilter = when (filter) {
                1 -> !task.done
                2 -> task.done
                else -> true
            }
            matchesSearch && matchesFilter
        }
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> viewModel.setRemindersEnabled(granted) }
    fun enableReminders() {
        if (Build.VERSION.SDK_INT >= 33) permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        else viewModel.setRemindersEnabled(true)
    }
    LazyColumn(
        Modifier.fillMaxSize().padding(16.dp).imePadding(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        userScrollEnabled = true,
    ) {
        item {
            Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("Ton espace études", style = MaterialTheme.typography.headlineMedium)
                Text("Des repères clairs pour avancer sereinement.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StudySummaryCard(
                    title = "À faire",
                    count = state.tasks.count { !it.done }.toString(),
                    tint = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.weight(1f),
                )
                StudySummaryCard(
                    title = "Terminées",
                    count = state.tasks.count { it.done }.toString(),
                    tint = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        item {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Rechercher une matière ou un devoir") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                trailingIcon = if (search.isNotEmpty()) {
                    { IconButton(onClick = { search = "" }) { Icon(Icons.Rounded.Close, contentDescription = "Effacer la recherche") } }
                } else null,
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf("Toutes", "À faire", "Terminées").forEachIndexed { index, label ->
                    FilterChip(
                        selected = filter == index,
                        onClick = { filter = index },
                        label = { Text(label) },
                    )
                }
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(state.remindersMessage ?: if (state.remindersEnabled) "Rappels activés" else "Rappels désactivés")
                if (!state.remindersEnabled) Button(onClick = ::enableReminders) { Text("Activer les rappels") }
            }
        }
        item { Button(onClick = { showForm = true }, modifier = Modifier.fillMaxWidth()) { Text("Ajouter un devoir ou examen") } }
        if (state.tasks.isEmpty()) {
            item { EmptyState(stringResource(R.string.empty_tasks)) }
        } else if (visibleTasks.isEmpty()) {
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    EmptyState("Aucun résultat pour cette recherche.")
                    TextButton(onClick = { search = ""; filter = 0 }) { Text("Effacer les filtres") }
                }
            }
        } else {
            item {
                Text("${visibleTasks.size} échéance(s)", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            items(visibleTasks, key = { it.id }) { task ->
                TaskRow(
                    task = task,
                    onDone = viewModel::setDone,
                    onEdit = { taskToEdit = task },
                    onDelete = { taskToDelete = task },
                )
            }
        }
    }
    if (showForm) AddTaskDialog(
        initialTask = null,
        onDismiss = { showForm = false },
        onAdd = { type, subject, title, date -> viewModel.addTask(type, subject, title, date); showForm = false },
    )
    taskToEdit?.let { task ->
        AddTaskDialog(
            initialTask = task,
            onDismiss = { taskToEdit = null },
            onAdd = { type, subject, title, date ->
                viewModel.updateTask(task, type, subject, title, date)
                taskToEdit = null
            },
        )
    }
    taskToDelete?.let { task ->
        AlertDialog(
            onDismissRequest = { taskToDelete = null },
            title = { Text("Supprimer cette échéance ?") },
            text = { Text("${task.title} · ${task.subject}") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteTask(task)
                    taskToDelete = null
                }) { Text("Supprimer") }
            },
            dismissButton = { TextButton(onClick = { taskToDelete = null }) { Text("Annuler") } },
        )
    }
}

@Composable
private fun StudySummaryCard(title: String, count: String, tint: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = tint.copy(alpha = .78f)),
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text(count, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.secondary)
            Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun TaskRow(task: Task, onDone: (Task, Boolean) -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.done) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .55f)
            else MaterialTheme.colorScheme.surface,
        ),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = task.done, onCheckedChange = { onDone(task, it) })
            Column(Modifier.weight(1f)) {
                Text(
                    task.title,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (task.done) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (task.done) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                )
                Text("${if (task.type == TaskType.EXAM) "Examen" else "Devoir"} · ${task.subject}")
                Text(DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(task.dueDate)), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Rounded.Edit, contentDescription = "Modifier ${task.title}", tint = MaterialTheme.colorScheme.secondary)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Rounded.Delete, contentDescription = "Supprimer ${task.title}", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTaskDialog(
    initialTask: Task?,
    onDismiss: () -> Unit,
    onAdd: (TaskType, String, String, Long) -> Unit,
) {
    var type by rememberSaveable(initialTask?.id) { mutableStateOf(initialTask?.type ?: TaskType.HOMEWORK) }
    var subject by rememberSaveable(initialTask?.id) { mutableStateOf(initialTask?.subject.orEmpty()) }
    var title by rememberSaveable(initialTask?.id) { mutableStateOf(initialTask?.title.orEmpty()) }
    var date by rememberSaveable(initialTask?.id) { mutableStateOf(initialTask?.dueDate) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf(false) }
    val valid = subject.isNotBlank() && title.isNotBlank() && (date ?: 0L) > 0L
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialTask == null) "Ajouter une échéance" else "Modifier l’échéance") },
        text = {
            Column(
                Modifier.imePadding().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(type == TaskType.HOMEWORK, { type = TaskType.HOMEWORK }, label = { Text("Devoir") })
                    FilterChip(type == TaskType.EXAM, { type = TaskType.EXAM }, label = { Text("Examen") })
                }
                OutlinedTextField(subject, { subject = it }, label = { Text("Matière") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(title, { title = it }, label = { Text("Titre") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                TextButton(onClick = { showDatePicker = true }) {
                    Text(date?.let { "Échéance : ${DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(it))}" } ?: "Choisir une date")
                }
            }
        },
        confirmButton = { TextButton(enabled = valid, onClick = {
            val selectedDate = date
            if (selectedDate == null || selectedDate <= 0L) error = true
            else onAdd(type, subject, title, TaskCalculations.datePickerMillisToLocalDay(selectedDate))
        }) { Text(if (initialTask == null) "Ajouter" else "Enregistrer") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } },
    )
    if (error) {
        AlertDialog(
            onDismissRequest = { error = false },
            title = { Text("Date invalide") },
            text = { Text(stringResource(R.string.invalid_date)) },
            confirmButton = { TextButton(onClick = { error = false }) { Text("OK") } },
        )
    }
    if (showDatePicker) {
        val pickerState = androidx.compose.material3.rememberDatePickerState(
            initialSelectedDateMillis = date?.let(TaskCalculations::localDayToDatePickerMillis),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { TextButton(onClick = { date = pickerState.selectedDateMillis; showDatePicker = false }) { Text("Valider") } },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Annuler") } },
        ) { DatePicker(state = pickerState) }
    }
}
