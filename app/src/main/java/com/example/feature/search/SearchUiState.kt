package com.example.feature.search

import com.example.core.database.ExampleEntity
import com.example.core.database.GermanNounEntity
import com.example.core.database.WordEntity
import com.example.core.database.WordWithDetails
import com.example.core.translation.Direction
import com.example.core.translation.TrilingualResult
import com.example.core.translation.VerbConjugationResult

data class DynamicTranslationResult(
    val sourceText: String,
    val sourceLang: String, // "de", "en", "ar"
    val wordType: String? = null,
    val article: String? = null,
    val plural: String? = null,
    val firstTargetLang: String, // e.g. "ar" or "de"
    val firstTargetTitle: String, // e.g. "الترجمة العربية" / "الترجمة الألمانية"
    val firstTranslation: String,
    val secondTargetLang: String, // e.g. "en" or "ar"
    val secondTargetTitle: String, // e.g. "English Translation" / "الترجمة العربية"
    val secondTranslation: String,
    val verbData: VerbConjugationResult? = null,
    val nounData: GermanNounEntity? = null,
    val examples: List<ExampleEntity> = emptyList()
)

sealed interface SearchResultState {
    data object Idle : SearchResultState
    data object Loading : SearchResultState
    data class DynamicResult(val data: DynamicTranslationResult) : SearchResultState
    data class WordFound(
        val details: WordWithDetails,
        val englishMeaning: String? = null
    ) : SearchResultState
    data class TrilingualDirect(
        val result: TrilingualResult
    ) : SearchResultState
    data class SentenceTranslation(
        val sourceText: String,
        val intermediateEnglish: String,
        val targetText: String,
        val direction: Direction
    ) : SearchResultState
    data class Error(val message: String) : SearchResultState
}

data class SearchUiState(
    val query: String = "",
    val sourceLanguage: String = "de",
    val targetLanguage: String = "ar",
    val direction: Direction = Direction.GERMAN_TO_ARABIC,
    val resultState: SearchResultState = SearchResultState.Idle,
    val suggestions: List<WordEntity> = emptyList(),
    val isTranslating: Boolean = false,
    val error: String? = null,
    val selectedTab: String = "dictionary"
)

