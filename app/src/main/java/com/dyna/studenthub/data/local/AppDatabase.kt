package com.dyna.studenthub.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Transaction::class, Product::class, Recipe::class, RecipeIngredient::class, Task::class, Metadata::class, RecommendationHistory::class],
    version = 4,
    exportSchema = false,
)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun productDao(): ProductDao
    abstract fun recipeDao(): RecipeDao
    abstract fun recipeIngredientDao(): RecipeIngredientDao
    abstract fun taskDao(): TaskDao
    abstract fun metadataDao(): MetadataDao
    abstract fun recommendationHistoryDao(): RecommendationHistoryDao
}
