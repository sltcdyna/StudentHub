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

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import com.dyna.studenthub.R
import com.dyna.studenthub.domain.CompanionAnimal
import com.dyna.studenthub.domain.StudentHubThemeChoice
import com.dyna.studenthub.ui.theme.themePreviewColors

@Composable
fun ProfileScreen(viewModel: ProfileViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var name by rememberSaveable(state.firstName) { mutableStateOf(state.firstName) }
    var resetDialog by rememberSaveable { mutableStateOf(false) }
    var validationError by rememberSaveable { mutableStateOf(false) }
    var selectedCompanion by rememberSaveable(state.companionId) { mutableStateOf(CompanionAnimal.fromId(state.companionId)) }
    var companionName by rememberSaveable(state.companionName) { mutableStateOf(state.companionName) }
    var selectedTheme by rememberSaveable(state.themeId) { mutableStateOf(StudentHubThemeChoice.fromId(state.themeId)) }
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
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.firstName.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Text(
                    state.firstName.ifBlank { "Mon profil" },
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
        item {
            ProfileCard("Personnalisation") {
                Text("Mon compagnon", style = MaterialTheme.typography.titleMedium)
                Text("Choisis ton animal et donne-lui un prénom.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(CompanionAnimal.entries.size) { index ->
                        val animal = CompanionAnimal.entries[index]
                        val imageId = when (animal) {
                            CompanionAnimal.CAT -> R.drawable.pet_cat
                            CompanionAnimal.DOG -> R.drawable.pet_dog
                            CompanionAnimal.RABBIT -> R.drawable.pet_rabbit
                            CompanionAnimal.FOX -> R.drawable.pet_fox
                            CompanionAnimal.PANDA -> R.drawable.pet_panda
                            CompanionAnimal.CAPYBARA -> R.drawable.pet_capybara
                        }
                        Card(
                            modifier = Modifier.width(112.dp).clickable {
                                val isDefaultName = companionName.isBlank() ||
                                    companionName == "Mochi" ||
                                    companionName == selectedCompanion.label ||
                                    CompanionAnimal.entries.any { it.label == companionName }
                                val newName = if (isDefaultName) animal.label else companionName
                                selectedCompanion = animal
                                companionName = newName
                                viewModel.saveCustomization(animal, newName, selectedTheme)
                            },
                            border = BorderStroke(2.dp, if (selectedCompanion == animal) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        ) {
                            Column(Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Image(painterResource(imageId), contentDescription = "Illustration du ${animal.label.lowercase()}", modifier = Modifier.size(78.dp), contentScale = ContentScale.Fit)
                                Text(animal.label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                                if (selectedCompanion == animal) {
                                    Text("Choisi", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                                }
                            }
                        }
                    }
                }
                OutlinedTextField(
                    value = companionName,
                    onValueChange = { companionName = it.take(20) },
                    label = { Text("Prénom du compagnon") },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Done),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = {
                        viewModel.saveCustomization(selectedCompanion, companionName, selectedTheme)
                    }),
                    modifier = Modifier.fillMaxWidth(),
                )
                Text("Mon thème", style = MaterialTheme.typography.titleMedium)
                Text("Choisis un thème : il s’applique tout de suite à toute l’application.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(StudentHubThemeChoice.entries.size) { index ->
                        val theme = StudentHubThemeChoice.entries[index]
                        val preview = themePreviewColors(theme)
                        val isSelected = selectedTheme == theme
                        Card(
                            modifier = Modifier.width(112.dp).clickable {
                                selectedTheme = theme
                                viewModel.saveCustomization(selectedCompanion, companionName, theme)
                            },
                            border = BorderStroke(2.dp, if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant),
                            colors = CardDefaults.cardColors(containerColor = preview.background),
                        ) {
                            Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Box(Modifier.size(20.dp).background(preview.primary, CircleShape))
                                    Box(Modifier.size(20.dp).background(preview.accent, CircleShape))
                                    Box(Modifier.size(20.dp).background(preview.surface, CircleShape))
                                }
                                Text(theme.label, color = preview.text, style = MaterialTheme.typography.labelLarge)
                                if (isSelected) {
                                    Text("Actif", color = preview.primaryDark, style = MaterialTheme.typography.labelSmall)
                                }
                                Box(Modifier.fillMaxWidth().height(4.dp).background(preview.accent, CircleShape))
                            }
                        }
                    }
                }
                Button(onClick = { viewModel.saveCustomization(selectedCompanion, companionName, selectedTheme) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Enregistrer le prénom du compagnon")
                }
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
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            content()
        }
    }
}
