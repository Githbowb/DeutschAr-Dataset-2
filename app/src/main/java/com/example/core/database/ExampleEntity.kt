package com.example.core.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "examples",
    indices = [
        Index(value = ["wordId"], unique = false)
    ]
)
data class ExampleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val wordId: Long,
    val germanSentence: String,
    val arabicTranslation: String
)
