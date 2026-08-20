package com.example.feature.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.DeutschArApp
import com.example.core.database.WordRepository
import com.example.core.translation.MlKitTranslatorManager
import com.example.ui.theme.AppLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class ModelDownloadState {
    data object Idle : ModelDownloadState()
    data class Downloading(val currentLanguage: String, val progressMessage: String) : ModelDownloadState()
    data class Success(val message: String) : ModelDownloadState()
    data class Error(val error: String) : ModelDownloadState()
}

data class LanguageModelStatus(
    val code: String,
    val nameAr: String,
    val nameNative: String,
    val flag: String,
    val isDownloaded: Boolean = false,
    val isDownloading: Boolean = false,
    val approxSize: String = "~30 MB"
)

data class SettingsUiState(
    val appLanguage: AppLanguage = AppLanguage.ARABIC,
    val germanModel: LanguageModelStatus = LanguageModelStatus("de", "الألمانية", "Deutsch", "🇩🇪"),
    val arabicModel: LanguageModelStatus = LanguageModelStatus("ar", "العربية", "العربية", "🇸🇦"),
    val englishModel: LanguageModelStatus = LanguageModelStatus("en", "الإنجليزية", "English", "🇬🇧"),
    val downloadAllState: ModelDownloadState = ModelDownloadState.Idle,
    val requireWifiOnly: Boolean = false,
    val totalWordsCount: Int = 0,
    val totalVerbsCount: Int = 0,
    val totalNounsCount: Int = 0,
    val isFirstTimeBannerDismissed: Boolean = false
) {
    val areAllModelsDownloaded: Boolean
        get() = germanModel.isDownloaded && arabicModel.isDownloaded && englishModel.isDownloaded

    val downloadedModelsCount: Int
        get() = (if (germanModel.isDownloaded) 1 else 0) +
                (if (arabicModel.isDownloaded) 1 else 0) +
                (if (englishModel.isDownloaded) 1 else 0)
}

class SettingsViewModel(
    private val mlKitTranslator: MlKitTranslatorManager = DeutschArApp.instance.mlKitTranslator,
    private val wordRepository: WordRepository = DeutschArApp.instance.wordRepository,
    private val context: Context = DeutschArApp.instance.applicationContext
) : ViewModel() {

    private val prefs = context.getSharedPreferences("deutsch_ar_settings", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            appLanguage = AppLanguage.fromCode(prefs.getString("pref_app_language", "ar") ?: "ar"),
            requireWifiOnly = prefs.getBoolean("pref_require_wifi", false),
            isFirstTimeBannerDismissed = prefs.getBoolean("pref_banner_dismissed", false)
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        refreshModelStatuses()
        loadDatabaseStats()
    }

    fun setAppLanguage(language: AppLanguage) {
        prefs.edit().putString("pref_app_language", language.code).apply()
        _uiState.update { it.copy(appLanguage = language) }
    }

    fun refreshModelStatuses() {
        viewModelScope.launch {
            val isDeDownloaded = mlKitTranslator.isModelDownloaded("de")
            val isArDownloaded = mlKitTranslator.isModelDownloaded("ar")
            val isEnDownloaded = mlKitTranslator.isModelDownloaded("en")

            _uiState.update { state ->
                state.copy(
                    germanModel = state.germanModel.copy(isDownloaded = isDeDownloaded, isDownloading = false),
                    arabicModel = state.arabicModel.copy(isDownloaded = isArDownloaded, isDownloading = false),
                    englishModel = state.englishModel.copy(isDownloaded = isEnDownloaded, isDownloading = false)
                )
            }
        }
    }

    private fun loadDatabaseStats() {
        viewModelScope.launch {
            val totalWords = wordRepository.getVocabularyCount()
            _uiState.update {
                it.copy(
                    totalWordsCount = totalWords,
                    totalVerbsCount = 14,
                    totalNounsCount = 20
                )
            }
        }
    }

    fun toggleRequireWifi(enabled: Boolean) {
        prefs.edit().putBoolean("pref_require_wifi", enabled).apply()
        _uiState.update { it.copy(requireWifiOnly = enabled) }
    }

    fun dismissFirstTimeBanner() {
        prefs.edit().putBoolean("pref_banner_dismissed", true).apply()
        _uiState.update { it.copy(isFirstTimeBannerDismissed = true) }
    }

    fun downloadAllModels() {
        if (_uiState.value.downloadAllState is ModelDownloadState.Downloading) return

        viewModelScope.launch {
            val requireWifi = _uiState.value.requireWifiOnly
            _uiState.update {
                it.copy(
                    downloadAllState = ModelDownloadState.Downloading("de", "جاري تحميل الحزمة الألمانية...")
                )
            }

            val deRes = mlKitTranslator.downloadSingleModel("de", requireWifi)
            if (deRes.isFailure) {
                _uiState.update {
                    it.copy(downloadAllState = ModelDownloadState.Error("تعذر تحميل الحزمة الألمانية. تحقق من الاتصال بالإنترنت."))
                }
                refreshModelStatuses()
                return@launch
            }

            _uiState.update {
                it.copy(
                    downloadAllState = ModelDownloadState.Downloading("ar", "جاري تحميل الحزمة العربية...")
                )
            }

            val arRes = mlKitTranslator.downloadSingleModel("ar", requireWifi)
            if (arRes.isFailure) {
                _uiState.update {
                    it.copy(downloadAllState = ModelDownloadState.Error("تعذر تحميل الحزمة العربية. تحقق من الاتصال بالإنترنت."))
                }
                refreshModelStatuses()
                return@launch
            }

            _uiState.update {
                it.copy(
                    downloadAllState = ModelDownloadState.Downloading("en", "جاري تحميل الحزمة الإنجليزية...")
                )
            }

            val enRes = mlKitTranslator.downloadSingleModel("en", requireWifi)
            if (enRes.isFailure) {
                _uiState.update {
                    it.copy(downloadAllState = ModelDownloadState.Error("تعذر تحميل الحزمة الإنجليزية. تحقق من الاتصال بالإنترنت."))
                }
                refreshModelStatuses()
                return@launch
            }

            refreshModelStatuses()
            _uiState.update {
                it.copy(
                    downloadAllState = ModelDownloadState.Success("تم تحميل جميع موديلات الترجمة بنجاح! التطبيق جاهز للعمل بدون إنترنت.")
                )
            }
        }
    }

    fun downloadSingleModel(languageCode: String) {
        viewModelScope.launch {
            updateModelDownloadingState(languageCode, true)
            val requireWifi = _uiState.value.requireWifiOnly
            val result = mlKitTranslator.downloadSingleModel(languageCode, requireWifi)
            updateModelDownloadingState(languageCode, false)
            refreshModelStatuses()

            if (result.isFailure) {
                _uiState.update {
                    it.copy(downloadAllState = ModelDownloadState.Error("فشل تحميل حزمة $languageCode. يرجى إعادة المحاولة."))
                }
            } else {
                _uiState.update {
                    it.copy(downloadAllState = ModelDownloadState.Success("تم تحميل حزمة $languageCode بنجاح!"))
                }
            }
        }
    }

    fun deleteSingleModel(languageCode: String) {
        viewModelScope.launch {
            mlKitTranslator.deleteModel(languageCode)
            refreshModelStatuses()
            _uiState.update {
                it.copy(downloadAllState = ModelDownloadState.Idle)
            }
        }
    }

    private fun updateModelDownloadingState(languageCode: String, isDownloading: Boolean) {
        _uiState.update { state ->
            when (languageCode) {
                "de" -> state.copy(germanModel = state.germanModel.copy(isDownloading = isDownloading))
                "ar" -> state.copy(arabicModel = state.arabicModel.copy(isDownloading = isDownloading))
                "en" -> state.copy(englishModel = state.englishModel.copy(isDownloading = isDownloading))
                else -> state
            }
        }
    }

    class Factory(
        private val context: Context = DeutschArApp.instance.applicationContext
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(
                mlKitTranslator = DeutschArApp.instance.mlKitTranslator,
                wordRepository = DeutschArApp.instance.wordRepository,
                context = context
            ) as T
        }
    }
}
