package com.dyna.studenthub.di

import android.content.Context
import androidx.room.Room
import com.dyna.studenthub.data.local.AppDatabase
import com.dyna.studenthub.data.local.ProductDao
import com.dyna.studenthub.data.local.RecipeDao
import com.dyna.studenthub.data.local.RecipeIngredientDao
import com.dyna.studenthub.data.local.TaskDao
import com.dyna.studenthub.data.local.TransactionDao
import com.dyna.studenthub.data.local.RecommendationHistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "studenthub.db")
            .addMigrations(object : Migration(1, 2) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE recipes ADD COLUMN favorite INTEGER NOT NULL DEFAULT 0")
                }
            }, object : Migration(2, 3) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE recipes ADD COLUMN category TEXT NOT NULL DEFAULT 'Plat'")
                    db.execSQL("ALTER TABLE recipes ADD COLUMN cuisine TEXT NOT NULL DEFAULT 'Française'")
                    db.execSQL("ALTER TABLE recipes ADD COLUMN tags TEXT NOT NULL DEFAULT ''")
                    db.execSQL("ALTER TABLE recipe_ingredients ADD COLUMN quantity TEXT NOT NULL DEFAULT ''")
                    db.execSQL("CREATE TABLE IF NOT EXISTS recommendation_history (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, recipeId INTEGER NOT NULL, proposedDate INTEGER NOT NULL, `order` INTEGER NOT NULL)")
                }
            }, object : Migration(3, 4) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE recipes ADD COLUMN catalogId TEXT NOT NULL DEFAULT ''")
                }
            }).build()

    @Provides fun provideTransactionDao(db: AppDatabase): TransactionDao = db.transactionDao()
    @Provides fun provideProductDao(db: AppDatabase): ProductDao = db.productDao()
    @Provides fun provideRecipeDao(db: AppDatabase): RecipeDao = db.recipeDao()
    @Provides fun provideRecipeIngredientDao(db: AppDatabase): RecipeIngredientDao = db.recipeIngredientDao()
    @Provides fun provideTaskDao(db: AppDatabase): TaskDao = db.taskDao()
    @Provides fun provideRecommendationHistoryDao(db: AppDatabase): RecommendationHistoryDao = db.recommendationHistoryDao()
}
