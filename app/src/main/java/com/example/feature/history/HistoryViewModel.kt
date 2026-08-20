package com.example.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.database.HistoryItemCombined
import com.example.core.database.HistoryResultType
import com.example.core.database.WordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val wordRepository: WordRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _filterType = MutableStateFlow<HistoryResultType?>(null)
    private val _filterSavedOnly = MutableStateFlow(false)

    val uiState: StateFlow<HistoryUiState> = combine(
        wordRepository.getSearchHistoryCombined(100),
        _searchQuery,
        _filterType,
        _filterSavedOnly
    ) { historyList, query, filterType, savedOnly ->
        val filtered = historyList.filter { item ->
            val matchesQuery = if (query.isBlank()) {
                true
            } else {
                item.history.query.contains(query, ignoreCase = true) ||
                        item.history.resultText.contains(query, ignoreCase = true) ||
                        (item.word?.germanWord?.contains(query, ignoreCase = true) == true) ||
                        (item.word?.arabicTranslation?.contains(query, ignoreCase = true) == true)
            }

            val matchesType = when (filterType) {
                null -> true
                HistoryResultType.DICTIONARY -> item.history.resultType.equals("dictionary", ignoreCase = true)
                HistoryResultType.OFFLINE_TRANSLATION -> !item.history.resultType.equals("dictionary", ignoreCase = true)
            }

            val matchesSaved = if (savedOnly) item.history.isSaved else true

            matchesQuery && matchesType && matchesSaved
        }

        HistoryUiState(
            items = filtered,
            searchQuery = query,
            filterType = filterType,
            filterSavedOnly = savedOnly,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryUiState(isLoading = true)
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.update { query }
    }

    fun setFilterType(type: HistoryResultType?) {
        _filterType.update { type }
    }

    fun toggleSavedOnlyFilter() {
        _filterSavedOnly.update { !it }
    }

    fun toggleSaved(item: HistoryItemCombined) {
        viewModelScope.launch {
            wordRepository.toggleHistorySaved(item.history.id)
        }
    }

    fun deleteHistoryItem(item: HistoryItemCombined) {
        viewModelScope.launch {
            wordRepository.deleteHistoryItem(item.history.id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            wordRepository.clearHistory()
        }
    }

    class Factory(
        private val wordRepository: WordRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HistoryViewModel(wordRepository) as T
        }
    }
}
