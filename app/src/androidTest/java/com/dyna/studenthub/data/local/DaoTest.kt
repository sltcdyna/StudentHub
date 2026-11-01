package com.dyna.studenthub.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class DaoTest {
    private lateinit var database: AppDatabase

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun transactionCrudAndQuery() = runBlocking {
        val transaction = Transaction(
            type = TransactionType.REVENUE,
            category = "Job",
            label = "Salaire",
            amountCents = 95_000L,
            date = 100L,
        )
        val id: Long = database.transactionDao().insert(transaction)
        val saved: Transaction? = database.transactionDao().observeById(id).first()
        assertNotNull(saved)
        assertEquals(95_000L, saved?.amountCents)
        assertEquals(1, database.transactionDao().observeAll().first().size)
        assertEquals(1, database.transactionDao().delete(saved!!))
    }

    @Test
    fun productRecipeIngredientAndTaskInsertAndQuery() = runBlocking {
        val product = Product(name = "Riz", quantity = 2, estimatedPriceCents = 300L, checked = false)
        val productId: Long = database.productDao().insert(product)
        assertEquals(product, database.productDao().observeById(productId).first())

        val recipeId: Long = database.recipeDao().insert(Recipe(name = "Pâtes", timeMinutes = 15, portions = 2))
        val ingredient = RecipeIngredient(recipeId = recipeId, name = "Tomate", priceCents = 250L)
        database.recipeIngredientDao().insert(ingredient)
        assertEquals(1, database.recipeIngredientDao().observeForRecipe(recipeId).first().size)

        val task = Task(type = TaskType.EXAM, subject = "Maths", title = "Révisions", dueDate = 500L, done = false)
        val taskId: Long = database.taskDao().insert(task)
        assertEquals(task, database.taskDao().observeById(taskId).first())
    }
}
