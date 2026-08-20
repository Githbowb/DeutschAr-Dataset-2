package com.example.core.database

import androidx.room.Embedded
import androidx.room.Relation

data class WordWithDetails(
    @Embedded
    val word: WordEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "wordId"
    )
    val examples: List<ExampleEntity> = emptyList(),

    val nounData: GermanNounEntity? = null,
    val verbData: GermanVerbEntity? = null
)

sealed interface WordSearchResult {
    data class Exact(
        val wordWithDetails: WordWithDetails,
        val isPluralMatch: Boolean = false,
        val matchedPlural: String? = null
    ) : WordSearchResult

    data class Suggestions(
        val items: List<WordEntity>
    ) : WordSearchResult

    data object NotFound : WordSearchResult
}

