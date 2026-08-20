package com.example.core.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CheatSheetDao {
    @Query("SELECT * FROM cheatsheet_items ORDER BY category ASC, phrase ASC")
    fun getAllItems(): Flow<List<CheatSheetEntity>>

    @Query("SELECT * FROM cheatsheet_items WHERE category = :category ORDER BY phrase ASC")
    fun getItemsByCategory(category: String): Flow<List<CheatSheetEntity>>

    @Query("SELECT DISTINCT category FROM cheatsheet_items ORDER BY category ASC")
    fun getAllCategories(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(entity: CheatSheetEntity): Long

    @Update
    suspend fun updateItem(entity: CheatSheetEntity): Int

    @Delete
    suspend fun deleteItem(entity: CheatSheetEntity): Int
}
