package com.dyna.studenthub.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
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
    val context = LocalContext.current
    var showForm by rememberSaveable { mutableStateOf(false) }
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
            Text("Études", style = MaterialTheme.typography.headlineMedium)
            Text("Tes devoirs et examens, toujours au bon moment.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(state.remindersMessage ?: "Rappels activés")
                if (!state.remindersEnabled) Button(onClick = ::enableReminders) { Text("Activer les rappels") }
            }
        }
        item { Button(onClick = { showForm = true }, modifier = Modifier.fillMaxWidth()) { Text("Ajouter un devoir ou examen") } }
        if (state.tasks.isEmpty()) item { EmptyState(stringResource(R.string.empty_tasks)) }
        items(state.tasks, key = { it.id }) { task -> TaskRow(task, viewModel::setDone) }
    }
    if (showForm) AddTaskDialog(
        onDismiss = { showForm = false },
        onAdd = { type, subject, title, date -> viewModel.addTask(type, subject, title, date); showForm = false },
    )
}

@Composable
private fun TaskRow(task: Task, onDone: (Task, Boolean) -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = task.done, onCheckedChange = { onDone(task, it) })
            Column(Modifier.weight(1f)) {
                Text(task.title, style = MaterialTheme.typography.titleMedium)
                Text("${if (task.type == TaskType.EXAM) "Examen" else "Devoir"} · ${task.subject}")
                Text(DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(task.dueDate)), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTaskDialog(onDismiss: () -> Unit, onAdd: (TaskType, String, String, Long) -> Unit) {
    var type by rememberSaveable { mutableStateOf(TaskType.HOMEWORK) }
    var subject by rememberSaveable { mutableStateOf("") }
    var title by rememberSaveable { mutableStateOf("") }
    var date by rememberSaveable { mutableStateOf<Long?>(null) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf(false) }
    val valid = subject.isNotBlank() && title.isNotBlank() && (date ?: 0L) > 0L
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajouter une échéance") },
        text = {
            Column(
                Modifier.imePadding().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(type == TaskType.HOMEWORK, { type = TaskType.HOMEWORK }, label = { Text("Devoir") })
                    FilterChip(type == TaskType.EXAM, { type = TaskType.EXAM }, label = { Text("Examen") })
                }
                OutlinedTextField(subject, { subject = it }, label = { Text("Matière") }, singleLine = true)
                OutlinedTextField(title, { title = it }, label = { Text("Titre") }, singleLine = true)
                TextButton(onClick = { showDatePicker = true }) {
                    Text(date?.let { "Échéance : ${DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(it))}" } ?: "Choisir une date")
                }
            }
        },
        confirmButton = { TextButton(enabled = valid, onClick = {
            val selectedDate = date
            if (selectedDate == null || selectedDate <= 0L) error = true
            else onAdd(type, subject, title, TaskCalculations.datePickerMillisToLocalDay(selectedDate))
        }) { Text("Ajouter") } },
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
        val pickerState = androidx.compose.material3.rememberDatePickerState(initialSelectedDateMillis = date)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { TextButton(onClick = { date = pickerState.selectedDateMillis; showDatePicker = false }) { Text("Valider") } },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Annuler") } },
        ) { DatePicker(state = pickerState) }
    }
}
