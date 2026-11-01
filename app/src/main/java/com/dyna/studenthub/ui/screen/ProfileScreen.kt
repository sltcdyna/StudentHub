package com.dyna.studenthub.ui.screen

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dyna.studenthub.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(viewModel: ProfileViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var name by rememberSaveable(state.firstName) { mutableStateOf(state.firstName) }
    var resetDialog by rememberSaveable { mutableStateOf(false) }
    var validationError by rememberSaveable { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        viewModel.setRemindersEnabled(granted)
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp).imePadding(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        userScrollEnabled = true,
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = state.firstName.firstOrNull()?.uppercase() ?: "?",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.clip(CircleShape).padding(12.dp),
                )
                Text(state.firstName.ifBlank { "Mon profil" }, style = MaterialTheme.typography.headlineMedium)
            }
        }
        item {
            ProfileCard("Mes infos") {
                OutlinedTextField(name, { name = it }, label = { Text("Prénom") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Button(onClick = { viewModel.saveFirstName(name) }, enabled = name.isNotBlank()) { Text("Enregistrer") }
            }
        }
        item {
            ProfileCard("Mes plafonds mensuels") {
                state.limits.forEach { (category, cents) ->
                    var value by rememberSaveable(category, cents) { mutableStateOf("%.2f".format(cents / 100.0).replace(',', '.')) }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(value, { value = it }, label = { Text(category) }, singleLine = true, modifier = Modifier.weight(1f))
                        TextButton(onClick = { validationError = !viewModel.saveLimit(category, value) }) { Text("OK") }
                    }
                }
            }
        }
        item {
            var goal by rememberSaveable(state.savingsGoalCents) { mutableStateOf("%.2f".format(state.savingsGoalCents / 100.0).replace(',', '.')) }
            ProfileCard("Mon objectif d'épargne") {
                OutlinedTextField(goal, { goal = it }, label = { Text("Montant mensuel (€)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Button(onClick = { validationError = !viewModel.saveSavingsGoal(goal) }) { Text("Enregistrer") }
            }
        }
        item {
            ProfileCard("Rappels") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(if (state.remindersEnabled) "Rappels activés" else "Rappels désactivés")
                    Switch(checked = state.remindersEnabled, onCheckedChange = {
                        if (it && android.os.Build.VERSION.SDK_INT >= 33) {
                            permissionLauncher.launch("android.permission.POST_NOTIFICATIONS")
                        } else viewModel.setRemindersEnabled(it)
                    })
                }
                if (!state.remindersEnabled) {
                    TextButton(onClick = {
                        context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}")))
                    }) { Text("Ouvrir les réglages de l'application") }
                }
            }
        }
        item {
            ProfileCard("Données") {
                Button(onClick = { resetDialog = true }) { Text("Réinitialiser les données de démo") }
            }
        }
        item { Text("StudentHub, version 1.0", modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp)) }
    }
    if (resetDialog) {
        AlertDialog(
            onDismissRequest = { resetDialog = false },
            title = { Text("Réinitialiser les données ?") },
            text = { Text("Les données actuelles seront remplacées par les données de démo.") },
            confirmButton = { TextButton(onClick = { resetDialog = false; viewModel.resetDemoData() }) { Text("Réinitialiser") } },
            dismissButton = { TextButton(onClick = { resetDialog = false }) { Text("Annuler") } },
        )
    }
    if (validationError) {
        AlertDialog(
            onDismissRequest = { validationError = false },
            title = { Text("Montant invalide") },
            text = { Text("Saisis un montant valide, vide ou positif.") },
            confirmButton = { TextButton(onClick = { validationError = false }) { Text("OK") } },
        )
    }
}

@Composable
private fun ProfileCard(title: String, content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            content()
        }
    }
}
