package com.example.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {

    @Query("SELECT * FROM words WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): WordEntity?

    @Query("SELECT * FROM words WHERE LOWER(germanWord) = LOWER(:word) LIMIT 1")
    suspend fun findByGermanWordExact(word: String): WordEntity?

    @Query("SELECT * FROM words WHERE LOWER(germanWord) = LOWER(:word) LIMIT 1")
    suspend fun findExactIgnoreCase(word: String): WordEntity?

    @Query("SELECT * FROM words WHERE LOWER(plural) = LOWER(:plural) LIMIT 1")
    suspend fun findByPluralIgnoreCase(plural: String): WordEntity?

    @Query("SELECT * FROM words WHERE germanWord LIKE :query || '%' OR arabicTranslation LIKE '%' || :query || '%' LIMIT :limit")
    suspend fun searchWords(query: String, limit: Int = 20): List<WordEntity>

    @Query("SELECT germanWord FROM words WHERE LOWER(germanWord) LIKE LOWER(:prefix) || '%' LIMIT :limit")
    suspend fun getSuggestions(prefix: String, limit: Int = 10): List<String>

    @Query("SELECT * FROM words WHERE arabicTranslation LIKE '%' || :arabic || '%' LIMIT :limit")
    suspend fun findByArabic(arabic: String, limit: Int = 10): List<WordEntity>

    @Query("SELECT * FROM words ORDER BY id ASC LIMIT :limit")
    suspend fun getVocabularyList(limit: Int = 50): List<WordEntity>

    @Query("SELECT * FROM words ORDER BY id ASC")
    suspend fun getAllWordsList(): List<WordEntity>

    @Query("SELECT COUNT(*) FROM words")
    suspend fun countWords(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: WordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(word: WordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(words: List<WordEntity>): List<Long>
}
