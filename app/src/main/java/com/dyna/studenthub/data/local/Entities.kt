package com.dyna.studenthub.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType { REVENUE, EXPENSE }
enum class TaskType { HOMEWORK, EXAM }

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val type: TransactionType,
    val category: String,
    val label: String,
    val amountCents: Long,
    val date: Long,
)

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val quantity: Int,
    val estimatedPriceCents: Long,
    val checked: Boolean,
)

@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val timeMinutes: Int,
    val portions: Int,
    val category: String = "Plat",
    val cuisine: String = "Française",
    val tags: String = "",
    val favorite: Boolean = false,
    val catalogId: String = "",
)

@Entity(tableName = "recipe_ingredients")
data class RecipeIngredient(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val recipeId: Long,
    val name: String,
    val quantity: String = "",
    val priceCents: Long,
)

@Entity(tableName = "recommendation_history")
data class RecommendationHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val recipeId: Long,
    val proposedDate: Long,
    val order: Long,
)

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val type: TaskType,
    val subject: String,
    val title: String,
    val dueDate: Long,
    val done: Boolean,
)

@Entity(tableName = "metadata")
data class Metadata(
    @PrimaryKey val key: String,
    val value: String,
)
