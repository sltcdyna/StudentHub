package com.dyna.studenthub

import android.app.Application
import com.dyna.studenthub.data.local.AppDatabase
import com.dyna.studenthub.data.local.Metadata
import com.dyna.studenthub.data.local.Product
import com.dyna.studenthub.data.local.Recipe
import com.dyna.studenthub.data.local.RecipeIngredient
import com.dyna.studenthub.data.local.Transaction
import com.dyna.studenthub.data.local.TransactionType
import com.dyna.studenthub.data.local.Task
import com.dyna.studenthub.data.local.TaskType
import com.dyna.studenthub.data.RecipeCatalogLoader
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.Executors
import javax.inject.Inject

@HiltAndroidApp
class StudentHubApplication : Application() {
    @Inject lateinit var database: AppDatabase

    override fun onCreate() {
        super.onCreate()
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            database.runInTransaction {
                if (database.metadataDao().value(SEED_KEY) != SEEDED_VALUE) {
                    val date: Long = System.currentTimeMillis()
                    database.transactionDao().insertAll(
                    listOf(
                        Transaction(type = TransactionType.REVENUE, category = "Salaire", label = "Salaire", amountCents = 95_000L, date = date),
                        Transaction(type = TransactionType.REVENUE, category = "APL / Aides", label = "APL", amountCents = 20_000L, date = date),
                        Transaction(type = TransactionType.REVENUE, category = "Autres revenus", label = "Autres revenus", amountCents = 5_000L, date = date),
                        Transaction(type = TransactionType.EXPENSE, category = "Logement", label = "Logement", amountCents = 52_000L, date = date),
                        Transaction(type = TransactionType.EXPENSE, category = "Transport", label = "Transport", amountCents = 7_000L, date = date),
                        Transaction(type = TransactionType.EXPENSE, category = "Alimentation", label = "Alimentation", amountCents = 18_000L, date = date),
                        Transaction(type = TransactionType.EXPENSE, category = "Loisirs", label = "Loisirs", amountCents = 12_000L, date = date),
                        Transaction(type = TransactionType.EXPENSE, category = "Autres", label = "Autres", amountCents = 6_300L, date = date),
                    ),
                    )
                    database.productDao().insertAll(
                    listOf(
                        Product(name = "Pâtes", quantity = 2, estimatedPriceCents = 250L, checked = false),
                        Product(name = "Lait", quantity = 1, estimatedPriceCents = 120L, checked = false),
                        Product(name = "Pommes", quantity = 6, estimatedPriceCents = 300L, checked = true),
                    ),
                    )
                    database.metadataDao().insert(Metadata(SEED_KEY, SEEDED_VALUE))
                }
                if (database.metadataDao().value(TASK_SEED_KEY) != TASK_SEED_VALUE) {
                    val taskDate = System.currentTimeMillis()
                    database.taskDao().insertAll(
                        listOf(
                            Task(type = TaskType.HOMEWORK, subject = "Mathématiques", title = "Série d'exercices", dueDate = taskDate + 2 * 86_400_000L, done = false),
                            Task(type = TaskType.EXAM, subject = "Droit", title = "Réviser le partiel", dueDate = taskDate + 5 * 86_400_000L, done = false),
                            Task(type = TaskType.HOMEWORK, subject = "Anglais", title = "Présentation orale", dueDate = taskDate + 8 * 86_400_000L, done = false),
                        ),
                    )
                    database.metadataDao().insert(Metadata(TASK_SEED_KEY, TASK_SEED_VALUE))
                }
                if (database.metadataDao().value(RECIPE_SEED_KEY) != RECIPE_SEED_VALUE) {
                    RecipeCatalogLoader.load(this).forEach { entry ->
                        val id = database.recipeDao().insert(entry.recipe)
                        database.recipeIngredientDao().insertAll(entry.ingredients.map { it.copy(recipeId = id) })
                    }
                    database.metadataDao().insert(Metadata(RECIPE_SEED_KEY, RECIPE_SEED_VALUE))
                }
            }
        }
        executor.shutdown()
    }

    private companion object {
        const val SEED_KEY: String = "demo_seed"
        const val SEEDED_VALUE: String = "v1"
        const val RECIPE_SEED_KEY: String = "recipe_demo_seed"
        const val RECIPE_SEED_VALUE: String = "v1"
        const val TASK_SEED_KEY: String = "task_demo_seed"
        const val TASK_SEED_VALUE: String = "v1"
    }
}
