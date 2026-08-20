package com.example.core.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class HistoryResultType {
    DICTIONARY,
    OFFLINE_TRANSLATION
}

@Entity(
    tableName = "history",
    foreignKeys = [
        ForeignKey(
            entity = WordEntity::class,
            parentColumns = ["id"],
            childColumns = ["word_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["searched_at"]),
        Index(value = ["word_id"]),
        Index(value = ["query"])
    ]
)
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "word_id")
    val wordId: Long? = null,
    
    @ColumnInfo(name = "query")
    val query: String,
    
    @ColumnInfo(name = "source_language")
    val sourceLanguage: String = "de",
    
    @ColumnInfo(name = "target_language")
    val targetLanguage: String = "ar",
    
    @ColumnInfo(name = "result_type")
    val resultType: String = "dictionary", // "dictionary" or "mlkit_fallback"
    
    @ColumnInfo(name = "result_text")
    val resultText: String = "",
    
    @ColumnInfo(name = "is_saved")
    val isSaved: Boolean = false,
    
    @ColumnInfo(name = "searched_at")
    val searchedAt: Long = System.currentTimeMillis()
)

data class HistoryItemCombined(
    val history: HistoryEntity,
    val word: WordEntity? = null
)
