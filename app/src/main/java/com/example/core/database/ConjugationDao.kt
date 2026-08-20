package com.example.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ConjugationDao {

    @Query("SELECT * FROM conjugations WHERE word_id = :wordId ORDER BY id ASC")
    fun getConjugationsForWord(wordId: Long): Flow<List<ConjugationEntity>>

    @Query("SELECT * FROM conjugations WHERE word_id = :wordId ORDER BY id ASC")
    suspend fun getConjugationsListForWord(wordId: Long): List<ConjugationEntity>

    @Query("SELECT * FROM conjugations WHERE LOWER(form) = LOWER(:form)")
    suspend fun findByForm(form: String): List<ConjugationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConjugation(conjugation: ConjugationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(conjugations: List<ConjugationEntity>): List<Long>
}
