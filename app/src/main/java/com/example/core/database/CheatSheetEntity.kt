package com.example.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cheatsheet_items")
data class CheatSheetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val phrase: String,
    val translation: String,
    val category: String,
    val languagePair: String,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
