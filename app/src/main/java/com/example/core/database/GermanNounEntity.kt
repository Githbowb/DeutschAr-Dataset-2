package com.example.core.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "german_nouns",
    indices = [
        Index(value = ["lemma"], unique = false),
        Index(value = ["nominativPlural"], unique = false)
    ]
)
data class GermanNounEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val lemma: String,
    val article: String? = null,
    val genus: String? = null,
    val pos: String? = "Substantiv",
    val nominativSingular: String? = null,
    val nominativPlural: String? = null,
    val genitivSingular: String? = null,
    val genitivPlural: String? = null,
    val dativSingular: String? = null,
    val dativPlural: String? = null,
    val akkusativSingular: String? = null,
    val akkusativPlural: String? = null,
    val rawGrammar: String? = null,
    val flexion: String? = null
)
