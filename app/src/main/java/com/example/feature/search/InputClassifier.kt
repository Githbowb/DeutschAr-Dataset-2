package com.example.feature.search

enum class InputType {
    GERMAN_WORD,
    ARABIC_WORD,
    SENTENCE_OR_QUESTION
}

object InputClassifier {

    private val ARABIC_REGEX = Regex("[\\u0600-\\u06FF]")

    fun classify(query: String): InputType {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return InputType.GERMAN_WORD

        val words = trimmed.split(Regex("\\s+"))
        val hasMultipleWords = words.size > 1
        val hasQuestionMark = trimmed.contains("?") || trimmed.contains("؟")

        if (hasMultipleWords || hasQuestionMark) {
            return InputType.SENTENCE_OR_QUESTION
        }

        return if (ARABIC_REGEX.containsMatchIn(trimmed)) {
            InputType.ARABIC_WORD
        } else {
            InputType.GERMAN_WORD
        }
    }
}
