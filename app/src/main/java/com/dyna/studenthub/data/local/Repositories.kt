package com.dyna.studenthub.data.local

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TransactionRepository @Inject constructor(private val dao: TransactionDao) {
    fun observeAll(): Flow<List<Transaction>> = dao.observeAll()
    fun observeById(id: Long): Flow<Transaction?> = dao.observeById(id)
    fun insert(value: Transaction): Long = dao.insert(value)
    fun insertAll(values: List<Transaction>) = dao.insertAll(values)
    fun delete(value: Transaction): Int = dao.delete(value)
    fun deleteAll() = dao.deleteAll()
}

class ProductRepository @Inject constructor(private val dao: ProductDao) {
    fun observeAll(): Flow<List<Product>> = dao.observeAll()
    fun observeById(id: Long): Flow<Product?> = dao.observeById(id)
    fun insert(value: Product): Long = dao.insert(value)
    fun delete(value: Product): Int = dao.delete(value)
    fun setChecked(value: Product, checked: Boolean) = dao.setChecked(value.id, checked)
    fun deleteAll() = dao.deleteAll()
}

class RecipeRepository @Inject constructor(private val dao: RecipeDao) {
    fun observeAll(): Flow<List<Recipe>> = dao.observeAll()
    fun observeById(id: Long): Flow<Recipe?> = dao.observeById(id)
    fun insert(value: Recipe): Long = dao.insert(value)
    fun delete(value: Recipe): Int = dao.delete(value)
    fun setFavorite(value: Recipe, favorite: Boolean) = dao.setFavorite(value.id, favorite)
}

class RecipeIngredientRepository @Inject constructor(private val dao: RecipeIngredientDao) {
    fun observeForRecipe(recipeId: Long): Flow<List<RecipeIngredient>> = dao.observeForRecipe(recipeId)
    fun observeById(id: Long): Flow<RecipeIngredient?> = dao.observeById(id)
    fun observeAll(): Flow<List<RecipeIngredient>> = dao.observeAll()
    fun insert(value: RecipeIngredient): Long = dao.insert(value)
    fun delete(value: RecipeIngredient): Int = dao.delete(value)
}

class TaskRepository @Inject constructor(private val dao: TaskDao) {
    fun observeAll(): Flow<List<Task>> = dao.observeAll()
    fun observeById(id: Long): Flow<Task?> = dao.observeById(id)
    fun insert(value: Task): Long = dao.insert(value)
    fun delete(value: Task): Int = dao.delete(value)
    fun setDone(value: Task, done: Boolean) = dao.setDone(value.id, done)
    fun deleteAll() = dao.deleteAll()
}

class RecommendationHistoryRepository @Inject constructor(private val dao: RecommendationHistoryDao) {
    fun observeRecent() = dao.observeRecent()
    fun insert(value: RecommendationHistory) = dao.insert(value)
    fun recentRecipeIds() = dao.recentRecipeIds()
    fun trim() = dao.trim()
}
