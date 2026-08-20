package com.example.core.translation

import com.example.core.database.GermanNounEntity
import com.example.core.database.GermanVerbEntity

data class Language(
    val code: String,
    val displayName: String
)

enum class TranslationSource {
    LOCAL_DB,
    ML_KIT_FALLBACK
}

typealias Source = TranslationSource

/**
 * Conjugation data for German verbs.
 */
data class VerbConjugationResult(
    val infinitive: String,
    val auxiliary: String? = null,           // "haben" / "sein"
    val prasensIch: String? = null,
    val prasensDu: String? = null,
    val prasensErSieEs: String? = null,
    val prasensWir: String? = null,
    val prasensIhr: String? = null,
    val prasensSie: String? = null,
    val prateritumIch: String? = null,
    val partizipZwei: String? = null,
    val konjunktivZweiIch: String? = null,
    val imperativSingular: String? = null,
    val imperativPlural: String? = null,
    val englishTranslation: String = "",
    val arabicTranslation: String = ""
)

/**
 * Unified Trilingual Result for the 3-Way Lookup + Fallback System (German ↔ English ↔ Arabic).
 * Contains German (+ article/conjugation if available), English, and Arabic regardless of input language.
 */
data class TrilingualResult(
    val german: String,
    val germanArticle: String? = null,      // From Local DB only, null if not available
    val english: String,
    val arabic: String,
    val source: TranslationSource = TranslationSource.LOCAL_DB,
    val plural: String? = null,
    val cases: String? = null,
    val posType: FunctionWordType? = null,
    val verbData: VerbConjugationResult? = null,
    val nounData: GermanNounEntity? = null,
    val verbEntity: GermanVerbEntity? = null,
    val intermediateEnglish: String? = null,
    val diagnostics: String? = null,
    val sourceLanguage: String = "de",
    val targetLanguage: String = "ar"
) {
    // Backwards compatibility properties
    val germanWord: String get() = german
    val article: String? get() = germanArticle
    val translatedText: String get() = arabic.ifEmpty { english.ifEmpty { german } }
    val resultType: String get() = if (source == TranslationSource.LOCAL_DB) "local_db" else "ml_kit_fallback"

    // Secondary constructor for legacy/convenience calls using germanWord / article parameter names
    constructor(
        germanWord: String,
        article: String?,
        english: String,
        arabic: String,
        source: TranslationSource = TranslationSource.LOCAL_DB,
        plural: String? = null,
        cases: String? = null,
        posType: FunctionWordType? = null,
        verbData: VerbConjugationResult? = null,
        nounData: GermanNounEntity? = null,
        intermediateEnglish: String? = null,
        translatedText: String = arabic.ifEmpty { english },
        sourceLanguage: String = "de",
        targetLanguage: String = "ar",
        resultType: String = if (source == TranslationSource.LOCAL_DB) "local_db" else "ml_kit_fallback",
        diagnostics: String? = null
    ) : this(
        german = germanWord,
        germanArticle = article,
        english = english,
        arabic = arabic,
        source = source,
        plural = plural,
        cases = cases,
        posType = posType,
        verbData = verbData,
        nounData = nounData,
        intermediateEnglish = intermediateEnglish,
        diagnostics = diagnostics,
        sourceLanguage = sourceLanguage,
        targetLanguage = targetLanguage
    )

    // Secondary constructor for simple text translation
    constructor(
        translatedText: String,
        resultType: String = "offline_translation",
        sourceLanguage: String = "de",
        targetLanguage: String = "ar",
        diagnostics: String? = null,
        posType: FunctionWordType? = null,
        verbData: VerbConjugationResult? = null,
        intermediateEnglish: String? = null
    ) : this(
        german = if (sourceLanguage == "de") "" else translatedText,
        germanArticle = null,
        english = intermediateEnglish ?: "",
        arabic = if (targetLanguage == "ar") translatedText else "",
        source = TranslationSource.ML_KIT_FALLBACK,
        plural = null,
        cases = null,
        posType = posType,
        verbData = verbData,
        nounData = null,
        intermediateEnglish = intermediateEnglish,
        diagnostics = diagnostics,
        sourceLanguage = sourceLanguage,
        targetLanguage = targetLanguage
    )
}

typealias TranslationResult = TrilingualResult

sealed interface ModelDownloadStatus {
    data object Unknown : ModelDownloadStatus
    data object Checking : ModelDownloadStatus
    data object NeedsDownload : ModelDownloadStatus
    data class Downloading(
        val currentLanguage: String,
        val progress: Float? = null,
        val downloadedBytes: Long = 0L,
        val totalBytes: Long = 0L,
        val currentFileName: String = "",
        val speedBytesPerSec: Long = 0L,
        val message: String = "جاري تنزيل حزم الترجمة..."
    ) : ModelDownloadStatus
    data class Verifying(val message: String = "جاري التحقق من الموديل...") : ModelDownloadStatus
    data class Loading(val message: String = "جاري تهيئة نموذج الذكاء الاصطناعي في الذاكرة...") : ModelDownloadStatus
    data object Downloaded : ModelDownloadStatus
    data class Error(val message: String) : ModelDownloadStatus
}

sealed interface TranslationState {
    data object Idle : TranslationState
    data class DownloadingModel(val message: String = "جاري تنزيل حزم اللغة...") : TranslationState
    data object Translating : TranslationState
    data class Success(val result: TranslationResult) : TranslationState
    data class Error(
        val message: String,
        val details: String? = null,
        val stackTrace: String? = null,
        val callPoint: String? = null
    ) : TranslationState
}
