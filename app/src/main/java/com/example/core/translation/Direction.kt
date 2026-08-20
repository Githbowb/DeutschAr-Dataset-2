package com.example.core.translation

enum class Direction(val sourceLanguage: String, val targetLanguage: String) {
    GERMAN_TO_ARABIC("de", "ar"),
    ARABIC_TO_GERMAN("ar", "de");

    val displayName: String
        get() = when (this) {
            GERMAN_TO_ARABIC -> "الألمانية -> العربية (DE -> AR)"
            ARABIC_TO_GERMAN -> "العربية -> الألمانية (AR -> DE)"
        }

    fun swapped(): Direction = when (this) {
        GERMAN_TO_ARABIC -> ARABIC_TO_GERMAN
        ARABIC_TO_GERMAN -> GERMAN_TO_ARABIC
    }

    companion object {
        fun fromLanguages(source: String, target: String): Direction {
            return if (source.equals("ar", ignoreCase = true)) {
                ARABIC_TO_GERMAN
            } else {
                GERMAN_TO_ARABIC
            }
        }
    }
}
