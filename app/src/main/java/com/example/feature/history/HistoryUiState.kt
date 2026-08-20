package com.example.feature.history

import com.example.core.database.HistoryItemCombined
import com.example.core.database.HistoryResultType

data class HistoryUiState(
    val items: List<HistoryItemCombined> = emptyList(),
    val searchQuery: String = "",
    val filterType: HistoryResultType? = null,
    val filterSavedOnly: Boolean = false,
    val isLoading: Boolean = false
)
