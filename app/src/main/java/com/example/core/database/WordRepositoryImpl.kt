package com.example.core.database

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class WordRepositoryImpl(
    val wordDao: WordDao,
    val conjugationDao: ConjugationDao? = null,
    val exampleDao: ExampleDao? = null,
    val historyDao: HistoryDao? = null,
    val nounDao: GermanNounDao? = null,
    val verbDao: GermanVerbDao? = null
) : WordRepository {

    constructor(
        wordDao: WordDao,
        nounDao: GermanNounDao,
        verbDao: GermanVerbDao,
        exampleDao: ExampleDao
    ) : this(
        wordDao = wordDao,
        conjugationDao = null,
        exampleDao = exampleDao,
        historyDao = null,
        nounDao = nounDao,
        verbDao = verbDao
    )

    constructor(
        wordDao: WordDao,
        conjugationDao: ConjugationDao,
        exampleDao: ExampleDao,
        historyDao: HistoryDao
    ) : this(
        wordDao = wordDao,
        conjugationDao = conjugationDao,
        exampleDao = exampleDao,
        historyDao = historyDao,
        nounDao = null,
        verbDao = null
    )

    override suspend fun getWordWithDetails(wordId: Long): WordWithDetails? {
        val word = wordDao.getById(wordId) ?: return null
        val examples = exampleDao?.getExamplesForWord(wordId) ?: emptyList()
        val nounData = nounDao?.findByLemma(word.germanWord)
        val verbData = verbDao?.findByInfinitive(word.germanWord)

        return WordWithDetails(
            word = word,
            examples = examples,
            nounData = nounData,
            verbData = verbData
        )
    }

    override suspend fun findExactGermanWord(word: String): WordWithDetails? {
        val exactWord = wordDao.findByGermanWordExact(word) ?: wordDao.findExactIgnoreCase(word)
        if (exactWord != null) {
            return getWordWithDetails(exactWord.id)
        }

        // Try looking up in noun or verb tables
        val noun = nounDao?.findByLemma(word) ?: nounDao?.findByPlural(word)
        if (noun != null) {
            val syntheticWord = WordEntity(
                id = -noun.id,
                germanWord = noun.lemma,
                article = noun.article ?: "das",
                plural = noun.nominativPlural,
                wordType = "noun",
                arabicTranslation = ""
            )
            return WordWithDetails(
                word = syntheticWord,
                nounData = noun
            )
        }

        val verb = verbDao?.findByInfinitive(word) ?: verbDao?.findByForm(word)
        if (verb != null) {
            val syntheticWord = WordEntity(
                id = -verb.id,
                germanWord = verb.infinitive,
                wordType = "verb",
                arabicTranslation = ""
            )
            return WordWithDetails(
                word = syntheticWord,
                verbData = verb
            )
        }

        return null
    }

    override suspend fun searchWord(query: String): WordSearchResult {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return WordSearchResult.NotFound

        // 1. Exact match on German word
        val exact = wordDao.findExactIgnoreCase(trimmed)
        if (exact != null) {
            val details = getWordWithDetails(exact.id) ?: WordWithDetails(word = exact)
            return WordSearchResult.Exact(wordWithDetails = details)
        }

        // 2. Match on plural form
        val pluralMatch = wordDao.findByPluralIgnoreCase(trimmed)
        if (pluralMatch != null) {
            val details = getWordWithDetails(pluralMatch.id) ?: WordWithDetails(word = pluralMatch)
            return WordSearchResult.Exact(
                wordWithDetails = details,
                isPluralMatch = true,
                matchedPlural = trimmed
            )
        }

        // 3. Search suggestions
        val suggestions = wordDao.searchWords(trimmed, limit = 20)
        if (suggestions.isNotEmpty()) {
            return WordSearchResult.Suggestions(items = suggestions)
        }

        return WordSearchResult.NotFound
    }

    override suspend fun searchVocabulary(query: String): List<WordEntity> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return emptyList()
        return wordDao.searchWords(trimmed, limit = 30)
    }

    override suspend fun findWordsByArabic(arabic: String): List<WordEntity> {
        val trimmed = arabic.trim()
        if (trimmed.isEmpty()) return emptyList()
        return wordDao.findByArabic(trimmed, limit = 15)
    }

    override suspend fun getInitialVocabulary(): List<WordEntity> {
        return wordDao.getVocabularyList(50)
    }

    override suspend fun getVocabularyCount(): Int {
        return wordDao.countWords()
    }

    override fun getSearchHistoryCombined(limit: Int): Flow<List<HistoryItemCombined>> {
        return historyDao?.getRecentHistory(limit)?.map { historyEntities ->
            historyEntities.map { h ->
                val word = h.wordId?.let { wordDao.getById(it) }
                HistoryItemCombined(history = h, word = word)
            }
        } ?: flow { emit(emptyList()) }
    }

    override suspend fun recordHistory(
        query: String,
        wordId: Long?,
        resultType: String,
        resultText: String,
        sourceLanguage: String,
        targetLanguage: String
    ): Long {
        if (historyDao == null || query.isBlank()) return -1L
        val trimmed = query.trim()
        val existing = historyDao.getHistoryByQuery(trimmed)
        return if (existing != null) {
            val updated = existing.copy(
                wordId = wordId ?: existing.wordId,
                resultType = resultType,
                resultText = resultText.ifBlank { existing.resultText },
                sourceLanguage = sourceLanguage,
                targetLanguage = targetLanguage,
                searchedAt = System.currentTimeMillis()
            )
            historyDao.updateHistory(updated)
            existing.id
        } else {
            val newEntry = HistoryEntity(
                wordId = wordId,
                query = trimmed,
                sourceLanguage = sourceLanguage,
                targetLanguage = targetLanguage,
                resultType = resultType,
                resultText = resultText,
                searchedAt = System.currentTimeMillis()
            )
            historyDao.insertHistory(newEntry)
        }
    }

    override suspend fun toggleHistorySaved(id: Long) {
        val item = historyDao?.getHistoryById(id) ?: return
        historyDao.updateSavedStatus(id, !item.isSaved)
    }

    override suspend fun deleteHistoryItem(id: Long) {
        historyDao?.deleteHistoryById(id)
    }

    override suspend fun clearHistory() {
        historyDao?.clearHistory()
    }
}

