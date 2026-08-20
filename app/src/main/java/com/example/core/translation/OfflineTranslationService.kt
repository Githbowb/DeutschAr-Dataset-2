package com.example.core.translation

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

/**
 * Offline Translation Service with ML Kit and Local Database fallback support.
 */
class OfflineTranslationService(
    private val context: Context
) : TranslationService {

    companion object {
        private const val TAG = "OfflineTranslationService"
    }

    private val triLanguageRepo by lazy { TriLanguageRepository.getInstance(context) }
    private val mlKitManager by lazy { MlKitTranslatorManager() }

    private val _downloadStatus = MutableStateFlow<ModelDownloadStatus>(ModelDownloadStatus.Unknown)
    override val downloadStatus: StateFlow<ModelDownloadStatus> = _downloadStatus.asStateFlow()

    override suspend fun checkModelStatus(): ModelDownloadStatus = withContext(Dispatchers.IO) {
        _downloadStatus.value = ModelDownloadStatus.Checking
        val isDeDownloaded = mlKitManager.isModelDownloaded("de")
        val isArDownloaded = mlKitManager.isModelDownloaded("ar")
        val isEnDownloaded = mlKitManager.isModelDownloaded("en")

        val status = if (isDeDownloaded && isArDownloaded && isEnDownloaded) {
            ModelDownloadStatus.Downloaded
        } else {
            ModelDownloadStatus.NeedsDownload
        }
        _downloadStatus.value = status
        status
    }

    override suspend fun isModelDownloaded(languageCode: String): Boolean {
        return mlKitManager.isModelDownloaded(languageCode)
    }

    override suspend fun downloadLanguageModels(requireWifi: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        _downloadStatus.value = ModelDownloadStatus.Downloading(
            currentLanguage = "de/en/ar",
            message = "Downloading on-device translation packages..."
        )
        val res = mlKitManager.downloadModels(listOf("de", "en", "ar"), requireWifi)
        if (res.isSuccess) {
            _downloadStatus.value = ModelDownloadStatus.Downloaded
        } else {
            _downloadStatus.value = ModelDownloadStatus.Error(res.exceptionOrNull()?.message ?: "Download failed")
        }
        res
    }

    override suspend fun translate(
        text: String,
        direction: Direction
    ): Result<String> = translateDetailed(text, direction).map { it.translatedText }

    override suspend fun translate(
        text: String,
        sourceLanguageCode: String,
        targetLanguageCode: String
    ): Result<String> {
        val direction = Direction.fromLanguages(sourceLanguageCode, targetLanguageCode)
        return translate(text, direction)
    }

    override suspend fun translateDetailed(
        text: String,
        direction: Direction
    ): Result<TranslationResult> = withContext(Dispatchers.IO) {
        val cleanText = text.trim()
        if (cleanText.isBlank()) {
            return@withContext Result.success(
                TranslationResult(
                    german = "",
                    germanArticle = null,
                    english = "",
                    arabic = "",
                    source = TranslationSource.LOCAL_DB,
                    sourceLanguage = direction.sourceLanguage,
                    targetLanguage = direction.targetLanguage
                )
            )
        }

        if (direction.sourceLanguage == "de" && direction.targetLanguage == "ar") {
            // German -> Arabic with English pivot & local DB enrichment
            triLanguageRepo.translateTriLanguage(cleanText)
        } else if (direction.sourceLanguage == "ar" && direction.targetLanguage == "de") {
            // Arabic -> German with English pivot & reverse local DB enrichment
            triLanguageRepo.translateArabicToTriLanguage(cleanText)
        } else {
            // Any other direction (e.g. EN <-> DE or EN <-> AR) via ML Kit
            mlKitManager.translate(cleanText, direction.sourceLanguage, direction.targetLanguage).map { trans ->
                TranslationResult(
                    german = if (direction.sourceLanguage == "de") cleanText else if (direction.targetLanguage == "de") trans else "",
                    germanArticle = null,
                    english = if (direction.sourceLanguage == "en") cleanText else if (direction.targetLanguage == "en") trans else "",
                    arabic = if (direction.sourceLanguage == "ar") cleanText else if (direction.targetLanguage == "ar") trans else "",
                    source = TranslationSource.ML_KIT_FALLBACK,
                    sourceLanguage = direction.sourceLanguage,
                    targetLanguage = direction.targetLanguage
                )
            }
        }
    }

    override fun getAvailableLanguages(): List<Language> {
        return listOf(
            Language("de", "German (Deutsch)"),
            Language("en", "English (الإنجليزية)"),
            Language("ar", "Arabic (العربية)")
        )
    }

    override fun close() {
        Log.i(TAG, "Closing OfflineTranslationService")
        mlKitManager.close()
        triLanguageRepo.close()
    }
}
