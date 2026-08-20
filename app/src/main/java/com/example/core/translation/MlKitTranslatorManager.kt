package com.example.core.translation

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.tasks.await
import java.util.concurrent.ConcurrentHashMap

class MlKitTranslatorManager {

    private val translators = ConcurrentHashMap<String, Translator>()
    private val modelManager = RemoteModelManager.getInstance()

    private fun getTranslator(sourceLang: String, targetLang: String): Translator {
        val key = "$sourceLang-$targetLang"
        return translators.getOrPut(key) {
            val sourceCode = mapToLanguageCode(sourceLang)
            val targetCode = mapToLanguageCode(targetLang)
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(sourceCode)
                .setTargetLanguage(targetCode)
                .build()
            Translation.getClient(options)
        }
    }

    fun mapToLanguageCode(lang: String): String {
        return when (lang.lowercase()) {
            "de", "german" -> TranslateLanguage.GERMAN
            "ar", "arabic" -> TranslateLanguage.ARABIC
            "en", "english" -> TranslateLanguage.ENGLISH
            else -> TranslateLanguage.ENGLISH
        }
    }

    suspend fun isModelDownloaded(languageCode: String): Boolean {
        return try {
            val mlkitLang = mapToLanguageCode(languageCode)
            val model = TranslateRemoteModel.Builder(mlkitLang).build()
            modelManager.isModelDownloaded(model).await()
        } catch (_: Exception) {
            false
        }
    }

    suspend fun downloadSingleModel(languageCode: String, requireWifi: Boolean = false): Result<Unit> {
        return try {
            val mlkitLang = mapToLanguageCode(languageCode)
            val model = TranslateRemoteModel.Builder(mlkitLang).build()
            val builder = DownloadConditions.Builder()
            if (requireWifi) builder.requireWifi()
            modelManager.download(model, builder.build()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteModel(languageCode: String): Result<Unit> {
        return try {
            val mlkitLang = mapToLanguageCode(languageCode)
            val model = TranslateRemoteModel.Builder(mlkitLang).build()
            modelManager.deleteDownloadedModel(model).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun downloadModels(languages: List<String>, requireWifi: Boolean = false): Result<Unit> {
        return try {
            val builder = DownloadConditions.Builder()
            if (requireWifi) builder.requireWifi()
            val conditions = builder.build()
            for (lang in languages) {
                val mlkitLang = mapToLanguageCode(lang)
                val model = TranslateRemoteModel.Builder(mlkitLang).build()
                modelManager.download(model, conditions).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun translate(text: String, sourceLang: String, targetLang: String): Result<String> {
        return try {
            val result = translateDirect(text, sourceLang, targetLang)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun translateDirect(text: String, sourceLang: String, targetLang: String): String {
        val s = sourceLang.lowercase()
        val t = targetLang.lowercase()
        if (s == t || text.isBlank()) return text

        // If direct AR <-> DE is requested, pivot through English
        if ((s == "de" && t == "ar") || (s == "ar" && t == "de")) {
            val intermediate = try {
                translateDirect(text, s, "en")
            } catch (_: Exception) {
                text
            }
            return translateDirect(intermediate, "en", t)
        }

        val translator = getTranslator(s, t)
        val conditions = DownloadConditions.Builder().build()
        try {
            translator.downloadModelIfNeeded(conditions).await()
        } catch (_: Exception) {
            // Model download might still be in progress or offline
        }
        return try {
            translator.translate(text).await()
        } catch (e: Exception) {
            // If translation still fails (e.g. model not yet ready), return original text or rethrow
            throw e
        }
    }

    suspend fun translateWithEnglishPivot(text: String, direction: Direction): TranslationResult {
        return when (direction) {
            Direction.GERMAN_TO_ARABIC -> {
                val english = translateDirect(text, "de", "en")
                val arabic = translateDirect(english, "en", "ar")
                TranslationResult(
                    german = text,
                    english = english,
                    arabic = arabic,
                    source = TranslationSource.ML_KIT_FALLBACK,
                    intermediateEnglish = english,
                    sourceLanguage = "de",
                    targetLanguage = "ar"
                )
            }
            Direction.ARABIC_TO_GERMAN -> {
                val english = translateDirect(text, "ar", "en")
                val german = translateDirect(english, "en", "de")
                TranslationResult(
                    german = german,
                    english = english,
                    arabic = text,
                    source = TranslationSource.ML_KIT_FALLBACK,
                    intermediateEnglish = english,
                    sourceLanguage = "ar",
                    targetLanguage = "de"
                )
            }
        }
    }

    fun close() {
        translators.values.forEach { it.close() }
        translators.clear()
    }
}

