package com.example.core.database

import kotlinx.coroutines.flow.Flow

interface WordRepository {
    suspend fun getWordWithDetails(wordId: Long): WordWithDetails?
    suspend fun findExactGermanWord(word: String): WordWithDetails?
    suspend fun searchWord(query: String): WordSearchResult
    suspend fun searchVocabulary(query: String): List<WordEntity>
    suspend fun findWordsByArabic(arabic: String): List<WordEntity>
    suspend fun getInitialVocabulary(): List<WordEntity>
    suspend fun getVocabularyCount(): Int
    fun getSearchHistoryCombined(limit: Int = 100): Flow<List<HistoryItemCombined>>
    suspend fun recordHistory(
        query: String,
        wordId: Long? = null,
        resultType: String = "dictionary",
        resultText: String = "",
        sourceLanguage: String = "de",
        targetLanguage: String = "ar"
    ): Long
    suspend fun toggleHistorySaved(id: Long)
    suspend fun deleteHistoryItem(id: Long)
    suspend fun clearHistory()
}

