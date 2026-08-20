package com.example.core.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "words",
    indices = [
        Index(value = ["germanWord"], unique = false),
        Index(value = ["arabicTranslation"], unique = false),
        Index(value = ["plural"], unique = false)
    ]
)
data class WordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val germanWord: String,
    val article: String? = null,
    val plural: String? = null,
    val wordType: String = "noun",
    val arabicTranslation: String = "",
    val level: Int = 0,
    val frequencyRank: Int? = null,
    val originNounId: Long? = null,
    val status: String = "ACTIVE",
    val pos: String = wordType,
    val source: String = "local"
)
