package com.example.core.translation

import kotlinx.coroutines.flow.StateFlow

interface TranslationService {
    val downloadStatus: StateFlow<ModelDownloadStatus>

    suspend fun translate(
        text: String,
        direction: Direction
    ): Result<String>

    suspend fun translate(
        text: String,
        sourceLanguageCode: String,
        targetLanguageCode: String
    ): Result<String>

    suspend fun translateDetailed(
        text: String,
        direction: Direction
    ): Result<TranslationResult>

    suspend fun checkModelStatus(): ModelDownloadStatus
    suspend fun isModelDownloaded(languageCode: String): Boolean
    suspend fun downloadLanguageModels(requireWifi: Boolean = true): Result<Unit>
    fun getAvailableLanguages(): List<Language>
    fun close()
}
