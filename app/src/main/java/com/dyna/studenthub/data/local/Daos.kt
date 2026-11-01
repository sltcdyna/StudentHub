package com.dyna.studenthub.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) fun insert(transaction: Transaction): Long
    @Insert(onConflict = OnConflictStrategy.REPLACE) fun insertAll(transactions: List<Transaction>): List<Long>
    @Delete fun delete(transaction: Transaction): Int
    @Query("SELECT * FROM transactions ORDER BY date DESC") fun observeAll(): Flow<List<Transaction>>
    @Query("SELECT * FROM transactions WHERE id = :id") fun observeById(id: Long): Flow<Transaction?>
    @Query("SELECT COUNT(*) FROM transactions") fun count(): Int
    @Query("DELETE FROM transactions") fun deleteAll()
}

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) fun insert(product: Product): Long
    @Insert(onConflict = OnConflictStrategy.REPLACE) fun insertAll(products: List<Product>): List<Long>
    @Delete fun delete(product: Product): Int
    @Query("UPDATE products SET checked = :checked WHERE id = :id") fun setChecked(id: Long, checked: Boolean)
    @Query("DELETE FROM products") fun deleteAll()
    @Query("SELECT * FROM products ORDER BY name") fun observeAll(): Flow<List<Product>>
    @Query("SELECT * FROM products WHERE id = :id") fun observeById(id: Long): Flow<Product?>
}

@Dao
interface RecipeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) fun insert(recipe: Recipe): Long
    @Insert(onConflict = OnConflictStrategy.REPLACE) fun insertAll(recipes: List<Recipe>): List<Long>
    @Delete fun delete(recipe: Recipe): Int
    @Query("SELECT * FROM recipes ORDER BY name") fun observeAll(): Flow<List<Recipe>>
    @Query("SELECT * FROM recipes WHERE id = :id") fun observeById(id: Long): Flow<Recipe?>
    @Query("UPDATE recipes SET favorite = :favorite WHERE id = :id") fun setFavorite(id: Long, favorite: Boolean)
}

@Dao
interface RecipeIngredientDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) fun insert(ingredient: RecipeIngredient): Long
    @Insert(onConflict = OnConflictStrategy.REPLACE) fun insertAll(ingredients: List<RecipeIngredient>): List<Long>
    @Delete fun delete(ingredient: RecipeIngredient): Int
    @Query("SELECT * FROM recipe_ingredients WHERE recipeId = :recipeId ORDER BY name")
    fun observeForRecipe(recipeId: Long): Flow<List<RecipeIngredient>>
    @Query("SELECT * FROM recipe_ingredients ORDER BY recipeId, name") fun observeAll(): Flow<List<RecipeIngredient>>
    @Query("SELECT * FROM recipe_ingredients WHERE id = :id") fun observeById(id: Long): Flow<RecipeIngredient?>
}

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) fun insert(task: Task): Long
    @Insert(onConflict = OnConflictStrategy.REPLACE) fun insertAll(tasks: List<Task>): List<Long>
    @Delete fun delete(task: Task): Int
    @Query("SELECT * FROM tasks ORDER BY dueDate ASC, subject ASC, title ASC") fun observeAll(): Flow<List<Task>>
    @Query("SELECT * FROM tasks WHERE id = :id") fun observeById(id: Long): Flow<Task?>
    @Query("UPDATE tasks SET done = :done WHERE id = :id") fun setDone(id: Long, done: Boolean)
    @Query("DELETE FROM tasks") fun deleteAll()
}

@Dao
interface MetadataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) fun insert(metadata: Metadata)
    @Query("SELECT value FROM metadata WHERE `key` = :key LIMIT 1") fun value(key: String): String?
}

@Dao
interface RecommendationHistoryDao {
    @Insert fun insert(value: RecommendationHistory): Long
    @Query("SELECT * FROM recommendation_history ORDER BY proposedDate DESC, `order` DESC LIMIT 20")
    fun observeRecent(): Flow<List<RecommendationHistory>>
    @Query("SELECT recipeId FROM recommendation_history ORDER BY proposedDate DESC, `order` DESC LIMIT 20")
    fun recentRecipeIds(): List<Long>
    @Query("DELETE FROM recommendation_history WHERE id NOT IN (SELECT id FROM recommendation_history ORDER BY proposedDate DESC, `order` DESC LIMIT 20)")
    fun trim()
}
