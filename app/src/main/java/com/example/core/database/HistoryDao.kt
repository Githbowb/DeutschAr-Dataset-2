package com.example.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {

    @Query("SELECT * FROM history ORDER BY searched_at DESC LIMIT :limit")
    fun getRecentHistory(limit: Int = 100): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM history WHERE is_saved = 1 ORDER BY searched_at DESC")
    fun getSavedHistory(): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM history WHERE query LIKE '%' || :query || '%' OR result_text LIKE '%' || :query || '%' ORDER BY searched_at DESC")
    fun searchHistory(query: String): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM history WHERE id = :id LIMIT 1")
    suspend fun getHistoryById(id: Long): HistoryEntity?

    @Query("SELECT * FROM history WHERE query = :query ORDER BY searched_at DESC LIMIT 1")
    suspend fun getHistoryByQuery(query: String): HistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: HistoryEntity): Long

    @Update
    suspend fun updateHistory(history: HistoryEntity)

    @Query("UPDATE history SET is_saved = :isSaved WHERE id = :id")
    suspend fun updateSavedStatus(id: Long, isSaved: Boolean)

    @Query("DELETE FROM history WHERE id = :id")
    suspend fun deleteHistoryById(id: Long)

    @Query("DELETE FROM history")
    suspend fun clearHistory()
}
