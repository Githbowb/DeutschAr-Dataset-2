package com.example.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.DeutschArApp
import com.example.core.database.ExampleEntity
import com.example.core.database.GermanVerbEntity
import com.example.core.database.WordEntity
import com.example.core.database.WordRepository
import com.example.core.database.WordWithDetails
import com.example.core.translation.CommonPhrases
import com.example.core.translation.DictionaryLexicon
import com.example.core.translation.LanguageDetector
import com.example.core.translation.MlKitTranslatorManager
import com.example.core.translation.TriLanguageRepository
import com.example.core.translation.TranslationService
import com.example.core.translation.VerbConjugationResult
import com.example.core.translation.VerbConjugator
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchViewModel(
    private val wordRepository: WordRepository = DeutschArApp.instance.wordRepository,
    private val triLanguageRepository: TriLanguageRepository = DeutschArApp.instance.triLanguageRepository,
    private val mlKitTranslator: MlKitTranslatorManager = DeutschArApp.instance.mlKitTranslator,
    private val translationService: TranslationService? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val words = wordRepository.getInitialVocabulary()
            _uiState.update { it.copy(suggestions = words, resultState = SearchResultState.Idle) }
        }
    }

    fun setSelectedTab(tab: String) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    /**
     * Updates the active input source language (German "de", English "en", Arabic "ar").
     * Defaults to German "de".
     * If user already entered text, immediately triggers search with the newly selected language.
     */
    fun setSourceLanguage(langCode: String) {
        val validCode = when (langCode.lowercase()) {
            "en", "english" -> "en"
            "ar", "arabic" -> "ar"
            else -> "de"
        }
        _uiState.update { it.copy(sourceLanguage = validCode) }
        val currentQuery = _uiState.value.query.trim()
        if (currentQuery.isNotEmpty()) {
            performSearch(currentQuery)
        }
    }

    /**
     * Called on every text change.
     * MANUAL SEARCH ONLY: Does NOT perform translation or database lookup on keystroke.
     * Only updates the input text and refreshes local autocomplete suggestions.
     */
    fun onQueryChanged(newQuery: String) {
        _uiState.update { it.copy(query = newQuery) }
        searchJob?.cancel()

        if (newQuery.isBlank()) {
            _uiState.update { it.copy(resultState = SearchResultState.Idle, isTranslating = false) }
            viewModelScope.launch {
                val words = wordRepository.getInitialVocabulary()
                _uiState.update { it.copy(suggestions = words) }
            }
        } else {
            val selectedLang = _uiState.value.sourceLanguage
            viewModelScope.launch {
                when (selectedLang) {
                    "ar" -> {
                        val matches = wordRepository.findWordsByArabic(newQuery.trim())
                        _uiState.update { it.copy(suggestions = matches) }
                    }
                    "en" -> {
                        val matches = wordRepository.searchVocabulary(newQuery.trim())
                        _uiState.update { it.copy(suggestions = matches) }
                    }
                    else -> {
                        val matches = wordRepository.searchVocabulary(newQuery.trim())
                        _uiState.update { it.copy(suggestions = matches) }
                    }
                }
            }
        }
    }

    /**
     * Triggered ONLY when the user clicks the explicit "بحث" button or presses Search/Done on the keyboard.
     */
    fun onSearchClicked() {
        val currentQuery = _uiState.value.query.trim()
        if (currentQuery.isNotBlank()) {
            performSearch(currentQuery)
        }
    }

    fun onSuggestionSelected(word: WordEntity) {
        _uiState.update { it.copy(query = word.germanWord, sourceLanguage = "de") }
        performSearch(word.germanWord)
    }

    private fun sanitizeTranslation(trans: String, original: String, fallback: String): String {
        val clean = trans.trim()
        if (clean.isBlank() || clean.equals(original.trim(), ignoreCase = true)) {
            return fallback.ifBlank { "الترجمة غير متوفرة" }
        }
        return clean
    }

    fun performSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _uiState.update { it.copy(isTranslating = true, resultState = SearchResultState.Loading) }

            val sourceLang = _uiState.value.sourceLanguage.ifBlank { "de" }

            val nounDao = (wordRepository as? com.example.core.database.WordRepositoryImpl)?.nounDao
            val verbDao = (wordRepository as? com.example.core.database.WordRepositoryImpl)?.verbDao
            val exampleDao = (wordRepository as? com.example.core.database.WordRepositoryImpl)?.exampleDao

            try {
                when (sourceLang) {
                    "de" -> {
                        // -------------------------------------------------------------
                        // SOURCE: GERMAN -> TARGETS: ARABIC (Primary) + ENGLISH (Secondary)
                        // -------------------------------------------------------------
                        val exactMatch = wordRepository.findExactGermanWord(trimmed)
                        val lexiconEntry = DictionaryLexicon.findByGerman(trimmed)
                        val phraseMatch = CommonPhrases.findPhrase(trimmed)
                        val noun = nounDao?.findByLemma(trimmed) ?: nounDao?.findByPlural(trimmed)
                        val verb = verbDao?.findByInfinitive(trimmed) ?: verbDao?.findByForm(trimmed)

                        val hasExactLexicon = lexiconEntry != null
                        val hasExactDbWord = exactMatch != null && exactMatch.word.id > 0 && exactMatch.word.arabicTranslation.isNotBlank()
                        val hasPhrase = phraseMatch != null

                        val englishTrans: String
                        val arabicTrans: String
                        val isPreseeded = hasExactLexicon || hasPhrase || hasExactDbWord

                        if (hasExactLexicon) {
                            englishTrans = lexiconEntry!!.english
                            arabicTrans = lexiconEntry.arabic
                        } else if (hasPhrase) {
                            englishTrans = phraseMatch!!.english
                            arabicTrans = phraseMatch.arabic
                        } else if (hasExactDbWord) {
                            arabicTrans = exactMatch!!.word.arabicTranslation
                            val rawEng = try { mlKitTranslator.translateDirect(trimmed, "de", "en") } catch (_: Exception) { "" }
                            englishTrans = sanitizeTranslation(rawEng, trimmed, fallback = "Translation")
                        } else {
                            val rawEnglish = try { mlKitTranslator.translateDirect(trimmed, "de", "en") } catch (_: Exception) { "" }
                            englishTrans = sanitizeTranslation(rawEnglish, trimmed, fallback = "Translation")

                            val rawArabic = try { mlKitTranslator.translateDirect(englishTrans, "en", "ar") } catch (_: Exception) { "" }
                            arabicTrans = sanitizeTranslation(rawArabic, trimmed, fallback = "الترجمة")
                        }

                        // Grammatical Part of Speech Classification
                        val isKnownPreposition = lexiconEntry?.wordType == "preposition" || exactMatch?.word?.wordType.equals("preposition", ignoreCase = true)
                        val isKnownConjunction = lexiconEntry?.wordType == "conjunction" || exactMatch?.word?.wordType.equals("conjunction", ignoreCase = true)
                        val isKnownAdverb = lexiconEntry?.wordType == "adverb" || exactMatch?.word?.wordType.equals("adverb", ignoreCase = true)
                        val isKnownPronoun = lexiconEntry?.wordType == "pronoun" || exactMatch?.word?.wordType.equals("pronoun", ignoreCase = true)
                        val isKnownQuestionWord = lexiconEntry?.wordType == "question_word" || exactMatch?.word?.wordType.equals("question_word", ignoreCase = true)
                        val isKnownNumeral = lexiconEntry?.wordType == "numeral" || exactMatch?.word?.wordType.equals("numeral", ignoreCase = true)

                        val isKnownNoun = !isKnownPreposition && !isKnownConjunction && !isKnownAdverb && !isKnownPronoun && !isKnownQuestionWord && !isKnownNumeral && (
                                noun != null ||
                                exactMatch?.nounData != null ||
                                !exactMatch?.word?.article.isNullOrBlank() ||
                                lexiconEntry?.article != null ||
                                exactMatch?.word?.wordType.equals("noun", ignoreCase = true) ||
                                exactMatch?.word?.wordType.equals("nomen", ignoreCase = true) ||
                                (trimmed.firstOrNull()?.isUpperCase() == true && !VerbConjugator.isGermanVerb(trimmed))
                        )

                        val isKnownVerb = !isKnownNoun && !isKnownPreposition && !isKnownConjunction && !isKnownAdverb && !isKnownPronoun && !isKnownQuestionWord && !isKnownNumeral && (
                                verb != null ||
                                exactMatch?.verbData != null ||
                                exactMatch?.word?.wordType.equals("verb", ignoreCase = true) ||
                                VerbConjugator.isGermanVerb(trimmed)
                        )

                        val isKnownAdjective = !isKnownNoun && !isKnownVerb && !isKnownPreposition && !isKnownConjunction && !isKnownAdverb && !isKnownPronoun && !isKnownQuestionWord && !isKnownNumeral && (
                                exactMatch?.word?.wordType.equals("adjective", ignoreCase = true) ||
                                exactMatch?.word?.wordType.equals("adjektiv", ignoreCase = true) ||
                                lexiconEntry?.wordType == "adjective"
                        )

                        val wordType = when {
                            lexiconEntry != null -> lexiconEntry.wordType
                            isKnownPreposition -> "preposition"
                            isKnownConjunction -> "conjunction"
                            isKnownAdverb -> "adverb"
                            isKnownPronoun -> "pronoun"
                            isKnownQuestionWord -> "question_word"
                            isKnownNumeral -> "numeral"
                            isKnownNoun -> "noun"
                            isKnownVerb -> "verb"
                            isKnownAdjective -> "adjective"
                            exactMatch?.word?.wordType != null -> exactMatch.word.wordType
                            else -> null
                        }

                        val verbResult = if (isKnownVerb) {
                            val inf = verb?.infinitive ?: exactMatch?.verbData?.infinitive ?: VerbConjugator.resolveInfinitive(trimmed)
                            VerbConjugator.createConjugation(
                                infinitive = inf,
                                verbEntity = verb ?: exactMatch?.verbData,
                                english = englishTrans,
                                arabic = arabicTrans
                            )
                        } else null

                        val nounData = if (isKnownNoun) (noun ?: exactMatch?.nounData) else null
                        val article = if (isKnownNoun) (exactMatch?.word?.article ?: lexiconEntry?.article ?: noun?.article) else null
                        val plural = if (isKnownNoun) (exactMatch?.word?.plural ?: lexiconEntry?.plural ?: noun?.nominativPlural) else null

                        val wordId = exactMatch?.word?.id?.takeIf { it > 0 }
                        val dbExamples = exactMatch?.examples?.ifEmpty { null }
                            ?: (if (wordId != null) exampleDao?.getExamplesForWord(wordId) else null)?.ifEmpty { null }
                            ?: exampleDao?.findExamplesByText(trimmed)
                            ?: emptyList()

                        val examples = if (dbExamples.isNotEmpty()) {
                            dbExamples
                        } else if (lexiconEntry?.exampleDe != null && lexiconEntry.exampleAr != null) {
                            listOf(ExampleEntity(id = 0, wordId = wordId ?: 0, germanSentence = lexiconEntry.exampleDe, arabicTranslation = lexiconEntry.exampleAr))
                        } else {
                            emptyList()
                        }

                        val dynamicResult = DynamicTranslationResult(
                            sourceText = trimmed,
                            sourceLang = "de",
                            wordType = wordType,
                            article = article,
                            plural = plural,
                            firstTargetLang = "ar",
                            firstTargetTitle = "الترجمة العربية",
                            firstTranslation = arabicTrans,
                            secondTargetLang = "en",
                            secondTargetTitle = "English Translation",
                            secondTranslation = englishTrans,
                            verbData = verbResult,
                            nounData = nounData,
                            examples = examples
                        )

                        wordRepository.recordHistory(
                            query = trimmed,
                            wordId = wordId,
                            resultType = if (isPreseeded) "dictionary" else "mlkit",
                            resultText = "$arabicTrans / $englishTrans",
                            sourceLanguage = "de",
                            targetLanguage = "ar"
                        )

                        _uiState.update { it.copy(isTranslating = false, resultState = SearchResultState.DynamicResult(dynamicResult)) }
                    }

                    "en" -> {
                        // -------------------------------------------------------------
                        // SOURCE: ENGLISH -> TARGETS: GERMAN (Primary) + ARABIC (Secondary)
                        // -------------------------------------------------------------
                        val rawGerman = try { mlKitTranslator.translateDirect(trimmed, "en", "de") } catch (_: Exception) { "" }
                        val germanTrans = sanitizeTranslation(rawGerman, trimmed, fallback = "Übersetzung")

                        val rawArabic = try { mlKitTranslator.translateDirect(trimmed, "en", "ar") } catch (_: Exception) { "" }
                        val arabicTrans = sanitizeTranslation(rawArabic, trimmed, fallback = "الترجمة")

                        // Enrich with German database/lexicon if the German translation is found
                        val exactMatch = wordRepository.findExactGermanWord(germanTrans)
                        val lexiconEntry = DictionaryLexicon.findByGerman(germanTrans)
                        val noun = nounDao?.findByLemma(germanTrans) ?: nounDao?.findByPlural(germanTrans)
                        val verb = verbDao?.findByInfinitive(germanTrans) ?: verbDao?.findByForm(germanTrans)

                        val isKnownVerb = verb != null || exactMatch?.verbData != null || VerbConjugator.isGermanVerb(germanTrans)
                        val isKnownNoun = noun != null || exactMatch?.nounData != null || lexiconEntry?.article != null || !exactMatch?.word?.article.isNullOrBlank()

                        val verbResult = if (isKnownVerb) {
                            val inf = verb?.infinitive ?: exactMatch?.verbData?.infinitive ?: VerbConjugator.resolveInfinitive(germanTrans)
                            VerbConjugator.createConjugation(
                                infinitive = inf,
                                verbEntity = verb ?: exactMatch?.verbData,
                                english = trimmed,
                                arabic = arabicTrans
                            )
                        } else null

                        val nounData = if (isKnownNoun) (noun ?: exactMatch?.nounData) else null
                        val article = if (isKnownNoun) (exactMatch?.word?.article ?: lexiconEntry?.article ?: noun?.article) else null
                        val plural = if (isKnownNoun) (exactMatch?.word?.plural ?: lexiconEntry?.plural ?: noun?.nominativPlural) else null

                        val dynamicResult = DynamicTranslationResult(
                            sourceText = trimmed,
                            sourceLang = "en",
                            wordType = when {
                                isKnownVerb -> "verb"
                                isKnownNoun -> "noun"
                                else -> null
                            },
                            article = article,
                            plural = plural,
                            firstTargetLang = "de",
                            firstTargetTitle = "الترجمة الألمانية",
                            firstTranslation = germanTrans,
                            secondTargetLang = "ar",
                            secondTargetTitle = "الترجمة العربية",
                            secondTranslation = arabicTrans,
                            verbData = verbResult,
                            nounData = nounData,
                            examples = emptyList()
                        )

                        wordRepository.recordHistory(
                            query = trimmed,
                            wordId = exactMatch?.word?.id?.takeIf { it > 0 },
                            resultType = "mlkit",
                            resultText = "$germanTrans / $arabicTrans",
                            sourceLanguage = "en",
                            targetLanguage = "de"
                        )

                        _uiState.update { it.copy(isTranslating = false, resultState = SearchResultState.DynamicResult(dynamicResult)) }
                    }

                    "ar" -> {
                        // -------------------------------------------------------------
                        // SOURCE: ARABIC -> TARGETS: GERMAN (Primary) + ENGLISH (Secondary)
                        // -------------------------------------------------------------
                        val dbArabicMatch = wordRepository.findWordsByArabic(trimmed).firstOrNull()

                        val englishTrans: String
                        val germanTrans: String

                        if (dbArabicMatch != null && dbArabicMatch.germanWord.isNotBlank()) {
                            germanTrans = dbArabicMatch.germanWord
                            val rawEng = try { mlKitTranslator.translateDirect(germanTrans, "de", "en") } catch (_: Exception) { "" }
                            englishTrans = sanitizeTranslation(rawEng, germanTrans, fallback = "Translation")
                        } else {
                            val rawEnglish = try { mlKitTranslator.translateDirect(trimmed, "ar", "en") } catch (_: Exception) { "" }
                            englishTrans = sanitizeTranslation(rawEnglish, trimmed, fallback = "Translation")

                            val rawGerman = try { mlKitTranslator.translateDirect(englishTrans, "en", "de") } catch (_: Exception) { "" }
                            germanTrans = sanitizeTranslation(rawGerman, trimmed, fallback = "Übersetzung")
                        }

                        // Enrich with German database/lexicon if the German translation is found
                        val exactMatch = wordRepository.findExactGermanWord(germanTrans)
                        val lexiconEntry = DictionaryLexicon.findByGerman(germanTrans)
                        val noun = nounDao?.findByLemma(germanTrans) ?: nounDao?.findByPlural(germanTrans)
                        val verb = verbDao?.findByInfinitive(germanTrans) ?: verbDao?.findByForm(germanTrans)

                        val isKnownVerb = verb != null || exactMatch?.verbData != null || VerbConjugator.isGermanVerb(germanTrans)
                        val isKnownNoun = noun != null || exactMatch?.nounData != null || lexiconEntry?.article != null || !exactMatch?.word?.article.isNullOrBlank()

                        val verbResult = if (isKnownVerb) {
                            val inf = verb?.infinitive ?: exactMatch?.verbData?.infinitive ?: VerbConjugator.resolveInfinitive(germanTrans)
                            VerbConjugator.createConjugation(
                                infinitive = inf,
                                verbEntity = verb ?: exactMatch?.verbData,
                                english = englishTrans,
                                arabic = trimmed
                            )
                        } else null

                        val nounData = if (isKnownNoun) (noun ?: exactMatch?.nounData) else null
                        val article = if (isKnownNoun) (exactMatch?.word?.article ?: lexiconEntry?.article ?: noun?.article) else null
                        val plural = if (isKnownNoun) (exactMatch?.word?.plural ?: lexiconEntry?.plural ?: noun?.nominativPlural) else null

                        val dynamicResult = DynamicTranslationResult(
                            sourceText = trimmed,
                            sourceLang = "ar",
                            wordType = when {
                                isKnownVerb -> "verb"
                                isKnownNoun -> "noun"
                                else -> null
                            },
                            article = article,
                            plural = plural,
                            firstTargetLang = "de",
                            firstTargetTitle = "الترجمة الألمانية",
                            firstTranslation = germanTrans,
                            secondTargetLang = "en",
                            secondTargetTitle = "English Translation",
                            secondTranslation = englishTrans,
                            verbData = verbResult,
                            nounData = nounData,
                            examples = emptyList()
                        )

                        wordRepository.recordHistory(
                            query = trimmed,
                            wordId = exactMatch?.word?.id?.takeIf { it > 0 } ?: dbArabicMatch?.id?.takeIf { it > 0 },
                            resultType = if (dbArabicMatch != null) "dictionary" else "mlkit",
                            resultText = "$germanTrans / $englishTrans",
                            sourceLanguage = "ar",
                            targetLanguage = "de"
                        )

                        _uiState.update { it.copy(isTranslating = false, resultState = SearchResultState.DynamicResult(dynamicResult)) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isTranslating = false,
                        resultState = SearchResultState.Error(e.localizedMessage ?: "Translation error")
                    )
                }
            }

            // Update suggestions based on selected source language
            if (sourceLang == "de") {
                val matches = wordRepository.searchVocabulary(trimmed)
                _uiState.update { it.copy(suggestions = matches) }
            } else if (sourceLang == "ar") {
                val matches = wordRepository.findWordsByArabic(trimmed)
                _uiState.update { it.copy(suggestions = matches) }
            } else {
                _uiState.update { it.copy(suggestions = emptyList()) }
            }
        }
    }

    class Factory(
        private val wordRepository: WordRepository,
        private val translationService: TranslationService? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SearchViewModel(
                wordRepository = wordRepository,
                triLanguageRepository = DeutschArApp.instance.triLanguageRepository,
                mlKitTranslator = DeutschArApp.instance.mlKitTranslator,
                translationService = translationService
            ) as T
        }
    }
}
