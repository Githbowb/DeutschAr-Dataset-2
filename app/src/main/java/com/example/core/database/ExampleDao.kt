package com.example.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ExampleDao {

    @Query("SELECT * FROM examples WHERE wordId = :wordId")
    suspend fun getExamplesForWord(wordId: Long): List<ExampleEntity>

    @Query("SELECT * FROM examples WHERE LOWER(germanSentence) LIKE '%' || LOWER(:query) || '%' LIMIT 3")
    suspend fun findExamplesByText(query: String): List<ExampleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(example: ExampleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(examples: List<ExampleEntity>): List<Long>
}
