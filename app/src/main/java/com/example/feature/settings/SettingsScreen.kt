package com.example.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.AppLanguage
import com.example.ui.theme.AppStrings
import com.example.ui.theme.Atmosphere
import com.example.ui.theme.ambientAtmosphere
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkCardBg
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSubCardBg
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.PrimaryAccent
import com.example.ui.theme.SecondaryAccent
import com.example.ui.theme.TextMain
import com.example.ui.theme.TextMuted

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onSetAppLanguage: (AppLanguage) -> Unit,
    onDownloadAll: () -> Unit,
    onDownloadSingle: (String) -> Unit,
    onDeleteSingle: (String) -> Unit,
    onToggleRequireWifi: (Boolean) -> Unit,
    onRefreshStatuses: () -> Unit
) {
    val scrollState = rememberScrollState()
    val lang = uiState.appLanguage

    Column(
        modifier = Modifier
            .fillMaxSize()
            .ambientAtmosphere()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = AppStrings.settingsTitle(lang),
                    fontFamily = if (lang == AppLanguage.ARABIC) CairoFontFamily else OutfitFontFamily,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMain
                )
                Text(
                    text = AppStrings.settingsSubtitle(lang),
                    fontFamily = if (lang == AppLanguage.ARABIC) CairoFontFamily else OutfitFontFamily,
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }

            IconButton(
                onClick = onRefreshStatuses,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(DarkSubCardBg)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = AppStrings.refreshStatus(lang),
                    tint = PrimaryAccent,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // 2. App Language Switcher Card (تبديل لغة واجهة التطبيق)
        AppLanguageSelectionCard(
            currentLanguage = uiState.appLanguage,
            onSelectLanguage = onSetAppLanguage
        )

        // 3. Main Master Download Card
        MasterDownloadCard(
            uiState = uiState,
            lang = lang,
            onDownloadAll = onDownloadAll
        )

        // 4. Individual Models List
        Text(
            text = AppStrings.availableLanguagePackages(lang),
            fontFamily = if (lang == AppLanguage.ARABIC) CairoFontFamily else OutfitFontFamily,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = TextMain,
            modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
        )

        LanguageModelCard(
            model = uiState.germanModel,
            lang = lang,
            onDownload = { onDownloadSingle("de") },
            onDelete = { onDeleteSingle("de") }
        )

        LanguageModelCard(
            model = uiState.arabicModel,
            lang = lang,
            onDownload = { onDownloadSingle("ar") },
            onDelete = { onDeleteSingle("ar") }
        )

        LanguageModelCard(
            model = uiState.englishModel,
            lang = lang,
            onDownload = { onDownloadSingle("en") },
            onDelete = { onDeleteSingle("en") }
        )

        // 5. Download Preferences Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCardBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = DarkSubCardBg,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Wifi,
                                contentDescription = null,
                                tint = PrimaryAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = AppStrings.wifiOnlyTitle(lang),
                            fontFamily = if (lang == AppLanguage.ARABIC) CairoFontFamily else OutfitFontFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMain
                        )
                        Text(
                            text = AppStrings.wifiOnlyDesc(lang),
                            fontFamily = if (lang == AppLanguage.ARABIC) CairoFontFamily else OutfitFontFamily,
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                }

                Switch(
                    checked = uiState.requireWifiOnly,
                    onCheckedChange = onToggleRequireWifi,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = DarkBg,
                        checkedTrackColor = PrimaryAccent,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = DarkSubCardBg
                    )
                )
            }
        }

        // 6. Info Footer Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSubCardBg.copy(alpha = 0.6f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = SecondaryAccent,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = AppStrings.aboutAppDesc(lang),
                    fontFamily = if (lang == AppLanguage.ARABIC) CairoFontFamily else OutfitFontFamily,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun AppLanguageSelectionCard(
    currentLanguage: AppLanguage,
    onSelectLanguage: (AppLanguage) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("app_language_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, Atmosphere.glassBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = PrimaryAccent.copy(alpha = 0.15f),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = PrimaryAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = AppStrings.appLanguageSectionTitle(currentLanguage),
                        fontFamily = if (currentLanguage == AppLanguage.ARABIC) CairoFontFamily else OutfitFontFamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMain
                    )
                    Text(
                        text = AppStrings.appLanguageSectionDesc(currentLanguage),
                        fontFamily = if (currentLanguage == AppLanguage.ARABIC) CairoFontFamily else OutfitFontFamily,
                        fontSize = 11.5.sp,
                        color = TextMuted
                    )
                }
            }

            HorizontalDivider(color = DarkCardBorder, thickness = 0.8.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Arabic Option
                val isArabic = currentLanguage == AppLanguage.ARABIC
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onSelectLanguage(AppLanguage.ARABIC) }
                        .testTag("lang_select_arabic"),
                    shape = RoundedCornerShape(14.dp),
                    color = if (isArabic) PrimaryAccent.copy(alpha = 0.15f) else DarkSubCardBg,
                    border = androidx.compose.foundation.BorderStroke(
                        if (isArabic) 1.5.dp else 1.dp,
                        if (isArabic) PrimaryAccent else DarkCardBorder
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("🇸🇦", fontSize = 18.sp)
                            Column {
                                Text(
                                    text = "العربية",
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (isArabic) PrimaryAccent else TextMain
                                )
                                Text(
                                    text = "Arabic",
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        if (isArabic) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = PrimaryAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // English Option
                val isEnglish = currentLanguage == AppLanguage.ENGLISH
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onSelectLanguage(AppLanguage.ENGLISH) }
                        .testTag("lang_select_english"),
                    shape = RoundedCornerShape(14.dp),
                    color = if (isEnglish) PrimaryAccent.copy(alpha = 0.15f) else DarkSubCardBg,
                    border = androidx.compose.foundation.BorderStroke(
                        if (isEnglish) 1.5.dp else 1.dp,
                        if (isEnglish) PrimaryAccent else DarkCardBorder
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("🇬🇧", fontSize = 18.sp)
                            Column {
                                Text(
                                    text = "English",
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (isEnglish) PrimaryAccent else TextMain
                                )
                                Text(
                                    text = "الإنجليزية",
                                    fontFamily = CairoFontFamily,
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        if (isEnglish) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = PrimaryAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MasterDownloadCard(
    uiState: SettingsUiState,
    lang: AppLanguage,
    onDownloadAll: () -> Unit
) {
    val isDownloading = uiState.downloadAllState is ModelDownloadState.Downloading
    val allDone = uiState.areAllModelsDownloaded

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (allDone) DarkSubCardBg else DarkCardBg
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (allDone) PrimaryAccent.copy(alpha = 0.4f) else DarkCardBorder
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (allDone) PrimaryAccent.copy(alpha = 0.15f) else SecondaryAccent.copy(alpha = 0.15f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (allDone) Icons.Default.CheckCircle else Icons.Default.CloudDownload,
                                contentDescription = null,
                                tint = if (allDone) PrimaryAccent else SecondaryAccent,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = if (allDone) {
                                if (lang == AppLanguage.ARABIC) "موديلات الترجمة جاهزة بالكامل" else "Translation Models Ready"
                            } else {
                                if (lang == AppLanguage.ARABIC) "تحميل موديلات الترجمة أوفلاين" else "Offline Translation Models"
                            },
                            fontFamily = if (lang == AppLanguage.ARABIC) CairoFontFamily else OutfitFontFamily,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMain
                        )
                        Text(
                            text = if (allDone) {
                                if (lang == AppLanguage.ARABIC) "3 من 3 حزم محملة (جاهز للعمل أوفلاين)" else "3 of 3 models downloaded (Ready offline)"
                            } else {
                                if (lang == AppLanguage.ARABIC) "${uiState.downloadedModelsCount} من 3 حزم محملة" else "${uiState.downloadedModelsCount} of 3 models downloaded"
                            },
                            fontFamily = if (lang == AppLanguage.ARABIC) CairoFontFamily else OutfitFontFamily,
                            fontSize = 12.sp,
                            color = if (allDone) PrimaryAccent else TextMuted
                        )
                    }
                }
            }

            // Progress or Status Message
            when (val state = uiState.downloadAllState) {
                is ModelDownloadState.Downloading -> {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = PrimaryAccent,
                            trackColor = DarkSubCardBg
                        )
                        Text(
                            text = state.progressMessage,
                            fontFamily = if (lang == AppLanguage.ARABIC) CairoFontFamily else OutfitFontFamily,
                            fontSize = 12.sp,
                            color = PrimaryAccent
                        )
                    }
                }

                is ModelDownloadState.Error -> {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = DangerRed.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = DangerRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = state.error,
                                fontFamily = if (lang == AppLanguage.ARABIC) CairoFontFamily else OutfitFontFamily,
                                fontSize = 12.sp,
                                color = DangerRed
                            )
                        }
                    }
                }

                is ModelDownloadState.Success -> {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = PrimaryAccent.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryAccent.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = PrimaryAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = state.message,
                                fontFamily = if (lang == AppLanguage.ARABIC) CairoFontFamily else OutfitFontFamily,
                                fontSize = 12.sp,
                                color = PrimaryAccent
                            )
                        }
                    }
                }

                is ModelDownloadState.Idle -> {
                    if (!allDone) {
                        Text(
                            text = if (lang == AppLanguage.ARABIC) {
                                "تحميل حزم اللغات (الألمانية، العربية، الإنجليزية) يتيح الترجمة الفورية في أي وقت وبدون الحاجة لشبكة إنترنت."
                            } else {
                                "Downloading language models (German, Arabic, English) enables instant translation anytime without internet connection."
                            },
                            fontFamily = if (lang == AppLanguage.ARABIC) CairoFontFamily else OutfitFontFamily,
                            fontSize = 12.sp,
                            color = TextMuted,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Action Button
            if (!allDone || isDownloading) {
                Button(
                    onClick = onDownloadAll,
                    enabled = !isDownloading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("download_all_models_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryAccent,
                        contentColor = DarkBg,
                        disabledContainerColor = DarkSubCardBg,
                        disabledContentColor = TextMuted
                    )
                ) {
                    if (isDownloading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = DarkBg,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            AppStrings.modelDownloadingStatus(lang),
                            fontFamily = if (lang == AppLanguage.ARABIC) CairoFontFamily else OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = DarkBg,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (lang == AppLanguage.ARABIC) "تحميل جميع الموديلات الآن" else "Download All Models Now",
                            fontFamily = if (lang == AppLanguage.ARABIC) CairoFontFamily else OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = DarkBg
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LanguageModelCard(
    model: LanguageModelStatus,
    lang: AppLanguage,
    onDownload: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardBg),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (model.isDownloaded) PrimaryAccent.copy(alpha = 0.3f) else DarkCardBorder
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = model.flag,
                    fontSize = 24.sp
                )

                Column {
                    val displayName = when (model.code) {
                        "de" -> if (lang == AppLanguage.ARABIC) "الألمانية (Deutsch)" else "German (Deutsch)"
                        "ar" -> if (lang == AppLanguage.ARABIC) "العربية (العربية)" else "Arabic (العربية)"
                        "en" -> if (lang == AppLanguage.ARABIC) "الإنجليزية (English)" else "English (الإنجليزية)"
                        else -> "${model.nameAr} (${model.nameNative})"
                    }

                    Text(
                        text = displayName,
                        fontFamily = if (lang == AppLanguage.ARABIC) CairoFontFamily else OutfitFontFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMain
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (model.isDownloaded) PrimaryAccent.copy(alpha = 0.15f) else DarkSubCardBg
                        ) {
                            Text(
                                text = if (model.isDownloaded) AppStrings.modelDownloadedStatus(lang) else AppStrings.modelNotDownloadedStatus(lang),
                                fontFamily = if (lang == AppLanguage.ARABIC) CairoFontFamily else OutfitFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (model.isDownloaded) PrimaryAccent else TextMuted,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Text(
                            text = model.approxSize,
                            fontFamily = OutfitFontFamily,
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
            }

            // Action Button (Download / Delete / Spinner)
            if (model.isDownloading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = PrimaryAccent,
                    strokeWidth = 2.dp
                )
            } else if (model.isDownloaded) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(DarkSubCardBg)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = AppStrings.deleteBtn(lang),
                        tint = DangerRed.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                OutlinedButton(
                    onClick = onDownload,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryAccent),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryAccent.copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        AppStrings.downloadBtn(lang),
                        fontFamily = if (lang == AppLanguage.ARABIC) CairoFontFamily else OutfitFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
