package com.example.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        WordEntity::class,
        GermanNounEntity::class,
        GermanVerbEntity::class,
        ExampleEntity::class,
        HistoryEntity::class,
        CheatSheetEntity::class,
        ConjugationEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun wordDao(): WordDao
    abstract fun germanNounDao(): GermanNounDao
    abstract fun germanVerbDao(): GermanVerbDao
    abstract fun exampleDao(): ExampleDao
    abstract fun historyDao(): HistoryDao
    abstract fun cheatSheetDao(): CheatSheetDao
    abstract fun conjugationDao(): ConjugationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Drop obsolete review_schedule table safely if it existed
                db.execSQL("DROP TABLE IF EXISTS review_schedule")
                
                // Drop redundant translation_history table safely if it existed
                db.execSQL("DROP TABLE IF EXISTS translation_history")

                // Update history table safely to add source_language and target_language columns if they don't exist
                try {
                    db.execSQL("ALTER TABLE history ADD COLUMN source_language TEXT NOT NULL DEFAULT 'de'")
                } catch (_: Exception) {}

                try {
                    db.execSQL("ALTER TABLE history ADD COLUMN target_language TEXT NOT NULL DEFAULT 'ar'")
                } catch (_: Exception) {}

                // Ensure fast index on query in history table
                try {
                    db.execSQL("CREATE INDEX IF NOT EXISTS index_history_query ON history(query)")
                } catch (_: Exception) {}
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "deutschar.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
