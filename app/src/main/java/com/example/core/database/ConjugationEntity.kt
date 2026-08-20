package com.example.core.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "conjugations",
    foreignKeys = [
        ForeignKey(
            entity = WordEntity::class,
            parentColumns = ["id"],
            childColumns = ["word_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["word_id"]),
        Index(value = ["form"])
    ]
)
data class ConjugationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "word_id")
    val wordId: Long,
    
    @ColumnInfo(name = "tense")
    val tense: String,
    
    @ColumnInfo(name = "person")
    val person: String,
    
    @ColumnInfo(name = "form")
    val form: String,
    
    @ColumnInfo(name = "source")
    val source: String
)
