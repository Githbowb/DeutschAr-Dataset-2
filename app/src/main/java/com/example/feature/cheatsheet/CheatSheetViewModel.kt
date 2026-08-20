package com.example.feature.cheatsheet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CheatSheetViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        CheatSheetUiState(
            selectedCategory = "ALL",
            rules = GermanGrammarData.rules,
            expandedRuleId = GermanGrammarData.rules.firstOrNull()?.id
        )
    )
    val uiState: StateFlow<CheatSheetUiState> = _uiState.asStateFlow()

    fun selectCategory(categoryId: String) {
        _uiState.update { current ->
            val filteredRules = if (categoryId == "ALL") {
                GermanGrammarData.rules
            } else {
                GermanGrammarData.rules.filter { it.category == categoryId }
            }
            current.copy(
                selectedCategory = categoryId,
                rules = filteredRules,
                expandedRuleId = filteredRules.firstOrNull()?.id
            )
        }
    }

    fun toggleRuleExpansion(ruleId: String) {
        _uiState.update { current ->
            current.copy(
                expandedRuleId = if (current.expandedRuleId == ruleId) null else ruleId
            )
        }
    }

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CheatSheetViewModel() as T
        }
    }
}
