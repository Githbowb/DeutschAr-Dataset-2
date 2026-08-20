package com.example.core.tts

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

sealed interface TtsState {
    data object Uninitialized : TtsState
    data object Initializing : TtsState
    data object Ready : TtsState
    data object LanguageMissingData : TtsState
    data object LanguageNotSupported : TtsState
    data class Speaking(val utteranceId: String) : TtsState
    data class Error(val message: String) : TtsState
}

/**
 * Robust Text-to-Speech wrapper configured for German pronunciation.
 * Detects missing voice data and provides intent triggers for the user to install TTS voices.
 */
class GermanTtsHelper(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private val _ttsState = MutableStateFlow<TtsState>(TtsState.Uninitialized)
    val ttsState: StateFlow<TtsState> = _ttsState.asStateFlow()

    init {
        initTts()
    }

    private fun initTts() {
        try {
            _ttsState.value = TtsState.Initializing
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Throwable) {
            Log.e("GermanTtsHelper", "TTS initialization failed: ${e.message}", e)
            _ttsState.value = TtsState.LanguageNotSupported
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.GERMAN)
            when (result) {
                TextToSpeech.LANG_MISSING_DATA -> {
                    Log.w("GermanTtsHelper", "German TTS data is missing on this device.")
                    _ttsState.value = TtsState.LanguageMissingData
                }
                TextToSpeech.LANG_NOT_SUPPORTED -> {
                    Log.w("GermanTtsHelper", "German language is not supported by current TTS engine.")
                    _ttsState.value = TtsState.LanguageNotSupported
                }
                else -> {
                    tts?.setPitch(1.0f)
                    tts?.setSpeechRate(0.95f)
                    tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            _ttsState.value = TtsState.Speaking(utteranceId ?: "default")
                        }

                        override fun onDone(utteranceId: String?) {
                            _ttsState.value = TtsState.Ready
                        }

                        @Deprecated("Deprecated in Java")
                        override fun onError(utteranceId: String?) {
                            _ttsState.value = TtsState.Ready
                        }
                    })
                    _ttsState.value = TtsState.Ready
                }
            }
        } else {
            Log.e("GermanTtsHelper", "Failed to initialize TextToSpeech engine. Status: $status")
            _ttsState.value = TtsState.Error("فشل تهيئة محرك النطق الصوتي")
        }
    }

    /**
     * Speaks the given German text aloud.
     */
    fun speak(text: String, utteranceId: String = System.currentTimeMillis().toString()) {
        if (text.isBlank()) return
        val current = _ttsState.value
        if (current is TtsState.LanguageMissingData || current is TtsState.LanguageNotSupported) {
            // State remains missing data so UI can show prompt
            return
        }
        if (tts == null) {
            initTts()
            return
        }
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stop() {
        tts?.stop()
        if (_ttsState.value is TtsState.Speaking) {
            _ttsState.value = TtsState.Ready
        }
    }

    /**
     * Launches system TTS settings or installer so user can download the German voice pack.
     */
    fun openTtsSettings(context: Context) {
        try {
            val installIntent = Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA)
            installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(installIntent)
        } catch (e: Exception) {
            try {
                val settingsIntent = Intent(Settings.ACTION_SETTINGS)
                settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(settingsIntent)
            } catch (e2: Exception) {
                Log.e("GermanTtsHelper", "Cannot open TTS settings: ${e2.message}")
            }
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        _ttsState.value = TtsState.Uninitialized
    }
}
