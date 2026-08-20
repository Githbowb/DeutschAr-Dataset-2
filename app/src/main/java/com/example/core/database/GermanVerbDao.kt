package com.example.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface GermanVerbDao {

    @Query("SELECT * FROM german_verbs WHERE LOWER(infinitive) = LOWER(:infinitive) LIMIT 1")
    suspend fun findByInfinitive(infinitive: String): GermanVerbEntity?

    @Query("""
        SELECT * FROM german_verbs 
        WHERE LOWER(infinitive) = LOWER(:verb) 
           OR LOWER(presentIch) = LOWER(:verb) 
           OR LOWER(presentDu) = LOWER(:verb) 
           OR LOWER(presentErSieEs) = LOWER(:verb) 
           OR LOWER(presentWir) = LOWER(:verb) 
           OR LOWER(presentIhr) = LOWER(:verb) 
           OR LOWER(presentSie) = LOWER(:verb) 
           OR LOWER(pastIch) = LOWER(:verb) 
           OR LOWER(partizipZwei) = LOWER(:verb) 
        LIMIT 1
    """)
    suspend fun findByForm(verb: String): GermanVerbEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(verb: GermanVerbEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVerbs(verbs: List<GermanVerbEntity>): List<Long>
}
