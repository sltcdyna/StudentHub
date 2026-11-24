package com.dyna.studenthub.domain

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object ProfilePreferences {
    val firstName = stringPreferencesKey("first_name")
    val savingsGoalCents = longPreferencesKey("savings_goal_cents")
    val remindersEnabled = booleanPreferencesKey("reminders_enabled")
    val companionId = stringPreferencesKey("companion_id")
    val companionName = stringPreferencesKey("companion_name")
    val themeId = stringPreferencesKey("theme_id")
    val lastTaskCompletedAt = longPreferencesKey("last_task_completed_at")
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

enum class CompanionAnimal(val id: String, val label: String, val drawableName: String) {
    CAT("cat", "Chat", "pet_cat"), DOG("dog", "Chien", "pet_dog"),
    RABBIT("rabbit", "Lapin", "pet_rabbit"), FOX("fox", "Renard", "pet_fox"),
    PANDA("panda", "Panda", "pet_panda"), CAPYBARA("capybara", "Capybara", "pet_capybara");

    companion object { fun fromId(id: String?) = entries.firstOrNull { it.id == id } ?: CAT }
}

enum class StudentHubThemeChoice(val id: String, val label: String) {
    LAVENDER("lavender", "Lavande"), SKY("sky", "Ciel"), PINK("pink", "Rose"),
    MINT("mint", "Menthe"), CREAM("cream", "Crème"), NIGHT("night", "Nuit");

    companion object { fun fromId(id: String?) = entries.firstOrNull { it.id == id } ?: LAVENDER }
}

object ProfileValidation {
    fun parseCents(value: String): Long? = Money.parseEuroToCents(value)
}
