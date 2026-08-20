package com.example.feature.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.database.HistoryItemCombined
import com.example.core.database.HistoryResultType
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.AppLanguage
import com.example.ui.theme.AppStrings
import com.example.ui.theme.Atmosphere
import com.example.ui.theme.ambientAtmosphere
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkCardBg
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSubCardBg
import com.example.ui.theme.DangerRed
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.PrimaryAccent
import com.example.ui.theme.SecondaryAccent
import com.example.ui.theme.TextMain
import com.example.ui.theme.TextMuted
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    uiState: HistoryUiState,
    lang: AppLanguage = AppLanguage.ARABIC,
    onSearchQueryChange: (String) -> Unit,
    onSetFilterType: (HistoryResultType?) -> Unit,
    onToggleSavedOnlyFilter: () -> Unit,
    onToggleSaved: (HistoryItemCombined) -> Unit,
    onDeleteItem: (HistoryItemCombined) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showConfirmClearDialog by remember { mutableStateOf(false) }

    if (showConfirmClearDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmClearDialog = false },
            title = {
                Text(
                    text = AppStrings.clearHistoryDialogTitle(lang),
                    fontFamily = if (lang == AppLanguage.ARABIC) CairoFontFamily else OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Text(
                    text = AppStrings.clearHistoryDialogMsg(lang),
                    fontFamily = if (lang == AppLanguage.ARABIC) CairoFontFamily else OutfitFontFamily,
                    color = TextMuted
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAll()
                        showConfirmClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text(
                        AppStrings.confirmDelete(lang),
                        fontFamily = if (lang == AppLanguage.ARABIC) CairoFontFamily else OutfitFontFamily,
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmClearDialog = false }) {
                    Text(
                        AppStrings.cancel(lang),
                        fontFamily = if (lang == AppLanguage.ARABIC) CairoFontFamily else OutfitFontFamily,
                        color = TextMuted
                    )
                }
            },
            containerColor = DarkCardBg,
            shape = RoundedCornerShape(20.dp)
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .ambientAtmosphere()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Search bar
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("history_search_input"),
            placeholder = {
                Text(
                    text = AppStrings.historySearchPlaceholder(lang),
                    fontFamily = if (lang == AppLanguage.ARABIC) CairoFontFamily else OutfitFontFamily,
                    color = TextMuted,
                    fontSize = 14.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (uiState.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
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
                focusedTextColor = TextMain,
                unfocusedTextColor = TextMain
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filter chips (All / Dictionary / ML Kit Fallback / Saved Only)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val chipColors = FilterChipDefaults.filterChipColors(
                containerColor = DarkCardBg,
                labelColor = TextMuted,
                selectedContainerColor = PrimaryAccent.copy(alpha = 0.2f),
                selectedLabelColor = PrimaryAccent
            )
            val chipBorder = FilterChipDefaults.filterChipBorder(
                borderColor = DarkCardBorder,
                selectedBorderColor = PrimaryAccent.copy(alpha = 0.5f),
                enabled = true,
                selected = false
            )

            FilterChip(
                selected = uiState.filterType == null && !uiState.filterSavedOnly,
                onClick = {
                    onSetFilterType(null)
                    if (uiState.filterSavedOnly) onToggleSavedOnlyFilter()
                },
                label = {
                    Text(
                        AppStrings.historyFilterAll(lang),
                        fontFamily = if (lang == AppLanguage.ARABIC) CairoFontFamily else OutfitFontFamily,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                shape = RoundedCornerShape(14.dp),
                colors = chipColors,
                border = chipBorder,
                modifier = Modifier.testTag("history_filter_all")
            )

            FilterChip(
                selected = uiState.filterType == HistoryResultType.DICTIONARY,
                onClick = {
                    if (uiState.filterType == HistoryResultType.DICTIONARY) {
                        onSetFilterType(null)
                    } else {
                        onSetFilterType(HistoryResultType.DICTIONARY)
                    }
                },
                label = {
                    Text(
                        AppStrings.historyFilterDictionary(lang),
                        fontFamily = if (lang == AppLanguage.ARABIC) CairoFontFamily else OutfitFontFamily,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                shape = RoundedCornerShape(14.dp),
                colors = chipColors,
                border = chipBorder,
                modifier = Modifier.testTag("history_filter_dictionary")
            )

            FilterChip(
                selected = uiState.filterType == HistoryResultType.OFFLINE_TRANSLATION,
                onClick = {
                    if (uiState.filterType == HistoryResultType.OFFLINE_TRANSLATION) {
                        onSetFilterType(null)
                    } else {
                        onSetFilterType(HistoryResultType.OFFLINE_TRANSLATION)
                    }
                },
                label = {
                    Text(
                        AppStrings.historyFilterTranslation(lang),
                        fontFamily = if (lang == AppLanguage.ARABIC) CairoFontFamily else OutfitFontFamily,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                shape = RoundedCornerShape(14.dp),
                colors = chipColors,
                border = chipBorder,
                modifier = Modifier.testTag("history_filter_translation")
            )

            FilterChip(
                selected = uiState.filterSavedOnly,
                onClick = onToggleSavedOnlyFilter,
                label = {
                    Text(
                        AppStrings.historyFilterSaved(lang),
                        fontFamily = if (lang == AppLanguage.ARABIC) CairoFontFamily else OutfitFontFamily,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                shape = RoundedCornerShape(14.dp),
                colors = chipColors,
                border = chipBorder,
                leadingIcon = {
                    Icon(
                        imageVector = if (uiState.filterSavedOnly) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = null,
                        tint = if (uiState.filterSavedOnly) PrimaryAccent else TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                },
                modifier = Modifier.testTag("history_filter_saved")
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Header info count & clear
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val countLabel = if (lang == AppLanguage.ARABIC) {
                "${uiState.items.size} عمليات بحث"
            } else {
                "${uiState.items.size} items"
            }
            Text(
                text = countLabel,
                fontFamily = if (lang == AppLanguage.ARABIC) CairoFontFamily else OutfitFontFamily,
                fontSize = 13.sp,
                color = TextMuted
            )

            if (uiState.items.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = DangerRed.copy(alpha = 0.1f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed.copy(alpha = 0.3f)),
                    modifier = Modifier.testTag("clear_all_history_btn")
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(onClick = { showConfirmClearDialog = true })
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = AppStrings.clearHistoryBtn(lang),
                            tint = DangerRed,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = AppStrings.clearHistoryBtn(lang),
                            fontSize = 11.sp,
                            fontFamily = if (lang == AppLanguage.ARABIC) CairoFontFamily else OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = DangerRed
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("empty_history_indicator"),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = TextMuted.copy(alpha = 0.5f)
                    )
                    Text(
                        text = if (uiState.searchQuery.isNotBlank()) {
                            if (lang == AppLanguage.ARABIC) "لا توجد نتائج مطابقة لبحثك." else "No matching results found."
                        } else {
                            AppStrings.historyEmptyTitle(lang)
                        },
                        fontFamily = if (lang == AppLanguage.ARABIC) CairoFontFamily else OutfitFontFamily,
                        fontSize = 14.sp,
                        color = TextMuted
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("history_list"),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(
                    items = uiState.items,
                    key = { it.history.id }
                ) { item ->
                    HistoryCard(
                        item = item,
                        lang = lang,
                        onToggleSaved = { onToggleSaved(item) },
                        onDelete = { onDeleteItem(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryCard(
    item: HistoryItemCombined,
    lang: AppLanguage,
    onToggleSaved: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
    val dateString = dateFormat.format(Date(item.history.searchedAt))
    val isDictionary = item.history.resultType.equals("dictionary", ignoreCase = true)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("history_item_${item.history.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, Atmosphere.glassBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isDictionary) PrimaryAccent.copy(alpha = 0.15f) else SecondaryAccent.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = if (isDictionary) {
                                if (lang == AppLanguage.ARABIC) "قاموس" else "Dictionary"
                            } else {
                                if (lang == AppLanguage.ARABIC) "ترجمة فورية" else "Translation"
                            },
                            fontFamily = if (lang == AppLanguage.ARABIC) CairoFontFamily else OutfitFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDictionary) PrimaryAccent else SecondaryAccent,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Text(
                        text = dateString,
                        fontFamily = OutfitFontFamily,
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onToggleSaved,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("save_history_btn_${item.history.id}")
                    ) {
                        Icon(
                            imageVector = if (item.history.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = if (item.history.isSaved) AppStrings.savedWord(lang) else AppStrings.saveWord(lang),
                            tint = if (item.history.isSaved) PrimaryAccent else TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("delete_history_btn_${item.history.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = AppStrings.deleteBtn(lang),
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.history.query,
                fontSize = 18.sp,
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                color = TextMain
            )

            Spacer(modifier = Modifier.height(4.dp))

            val displayText = when {
                item.word != null -> "${item.word.article ?: ""} ${item.word.germanWord} → ${item.word.arabicTranslation}".trim()
                item.history.resultText.isNotBlank() -> item.history.resultText
                else -> ""
            }

            if (displayText.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = DarkSubCardBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
                ) {
                    Text(
                        text = displayText,
                        fontFamily = CairoFontFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryAccent,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }

            if (item.history.isSaved) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (lang == AppLanguage.ARABIC) "✓ مضاف للمحفوظات والمراجعة" else "✓ Added to saved for review",
                    fontFamily = if (lang == AppLanguage.ARABIC) CairoFontFamily else OutfitFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = PrimaryAccent
                )
            }
        }
    }
}
