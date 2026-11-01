package com.dyna.studenthub.domain

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object ProfilePreferences {
    val firstName = stringPreferencesKey("first_name")
    val savingsGoalCents = longPreferencesKey("savings_goal_cents")
    val remindersEnabled = booleanPreferencesKey("reminders_enabled")
    val limits = mapOf(
        "Logement" to longPreferencesKey("limit_logement"),
        "Transport" to longPreferencesKey("limit_transport"),
        "Alimentation" to longPreferencesKey("limit_alimentation"),
        "Loisirs" to longPreferencesKey("limit_loisirs"),
        "Autres" to longPreferencesKey("limit_autres"),
    )
    val defaultLimits = mapOf(
        "Logement" to 60_000L,
        "Transport" to 10_000L,
        "Alimentation" to 20_000L,
        "Loisirs" to 15_000L,
        "Autres" to 10_000L,
    )
}

object ProfileValidation {
    fun parseCents(value: String): Long? =
        value.replace(',', '.').toDoubleOrNull()?.takeIf { it >= 0 }?.let { (it * 100).toLong() }
}
