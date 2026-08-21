package com.example.feature.search

import android.speech.tts.TextToSpeech
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.database.ExampleEntity
import com.example.core.database.GermanNounEntity
import com.example.core.database.GermanVerbEntity
import com.example.core.database.WordEntity
import com.example.core.database.WordWithDetails
import com.example.core.translation.Direction
import com.example.core.translation.TrilingualResult
import com.example.core.translation.VerbConjugationResult
import com.example.feature.cheatsheet.CheatSheetScreen
import com.example.feature.cheatsheet.CheatSheetViewModel
import com.example.feature.history.HistoryScreen
import com.example.feature.history.HistoryViewModel
import com.example.feature.settings.SettingsScreen
import com.example.feature.settings.SettingsViewModel
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkCardBg
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSubCardBg
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.NavBg
import com.example.ui.theme.PrimaryAccent
import com.example.ui.theme.SecondaryAccent
import com.example.ui.theme.TextMain
import com.example.ui.theme.TextMuted
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DerBlue
import com.example.ui.theme.DieRed
import com.example.ui.theme.DasGreen
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.VerbPurple
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.EmeraldLightText
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.OutfitFontFamily
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Translate
import com.example.ui.theme.Atmosphere
import com.example.ui.theme.ambientAtmosphere
import com.example.ui.theme.AppLanguage
import com.example.ui.theme.AppStrings
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextStyle
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Text To Speech initialization
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(context) {
        val engine = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Initialized
            }
        }
        tts = engine
        onDispose {
            engine.stop()
            engine.shutdown()
        }
    }

    fun playAudio(text: String, lang: String = "de") {
        tts?.let { player ->
            val locale = when (lang) {
                "ar" -> Locale("ar")
                "en" -> Locale.ENGLISH
                else -> Locale.GERMAN
            }
            player.language = locale
            player.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts_search_word")
        }
    }

    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.Factory()
    )
    val settingsState by settingsViewModel.uiState.collectAsState()
    val currentLang = settingsState.appLanguage

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // DEUTSCH AR brand logo with rich gradient styling
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "DEUTSCH",
                                fontSize = 22.sp,
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                letterSpacing = 0.8.sp
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.Transparent,
                                border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryAccent.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = "AR",
                                    fontSize = 18.sp,
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = TextStyle(
                                        brush = Brush.linearGradient(
                                            listOf(PrimaryAccent, SecondaryAccent)
                                        )
                                    ),
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            ModernBottomNavBar(
                selectedTab = uiState.selectedTab,
                lang = currentLang,
                onTabSelected = { viewModel.setSelectedTab(it) }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .ambientAtmosphere()
                .padding(paddingValues)
        ) {
            when (uiState.selectedTab) {
                "dictionary" -> {
                    DictionaryMainContent(
                        uiState = uiState,
                        settingsState = settingsState,
                        onDownloadAllModels = { settingsViewModel.downloadAllModels() },
                        onDismissFirstTimeBanner = { settingsViewModel.dismissFirstTimeBanner() },
                        onNavigateToSettings = { viewModel.setSelectedTab("settings") },
                        onNavigateToCheatSheet = { viewModel.setSelectedTab("cheatsheet") },
                        onQueryChanged = { viewModel.onQueryChanged(it) },
                        onSourceLanguageSelected = { viewModel.setSourceLanguage(it) },
                        onSearchClicked = { viewModel.onSearchClicked() },
                        onSuggestionSelected = { viewModel.onSuggestionSelected(it) },
                        onSelectKeyword = { keyword ->
                            viewModel.onQueryChanged(keyword)
                            viewModel.performSearch(keyword)
                        },
                        onPlayAudio = { text, lang -> playAudio(text, lang) },
                        onCopyText = { text ->
                            clipboardManager.setText(AnnotatedString(text))
                            scope.launch {
                                snackbarHostState.showSnackbar(AppStrings.copiedToClipboard(currentLang))
                            }
                        }
                    )
                }

                "history" -> {
                    val historyViewModel: HistoryViewModel = viewModel(
                        factory = HistoryViewModel.Factory(
                            wordRepository = com.example.DeutschArApp.instance.wordRepository
                        )
                    )
                    val historyState by historyViewModel.uiState.collectAsState()
                    HistoryScreen(
                        uiState = historyState,
                        lang = currentLang,
                        onSearchQueryChange = { historyViewModel.onSearchQueryChange(it) },
                        onSetFilterType = { historyViewModel.setFilterType(it) },
                        onToggleSavedOnlyFilter = { historyViewModel.toggleSavedOnlyFilter() },
                        onToggleSaved = { historyViewModel.toggleSaved(it) },
                        onDeleteItem = { historyViewModel.deleteHistoryItem(it) },
                        onClearAll = { historyViewModel.clearAllHistory() }
                    )
                }

                "cheatsheet" -> {
                    val cheatSheetViewModel: CheatSheetViewModel = viewModel(
                        factory = CheatSheetViewModel.Factory()
                    )
                    val cheatSheetState by cheatSheetViewModel.uiState.collectAsState()
                    CheatSheetScreen(
                        uiState = cheatSheetState,
                        onSelectCategory = { cheatSheetViewModel.selectCategory(it) },
                        onToggleRuleExpansion = { cheatSheetViewModel.toggleRuleExpansion(it) }
                    )
                }

                "settings" -> {
                    SettingsScreen(
                        uiState = settingsState,
                        onSetAppLanguage = { settingsViewModel.setAppLanguage(it) },
                        onDownloadAll = { settingsViewModel.downloadAllModels() },
                        onDownloadSingle = { settingsViewModel.downloadSingleModel(it) },
                        onDeleteSingle = { settingsViewModel.deleteSingleModel(it) },
                        onToggleRequireWifi = { settingsViewModel.toggleRequireWifi(it) },
                        onRefreshStatuses = { settingsViewModel.refreshModelStatuses() }
                    )
                }
            }
        }
    }
}

@Composable
fun DictionaryMainContent(
    uiState: SearchUiState,
    settingsState: com.example.feature.settings.SettingsUiState,
    onDownloadAllModels: () -> Unit,
    onDismissFirstTimeBanner: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToCheatSheet: () -> Unit,
    onQueryChanged: (String) -> Unit,
    onSourceLanguageSelected: (String) -> Unit,
    onSearchClicked: () -> Unit,
    onSuggestionSelected: (WordEntity) -> Unit,
    onSelectKeyword: (String) -> Unit,
    onPlayAudio: (String, String) -> Unit,
    onCopyText: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Missing Models Download Alert Banner (Alerts user to download packages, especially when searching)
        if (!settingsState.areAllModelsDownloaded) {
            MissingModelDownloadBanner(
                hasQuery = uiState.query.isNotBlank(),
                query = uiState.query,
                settingsState = settingsState,
                onDownloadAll = onDownloadAllModels,
                onNavigateToSettings = onNavigateToSettings
            )
        }

        // Full Width Search Bar with Language Selector and Dedicated Search Button
        FullWidthSearchBar(
            query = uiState.query,
            lang = settingsState.appLanguage,
            selectedSourceLanguage = uiState.sourceLanguage,
            onSourceLanguageSelected = onSourceLanguageSelected,
            onQueryChanged = onQueryChanged,
            onSearchClicked = onSearchClicked
        )

        // Loading indicator if translating
        if (uiState.isTranslating) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = DarkCardBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Atmosphere.glassBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            color = PrimaryAccent,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.5.dp
                        )
                        Text(
                            text = if (settingsState.appLanguage == AppLanguage.ARABIC) "جاري البحث والترجمة الفورية..." else "Searching & translating...",
                            fontFamily = if (settingsState.appLanguage == AppLanguage.ARABIC) CairoFontFamily else OutfitFontFamily,
                            color = TextMain,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Dynamic Search Results
        when (val state = uiState.resultState) {
            is SearchResultState.DynamicResult -> {
                DetailedDynamicResultCard(
                    data = state.data,
                    onPlayAudio = { text, lang -> onPlayAudio(text, lang) },
                    onCopy = { text -> onCopyText(text) }
                )
            }

            is SearchResultState.WordFound -> {
                DetailedWordCard(
                    details = state.details,
                    englishMeaning = state.englishMeaning ?: "Translation",
                    onPlayAudio = { onPlayAudio(state.details.word.germanWord, "de") },
                    onCopy = { onCopyText(state.details.word.germanWord) }
                )
            }

            is SearchResultState.TrilingualDirect -> {
                DetailedTrilingualCard(
                    result = state.result,
                    onPlayAudio = { onPlayAudio(state.result.german, "de") },
                    onCopy = { onCopyText(state.result.german) }
                )
            }

            is SearchResultState.SentenceTranslation -> {
                DetailedSentenceCard(
                    sourceText = state.sourceText,
                    intermediateEnglish = state.intermediateEnglish,
                    targetText = state.targetText,
                    direction = state.direction,
                    onPlaySource = { onPlayAudio(state.sourceText, if (state.direction == Direction.GERMAN_TO_ARABIC) "de" else "ar") },
                    onPlayTarget = { onPlayAudio(state.targetText, if (state.direction == Direction.GERMAN_TO_ARABIC) "ar" else "de") },
                    onCopy = onCopyText
                )
            }

            is SearchResultState.Error -> {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = DangerRed.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "⚠️",
                            fontSize = 18.sp
                        )
                        Text(
                            text = state.message,
                            fontFamily = if (settingsState.appLanguage == AppLanguage.ARABIC) CairoFontFamily else OutfitFontFamily,
                            color = Color(0xFFFFB4AB),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            SearchResultState.Idle -> {
                if (uiState.query.isBlank()) {
                    DictionaryEmptyState(lang = settingsState.appLanguage)
                }
            }

            SearchResultState.Loading -> {
                // Handled by loading indicator
            }
        }

        // Suggestions List when typing query
        if (uiState.suggestions.isNotEmpty() && uiState.query.isNotBlank() && uiState.resultState is SearchResultState.Idle) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (settingsState.appLanguage == AppLanguage.ARABIC) "مقترحات البحث السريع:" else "Quick Suggestions:",
                    fontFamily = if (settingsState.appLanguage == AppLanguage.ARABIC) CairoFontFamily else OutfitFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = SecondaryAccent
                )
                Text(
                    text = if (settingsState.appLanguage == AppLanguage.ARABIC) "${uiState.suggestions.size} نتيجة" else "${uiState.suggestions.size} results",
                    fontFamily = if (settingsState.appLanguage == AppLanguage.ARABIC) CairoFontFamily else OutfitFontFamily,
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                uiState.suggestions.take(6).forEach { word ->
                    SuggestionCardItem(
                        word = word,
                        onClick = { onSuggestionSelected(word) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

/**
 * Clean & Minimal Empty State when Idle
 */
@Composable
fun DictionaryEmptyState(
    lang: AppLanguage = AppLanguage.ARABIC,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        shape = RoundedCornerShape(22.dp),
        color = DarkCardBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, Atmosphere.glassBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = PrimaryAccent.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryAccent.copy(alpha = 0.35f)),
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = PrimaryAccent,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Text(
                text = AppStrings.emptyStateTitle(lang),
                fontFamily = if (lang == AppLanguage.ARABIC) CairoFontFamily else OutfitFontFamily,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = AppStrings.emptyStateDesc(lang),
                fontFamily = if (lang == AppLanguage.ARABIC) CairoFontFamily else OutfitFontFamily,
                fontSize = 13.sp,
                color = TextMuted,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
        }
    }
}

/**
 * 3. Full-Width Search Bar with Interactive Source Language Selector and Dedicated Search Button
 */
@Composable
fun FullWidthSearchBar(
    query: String,
    lang: AppLanguage = AppLanguage.ARABIC,
    selectedSourceLanguage: String = "de",
    onSourceLanguageSelected: (String) -> Unit,
    onQueryChanged: (String) -> Unit,
    onSearchClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Search Input Field & Dedicated Action Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChanged,
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 54.dp)
                    .testTag("search_text_input"),
                placeholder = {
                    Text(
                        text = AppStrings.searchPlaceholderForLang(selectedSourceLanguage, lang),
                        color = TextMuted,
                        fontSize = 13.sp,
                        fontFamily = if (lang == AppLanguage.ARABIC) CairoFontFamily else OutfitFontFamily
                    )
                },
                textStyle = TextStyle(
                    fontSize = if (query.length > 50) 13.5.sp else 15.sp,
                    fontFamily = if (query.any { it in '\u0600'..'\u06FF' }) CairoFontFamily else OutfitFontFamily,
                    color = Color.White
                ),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = AppStrings.searchButton(lang),
                        tint = PrimaryAccent,
                        modifier = Modifier.size(22.dp)
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(
                            onClick = { onQueryChanged("") },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = AppStrings.clearQuery(lang),
                                tint = TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DarkCardBg,
                    unfocusedContainerColor = DarkCardBg,
                    focusedBorderColor = PrimaryAccent,
                    unfocusedBorderColor = DarkCardBorder,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        keyboardController?.hide()
                        onSearchClicked()
                    }
                ),
                minLines = 1,
                maxLines = 3,
                singleLine = false
            )

            // Dedicated Search Button with Gradient Accent
            Button(
                onClick = {
                    keyboardController?.hide()
                    onSearchClicked()
                },
                modifier = Modifier
                    .height(54.dp)
                    .align(Alignment.CenterVertically)
                    .testTag("search_action_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryAccent,
                    contentColor = DarkBg
                ),
                contentPadding = PaddingValues(horizontal = 18.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = DarkBg
                    )
                    Text(
                        text = AppStrings.searchButton(lang),
                        fontFamily = if (lang == AppLanguage.ARABIC) CairoFontFamily else OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = DarkBg
                    )
                }
            }
        }

        // Interactive Language Selector Panel
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = DarkCardBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header Label
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (lang == AppLanguage.ARABIC) "اختر لغة الإدخال والبحث:" else "Select Input Language:",
                        fontSize = 13.sp,
                        fontFamily = if (lang == AppLanguage.ARABIC) CairoFontFamily else OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = DarkSubCardBg
                    ) {
                        Text(
                            text = if (lang == AppLanguage.ARABIC) "🇩🇪 الافتراضي ألماني" else "🇩🇪 Default: German",
                            fontSize = 11.sp,
                            fontFamily = if (lang == AppLanguage.ARABIC) CairoFontFamily else OutfitFontFamily,
                            color = PrimaryAccent,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                // 3 Large, Highly Touchable Language Selector Buttons (Full Width Segmented Row)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val languages = listOf(
                        Triple("de", "🇩🇪 ألماني", "🇩🇪 German"),
                        Triple("en", "🇬🇧 إنجليزي", "🇬🇧 English"),
                        Triple("ar", "🇪🇬 عربي", "🇪🇬 Arabic")
                    )

                    languages.forEach { (code, arLabel, enLabel) ->
                        val isSelected = selectedSourceLanguage.equals(code, ignoreCase = true)
                        val label = if (lang == AppLanguage.ARABIC) arLabel else enLabel

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) PrimaryAccent else DarkSubCardBg,
                            border = androidx.compose.foundation.BorderStroke(
                                if (isSelected) 1.5.dp else 1.dp,
                                if (isSelected) PrimaryAccent else DarkBorder
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onSourceLanguageSelected(code) }
                                .testTag("lang_btn_$code")
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = DarkBg,
                                            modifier = Modifier
                                                .size(16.dp)
                                                .padding(end = 3.dp)
                                        )
                                    }
                                    Text(
                                        text = label,
                                        fontSize = 13.sp,
                                        fontFamily = if (lang == AppLanguage.ARABIC) CairoFontFamily else OutfitFontFamily,
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                        color = if (isSelected) DarkBg else Color(0xFFDDDDDD),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }

                // Target Translation Hint Badge
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = PrimaryAccent.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(0.8.dp, PrimaryAccent.copy(alpha = 0.25f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚡ " + AppStrings.translationTargetHint(selectedSourceLanguage, lang),
                            fontSize = 12.sp,
                            fontFamily = if (lang == AppLanguage.ARABIC) CairoFontFamily else OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryAccent
                        )

                        Text(
                            text = when (selectedSourceLanguage) {
                                "de" -> "DE ➔ AR + EN"
                                "en" -> "EN ➔ DE + AR"
                                "ar" -> "AR ➔ DE + EN"
                                else -> ""
                            },
                            fontSize = 11.sp,
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryAccent.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }
    }
}

fun getPosInfo(wordType: String?, article: String?, sourceLang: String = "de"): Pair<Color, String> {
    if (!article.isNullOrBlank()) {
        return when (article.lowercase()) {
            "der" -> Pair(DerBlue, "NOMEN • اسم مذكر")
            "die" -> Pair(DieRed, "NOMEN • اسم مؤنث")
            "das" -> Pair(DasGreen, "NOMEN • اسم محايد")
            else -> Pair(SecondaryAccent, "NOMEN • اسم")
        }
    }
    return when (wordType?.lowercase()?.trim()) {
        "preposition", "präposition" -> Pair(Color(0xFF06B6D4), "PRÄPOSITION • حرف جر")
        "conjunction", "konjunktion" -> Pair(PrimaryAccent, "KONJUNKTION • حرف عطف")
        "adverb" -> Pair(Color(0xFF818CF8), "ADVERB • ظرف")
        "pronoun", "pronomen" -> Pair(Color(0xFFFB923C), "PRONOMEN • ضمير")
        "question_word", "fragewort" -> Pair(Color(0xFFF472B6), "FRAGEWORT • اسم استفهام")
        "numeral", "zahlwort", "zahl" -> Pair(Color(0xFFA3E635), "ZAHLWORT • عدد")
        "phrase", "ausdruck" -> Pair(PrimaryAccent, "AUSDRUCK • تعبير")
        "noun", "nomen" -> Pair(SecondaryAccent, "NOMEN • اسم")
        "verb" -> Pair(SecondaryAccent, "VERB • فعل")
        "adjective", "adjektiv" -> Pair(AmberAccent, "ADJEKTIV • صفة")
        else -> {
            when (sourceLang) {
                "ar" -> Pair(PrimaryAccent, "ARABISCH • عربي")
                "en" -> Pair(SecondaryAccent, "ENGLISCH • إنجليزي")
                else -> Pair(SecondaryAccent, (wordType?.uppercase() ?: "WORT"))
            }
        }
    }
}

/**
 * 4. Dynamic Detailed Result Card (Supports all source languages: DE, EN, AR)
 */
@Composable
fun DetailedDynamicResultCard(
    data: DynamicTranslationResult,
    onPlayAudio: (String, String) -> Unit,
    onCopy: (String) -> Unit
) {
    val posInfo = getPosInfo(data.wordType, data.article, data.sourceLang)
    val articleColor = posInfo.first
    val posLabel = posInfo.second

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("detailed_word_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, Atmosphere.glassBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Audio, Copy, Article Badge, Main Word, POS Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Speaker & Copy Icons (Left)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(
                        onClick = { onPlayAudio(data.sourceText, data.sourceLang) },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(DarkSubCardBg)
                            .border(1.dp, PrimaryAccent.copy(alpha = 0.4f), CircleShape)
                            .testTag("play_audio_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "استماع للنطق",
                            tint = PrimaryAccent,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    IconButton(
                        onClick = { onCopy(data.sourceText) },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(DarkSubCardBg)
                            .border(1.dp, DarkBorder, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "نسخ الكلمة",
                            tint = Color.LightGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Main Word + Article + POS Badge (Right)
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!data.article.isNullOrBlank()) {
                        val articleBrush = when (data.article.lowercase()) {
                            "der" -> Atmosphere.derCardGradient
                            "die" -> Atmosphere.dieCardGradient
                            "das" -> Atmosphere.dasCardGradient
                            else -> Atmosphere.sapphireGlowGradient
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.Transparent,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(articleBrush)
                        ) {
                            Text(
                                text = data.article,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    val dynamicFontSize = when {
                        data.sourceText.length <= 12 -> 26.sp
                        data.sourceText.length <= 22 -> 20.sp
                        data.sourceText.length <= 35 -> 17.sp
                        else -> 15.sp
                    }

                    Text(
                        text = data.sourceText,
                        fontSize = dynamicFontSize,
                        fontFamily = if (data.sourceText.any { it in '\u0600'..'\u06FF' }) CairoFontFamily else OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = DarkSubCardBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                    ) {
                        Text(
                            text = posLabel,
                            color = PrimaryAccent,
                            fontSize = 11.sp,
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            if (!data.plural.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = DarkSubCardBg,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = "صيغة الجمع: die ${data.plural}",
                        color = Color(0xFFCBD5E1),
                        fontSize = 13.sp,
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(color = DarkBorder, thickness = 1.dp)

            // Dual Translation Cards (Dynamic based on source language)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Left sub-card: Second Target Language (e.g. English Translation / الترجمة العربية)
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(18.dp),
                    color = DarkSubCardBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = data.secondTargetTitle,
                                fontSize = 11.sp,
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = SecondaryAccent,
                                letterSpacing = 0.5.sp
                            )
                            IconButton(
                                onClick = { onPlayAudio(data.secondTranslation, data.secondTargetLang) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "استماع",
                                    tint = SecondaryAccent.copy(alpha = 0.9f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = data.secondTranslation.ifBlank { "Translation" },
                            fontSize = 16.sp,
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                // Right sub-card: First Target Language (e.g. الترجمة العربية / الترجمة الألمانية)
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(18.dp),
                    color = DarkSubCardBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryAccent.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = data.firstTargetTitle,
                                fontSize = 11.sp,
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryAccent,
                                letterSpacing = 0.5.sp
                            )
                            IconButton(
                                onClick = { onPlayAudio(data.firstTranslation, data.firstTargetLang) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "استماع",
                                    tint = PrimaryAccent.copy(alpha = 0.9f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = data.firstTranslation.ifBlank { "الترجمة" },
                            fontSize = 18.sp,
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryAccent
                        )
                    }
                }
            }

            // Conjugation / Declension Grid if available
            data.verbData?.let { verbResult ->
                ModernVerbConjugationGrid(verbResult = verbResult)
            }

            data.nounData?.let { noun ->
                ModernNounDeclensionGrid(noun = noun)
            }

            // Example Sentence Box if available
            val primaryExample = data.examples.firstOrNull()
            if (primaryExample != null) {
                ExampleSentenceCard(example = primaryExample)
            }
        }
    }
}

/**
 * Detailed Word Result Card
 */
@Composable
fun DetailedWordCard(
    details: WordWithDetails,
    englishMeaning: String,
    onPlayAudio: () -> Unit,
    onCopy: () -> Unit
) {
    val word = details.word

    val posInfo = getPosInfo(word.wordType, word.article, "de")
    val articleColor = posInfo.first
    val posLabel = posInfo.second

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("detailed_word_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, Atmosphere.glassBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Audio, Copy, Article Badge, Word, POS Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Speaker & Copy Icons (Left)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(
                        onClick = onPlayAudio,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(DarkSubCardBg)
                            .border(1.dp, PrimaryAccent.copy(alpha = 0.4f), CircleShape)
                            .testTag("play_audio_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "استماع للنطق",
                            tint = PrimaryAccent,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    IconButton(
                        onClick = onCopy,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(DarkSubCardBg)
                            .border(1.dp, DarkBorder, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "نسخ الكلمة",
                            tint = Color.LightGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Word + Article + POS Badge (Right)
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!word.article.isNullOrBlank()) {
                        val articleBrush = when (word.article.lowercase()) {
                            "der" -> Atmosphere.derCardGradient
                            "die" -> Atmosphere.dieCardGradient
                            "das" -> Atmosphere.dasCardGradient
                            else -> Atmosphere.sapphireGlowGradient
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.Transparent,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(articleBrush)
                        ) {
                            Text(
                                text = word.article,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    val dynamicFontSize = when {
                        word.germanWord.length <= 12 -> 26.sp
                        word.germanWord.length <= 22 -> 20.sp
                        word.germanWord.length <= 35 -> 17.sp
                        else -> 15.sp
                    }

                    Text(
                        text = word.germanWord,
                        fontSize = dynamicFontSize,
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = DarkSubCardBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                    ) {
                        Text(
                            text = posLabel,
                            color = PrimaryAccent,
                            fontSize = 11.sp,
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            if (!word.plural.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = DarkSubCardBg,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = "صيغة الجمع: die ${word.plural}",
                        color = Color(0xFFCBD5E1),
                        fontSize = 13.sp,
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(color = DarkBorder, thickness = 1.dp)

            // Dual Translation Cards (Arabic on right, English on left)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Left sub-card: English Translation
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(18.dp),
                    color = DarkSubCardBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "English Translation",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryAccent,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = englishMeaning.ifBlank { "Translation" },
                            fontSize = 16.sp,
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                // Right sub-card: Arabic Translation in bold bright green text
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(18.dp),
                    color = DarkSubCardBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryAccent.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "الترجمة العربية:",
                            fontSize = 11.sp,
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryAccent,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = word.arabicTranslation.ifBlank { "الترجمة" },
                            fontSize = 18.sp,
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryAccent
                        )
                    }
                }
            }

            // Conjugation / Declension Grid if available
            details.verbData?.let { verb ->
                ModernVerbConjugationGrid(verb = verb)
            }

            details.nounData?.let { noun ->
                ModernNounDeclensionGrid(noun = noun)
            }

            // Example Sentence Box with green vertical line accent
            val primaryExample = details.examples.firstOrNull()
            if (primaryExample != null) {
                ExampleSentenceCard(example = primaryExample)
            }
        }
    }
}

/**
 * Trilingual Result Card (Dynamic)
 */
@Composable
fun DetailedTrilingualCard(
    result: TrilingualResult,
    onPlayAudio: () -> Unit,
    onCopy: () -> Unit
) {
    val articleText = result.germanArticle ?: result.article

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("detailed_trilingual_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, Atmosphere.glassBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Speaker & Copy
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(
                        onClick = onPlayAudio,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(DarkSubCardBg)
                            .border(1.dp, PrimaryAccent.copy(alpha = 0.4f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "استماع",
                            tint = PrimaryAccent,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    IconButton(
                        onClick = onCopy,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(DarkSubCardBg)
                            .border(1.dp, DarkBorder, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "نسخ",
                            tint = Color.LightGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // German word + badge
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!articleText.isNullOrBlank()) {
                        val articleBrush = when (articleText.lowercase()) {
                            "der" -> Atmosphere.derCardGradient
                            "die" -> Atmosphere.dieCardGradient
                            "das" -> Atmosphere.dasCardGradient
                            else -> Atmosphere.sapphireGlowGradient
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.Transparent,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(articleBrush)
                        ) {
                            Text(
                                text = articleText,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    val dynamicFontSize = when {
                        result.german.length <= 12 -> 26.sp
                        result.german.length <= 22 -> 20.sp
                        result.german.length <= 35 -> 17.sp
                        else -> 15.sp
                    }

                    Text(
                        text = result.german,
                        fontSize = dynamicFontSize,
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = DarkSubCardBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                    ) {
                        Text(
                            text = result.posType?.name ?: "3-WAY",
                            color = PrimaryAccent,
                            fontSize = 11.sp,
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            if (!result.plural.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = DarkSubCardBg,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = "صيغة الجمع: die ${result.plural}",
                        color = Color(0xFFCBD5E1),
                        fontSize = 13.sp,
                        fontFamily = CairoFontFamily,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(color = DarkBorder, thickness = 1.dp)

            // Dual Translations
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Left English Card
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(18.dp),
                    color = DarkSubCardBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "English Translation",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryAccent
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = result.english.ifBlank { "Translation" },
                            fontSize = 16.sp,
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                // Right Arabic Card
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(18.dp),
                    color = DarkSubCardBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryAccent.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "الترجمة العربية:",
                            fontSize = 11.sp,
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryAccent
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = result.arabic.ifBlank { "الترجمة" },
                            fontSize = 18.sp,
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryAccent
                        )
                    }
                }
            }

            // Verb Conjugation Table if available
            result.verbData?.let { verbResult ->
                ModernVerbConjugationGrid(verbResult = verbResult)
            }

            result.nounData?.let { noun ->
                ModernNounDeclensionGrid(noun = noun)
            }
        }
    }
}

/**
 * Sentence translation card
 */
@Composable
fun DetailedSentenceCard(
    sourceText: String,
    intermediateEnglish: String,
    targetText: String,
    direction: Direction,
    onPlaySource: () -> Unit,
    onPlayTarget: () -> Unit,
    onCopy: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, Atmosphere.glassBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Source Sentence
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = DarkSubCardBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onPlaySource,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(DarkCardBg)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "استماع للنص الأصلي",
                            tint = SecondaryAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 10.dp)
                    ) {
                        Text(
                            text = if (direction == Direction.GERMAN_TO_ARABIC) "النص الأصلي (الألماني):" else "النص الأصلي (العربي):",
                            fontSize = 12.sp,
                            fontFamily = CairoFontFamily,
                            color = SecondaryAccent,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val sourceFontSize = when {
                            sourceText.length <= 60 -> 16.sp
                            sourceText.length <= 120 -> 14.5.sp
                            else -> 13.5.sp
                        }
                        Text(
                            text = sourceText,
                            fontSize = sourceFontSize,
                            lineHeight = (sourceFontSize.value * 1.35f).sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            maxLines = 8,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Intermediate English Translation
            if (intermediateEnglish.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = DarkSubCardBg.copy(alpha = 0.6f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "English Translation:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryAccent.copy(alpha = 0.9f)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val englishFontSize = when {
                            intermediateEnglish.length <= 60 -> 14.sp
                            intermediateEnglish.length <= 120 -> 13.sp
                            else -> 12.sp
                        }
                        Text(
                            text = intermediateEnglish,
                            fontSize = englishFontSize,
                            lineHeight = (englishFontSize.value * 1.35f).sp,
                            fontFamily = OutfitFontFamily,
                            color = Color(0xFFCBD5E1),
                            maxLines = 6,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Target Translated Sentence
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = DarkSubCardBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryAccent.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = onPlayTarget,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(PrimaryAccent.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "استماع للترجمة",
                                tint = PrimaryAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(
                            onClick = { onCopy(targetText) },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(DarkCardBg)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "نسخ الترجمة",
                                tint = Color.LightGray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 10.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = if (direction == Direction.GERMAN_TO_ARABIC) "الترجمة العربية المعتمدة:" else "الترجمة الألمانية المعتمدة:",
                            fontSize = 12.sp,
                            fontFamily = CairoFontFamily,
                            color = PrimaryAccent,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val targetFontSize = when {
                            targetText.length <= 60 -> 18.sp
                            targetText.length <= 120 -> 16.sp
                            else -> 14.5.sp
                        }
                        Text(
                            text = targetText,
                            fontSize = targetFontSize,
                            lineHeight = (targetFontSize.value * 1.4f).sp,
                            fontFamily = if (targetText.any { it in '\u0600'..'\u06FF' }) CairoFontFamily else OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryAccent,
                            textAlign = TextAlign.End,
                            maxLines = 8,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

/**
 * Verb Conjugation Grid (PRÄSENS & PERFEKT)
 */
@Composable
fun ModernVerbConjugationGrid(verb: GermanVerbEntity) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = DarkSubCardBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "تصريف الفعل (Konjugation - Präsens)",
                    fontSize = 13.sp,
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryAccent
                )
                Text(
                    text = "المساعد: ${verb.auxiliary} | التام: ${verb.partizipZwei ?: "-"}",
                    fontSize = 11.sp,
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Medium,
                    color = SecondaryAccent
                )
            }

            HorizontalDivider(color = DarkBorder)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ConjugationPill(pronoun = "ich", form = verb.presentIch ?: "-", modifier = Modifier.weight(1f))
                ConjugationPill(pronoun = "du", form = verb.presentDu ?: "-", modifier = Modifier.weight(1f))
                ConjugationPill(pronoun = "er/sie/es", form = verb.presentErSieEs ?: "-", modifier = Modifier.weight(1f))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ConjugationPill(pronoun = "wir", form = verb.presentWir ?: verb.infinitive, modifier = Modifier.weight(1f))
                ConjugationPill(pronoun = "ihr", form = verb.presentIhr ?: "-", modifier = Modifier.weight(1f))
                ConjugationPill(pronoun = "sie/Sie", form = verb.presentSie ?: verb.infinitive, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun ModernVerbConjugationGrid(verbResult: VerbConjugationResult) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = DarkSubCardBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "تصريف الفعل (Konjugation - Präsens)",
                    fontSize = 13.sp,
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryAccent
                )
                Text(
                    text = "المساعد: ${verbResult.auxiliary ?: "haben"} | التام: ${verbResult.partizipZwei ?: "-"}",
                    fontSize = 11.sp,
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Medium,
                    color = SecondaryAccent
                )
            }

            HorizontalDivider(color = DarkBorder)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ConjugationPill(pronoun = "ich", form = verbResult.prasensIch ?: "-", modifier = Modifier.weight(1f))
                ConjugationPill(pronoun = "du", form = verbResult.prasensDu ?: "-", modifier = Modifier.weight(1f))
                ConjugationPill(pronoun = "er/sie/es", form = verbResult.prasensErSieEs ?: "-", modifier = Modifier.weight(1f))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ConjugationPill(pronoun = "wir", form = verbResult.prasensWir ?: verbResult.infinitive, modifier = Modifier.weight(1f))
                ConjugationPill(pronoun = "ihr", form = verbResult.prasensIhr ?: "-", modifier = Modifier.weight(1f))
                ConjugationPill(pronoun = "sie/Sie", form = verbResult.prasensSie ?: verbResult.infinitive, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun ConjugationPill(
    pronoun: String,
    form: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = DarkCardBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = pronoun,
                fontSize = 10.5.sp,
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                color = SecondaryAccent
            )
            Text(
                text = form,
                fontSize = 12.sp,
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

/**
 * Noun Declension Grid (Kasus)
 */
@Composable
fun ModernNounDeclensionGrid(noun: GermanNounEntity) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = DarkSubCardBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "جدول حالات الإعراب (Kasus Deklination):",
                fontSize = 13.sp,
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.Bold,
                color = PrimaryAccent
            )
            HorizontalDivider(color = DarkBorder)

            NounKasusRow("Nominativ (مرفوع)", noun.nominativSingular ?: noun.lemma, noun.nominativPlural ?: "-", PrimaryAccent)
            NounKasusRow("Akkusativ (منصوب)", noun.akkusativSingular ?: "-", noun.akkusativPlural ?: "-", SecondaryAccent)
            NounKasusRow("Dativ (مجرور)", noun.dativSingular ?: "-", noun.dativPlural ?: "-", AmberAccent)
            NounKasusRow("Genitiv (مضاف إليه)", noun.genitivSingular ?: "-", noun.genitivPlural ?: "-", VerbPurple)
        }
    }
}

@Composable
fun NounKasusRow(kasus: String, sg: String, pl: String, accentColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(accentColor)
            )
            Text(
                text = kasus,
                fontSize = 11.5.sp,
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(text = "مفرد: $sg", fontSize = 11.5.sp, fontFamily = OutfitFontFamily, color = Color.White)
            Text(text = "جمع: $pl", fontSize = 11.5.sp, fontFamily = OutfitFontFamily, color = Color(0xFFCBD5E1))
        }
    }
}

/**
 * Example Sentence Box with green vertical accent line
 */
@Composable
fun ExampleSentenceCard(example: ExampleEntity) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = DarkSubCardBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryAccent.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Atmosphere.primaryGlowGradient)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = example.germanSentence,
                    fontSize = 14.5.sp,
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = example.arabicTranslation,
                    fontSize = 13.sp,
                    fontFamily = CairoFontFamily,
                    color = PrimaryAccent
                )
            }
        }
    }
}

@Composable
fun SuggestionCardItem(
    word: WordEntity,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = DarkCardBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!word.article.isNullOrBlank()) {
                    val articleBrush = when (word.article.lowercase()) {
                        "der" -> Atmosphere.derCardGradient
                        "die" -> Atmosphere.dieCardGradient
                        "das" -> Atmosphere.dasCardGradient
                        else -> Atmosphere.sapphireGlowGradient
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.Transparent,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(articleBrush)
                    ) {
                        Text(
                            text = word.article,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = word.germanWord,
                    color = Color.White,
                    fontFamily = OutfitFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = word.arabicTranslation,
                color = PrimaryAccent,
                fontFamily = CairoFontFamily,
                fontSize = 14.sp
            )
        }
    }
}

/**
 * 5. Floating Dark Bottom Navigation Bar
 */
@Composable
fun ModernBottomNavBar(
    selectedTab: String,
    lang: AppLanguage = AppLanguage.ARABIC,
    onTabSelected: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = NavBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x18FFFFFF))
    ) {
        NavigationBar(
            containerColor = NavBg,
            contentColor = Color.White,
            tonalElevation = 0.dp,
            modifier = Modifier.height(68.dp)
        ) {
            val tabs = listOf(
                Triple("dictionary", AppStrings.tabDictionary(lang), Icons.Default.MenuBook),
                Triple("history", AppStrings.tabHistory(lang), Icons.Default.History),
                Triple("cheatsheet", AppStrings.tabCheatSheet(lang), Icons.Default.AutoStories),
                Triple("settings", AppStrings.tabSettings(lang), Icons.Default.Settings)
            )

            tabs.forEach { (tabId, label, icon) ->
                val isSelected = selectedTab == tabId
                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onTabSelected(tabId) },
                    icon = {
                        Box(
                            modifier = Modifier
                                .size(width = 48.dp, height = 28.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) PrimaryAccent.copy(alpha = 0.2f) else Color.Transparent),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                modifier = Modifier.size(20.dp),
                                tint = if (isSelected) PrimaryAccent else TextMuted
                            )
                        }
                    },
                    label = {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontFamily = if (lang == AppLanguage.ARABIC) CairoFontFamily else OutfitFontFamily,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else TextMuted
                        )
                    },
                    alwaysShowLabel = true,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryAccent,
                        selectedTextColor = Color.White,
                        indicatorColor = Color.Transparent,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    )
                )
            }
        }
    }
}

@Composable
fun MissingModelDownloadBanner(
    hasQuery: Boolean,
    query: String,
    settingsState: com.example.feature.settings.SettingsUiState,
    onDownloadAll: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    if (settingsState.areAllModelsDownloaded) return

    val lang = settingsState.appLanguage
    val isDownloading = settingsState.downloadAllState is com.example.feature.settings.ModelDownloadState.Downloading

    val borderStroke = if (hasQuery) {
        androidx.compose.foundation.BorderStroke(1.5.dp, AmberAccent)
    } else {
        androidx.compose.foundation.BorderStroke(1.dp, Atmosphere.glassBorder)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardBg),
        border = borderStroke,
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = (if (hasQuery) AmberAccent else PrimaryAccent).copy(alpha = 0.15f),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = null,
                                tint = if (hasQuery) AmberAccent else PrimaryAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = if (hasQuery) {
                                AppStrings.missingModelAlertTitle(lang)
                            } else {
                                if (lang == AppLanguage.ARABIC) "تفعيل الترجمة الفورية دون اتصال" else "Enable Offline Instant Translation"
                            },
                            fontSize = 14.sp,
                            fontFamily = if (lang == AppLanguage.ARABIC) CairoFontFamily else OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = if (hasQuery) AmberAccent else Color.White
                        )
                        Text(
                            text = if (lang == AppLanguage.ARABIC) {
                                "${settingsState.downloadedModelsCount} من 3 حزم لغات متوفرة"
                            } else {
                                "${settingsState.downloadedModelsCount} of 3 language packages ready"
                            },
                            fontSize = 11.sp,
                            fontFamily = if (lang == AppLanguage.ARABIC) CairoFontFamily else OutfitFontFamily,
                            color = TextMuted
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = (if (hasQuery) AmberAccent else PrimaryAccent).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (hasQuery) {
                            AppStrings.requiredDownloadBadge(lang)
                        } else {
                            if (lang == AppLanguage.ARABIC) "أوفلاين" else "Offline"
                        },
                        fontFamily = if (lang == AppLanguage.ARABIC) CairoFontFamily else OutfitFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (hasQuery) AmberAccent else PrimaryAccent,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Text(
                text = if (hasQuery) {
                    AppStrings.missingModelAlertSubtitle(lang)
                } else {
                    if (lang == AppLanguage.ARABIC) {
                        "حمّل موديلات الترجمة الآن (الألمانية، العربية، الإنجليزية) لتتمكن من ترجمة أي كلمة ونطقها بدون إنترنت وبسرعة فائقة."
                    } else {
                        "Download translation models now (German, Arabic, English) to translate and pronounce any word offline instantly."
                    }
                },
                fontSize = 12.5.sp,
                fontFamily = if (lang == AppLanguage.ARABIC) CairoFontFamily else OutfitFontFamily,
                lineHeight = 18.sp,
                color = if (hasQuery) Color.White else Color(0xFFCBD5E1)
            )

            if (isDownloading) {
                val msg = (settingsState.downloadAllState as com.example.feature.settings.ModelDownloadState.Downloading).progressMessage
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = PrimaryAccent, strokeWidth = 2.dp)
                    Text(msg, fontSize = 12.sp, fontFamily = if (lang == AppLanguage.ARABIC) CairoFontFamily else OutfitFontFamily, color = PrimaryAccent)
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onDownloadAll,
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (hasQuery) AmberAccent else PrimaryAccent,
                            contentColor = DarkBg
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = DarkBg,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            AppStrings.downloadModelsNowBtn(lang),
                            fontSize = 12.5.sp,
                            fontFamily = if (lang == AppLanguage.ARABIC) CairoFontFamily else OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = DarkBg
                        )
                    }

                    androidx.compose.material3.OutlinedButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.height(42.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFCBD5E1)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                    ) {
                        Text(
                            AppStrings.tabSettings(lang),
                            fontSize = 12.sp,
                            fontFamily = if (lang == AppLanguage.ARABIC) CairoFontFamily else OutfitFontFamily
                        )
                    }
                }
            }
        }
    }
}

