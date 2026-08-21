package com.example.feature.cheatsheet

data class GrammarRule(
    val id: String,
    val titleAr: String,
    val titleDe: String,
    val summaryAr: String,
    val category: String, // "GENDER", "CASES", "WORD_ORDER"
    val explanations: List<GrammarExplanation>,
    val tables: List<GrammarTable> = emptyList(),
    val examples: List<GrammarExample> = emptyList(),
    val importantTips: List<String> = emptyList()
)

data class GrammarExplanation(
    val heading: String,
    val content: String
)

data class GrammarTable(
    val title: String,
    val headers: List<String>,
    val rows: List<List<String>>
)

data class GrammarExample(
    val german: String,
    val arabic: String,
    val explanation: String = ""
)

data class CheatSheetCategory(
    val id: String,
    val titleAr: String,
    val titleDe: String,
    val icon: String
)

data class CheatSheetUiState(
    val selectedCategory: String = "ALL", // "ALL", "GENDER", "CASES", "WORD_ORDER"
    val rules: List<GrammarRule> = emptyList(),
    val expandedRuleId: String? = null
)
