package com.example.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface GermanNounDao {

    @Query("SELECT * FROM german_nouns WHERE LOWER(lemma) = LOWER(:lemma) LIMIT 1")
    suspend fun findByLemma(lemma: String): GermanNounEntity?

    @Query("SELECT * FROM german_nouns WHERE LOWER(nominativPlural) = LOWER(:plural) LIMIT 1")
    suspend fun findByPlural(plural: String): GermanNounEntity?

    @Query("SELECT * FROM german_nouns WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): GermanNounEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(noun: GermanNounEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNouns(nouns: List<GermanNounEntity>): List<Long>
}
