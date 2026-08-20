package com.example.core.translation

import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.languageid.LanguageIdentifier
import kotlinx.coroutines.tasks.await

object LanguageDetector {

    private var languageIdentifier: LanguageIdentifier? = null

    private fun getLanguageIdentifier(): LanguageIdentifier? {
        if (languageIdentifier == null) {
            try {
                languageIdentifier = LanguageIdentification.getClient()
            } catch (_: Throwable) {
                languageIdentifier = null
            }
        }
        return languageIdentifier
    }

    private val arabicRegex = Regex("[\u0600-\u06FF\u0750-\u077F\u08A0-\u08FF\uFB50-\uFDFF\uFE70-\uFEFF]")
    private val germanCharsRegex = Regex("[äöüßÄÖÜ]")

    private val commonGermanWords = setOf(
        "der", "die", "das", "ein", "eine", "einer", "einem", "einen", "eines",
        "ich", "du", "er", "sie", "es", "wir", "ihr", "und", "oder", "aber",
        "nicht", "ist", "sind", "war", "haben", "hat", "hatte", "sein", "werden",
        "wird", "wurde", "mit", "von", "zu", "in", "auf", "für", "an", "nach",
        "bei", "aus", "über", "unter", "vor", "zwischen", "wie", "was", "wo",
        "wer", "warum", "wann", "welche", "welcher", "welches", "will", "wollen",
        "kann", "können", "muss", "müssen", "soll", "sollen", "darf", "dürfen",
        "möchte", "möchten", "gut", "sehr", "viel", "hier", "dort", "jetzt", "immer"
    )

    private val commonEnglishWords = setOf(
        "the", "a", "an", "is", "are", "was", "were", "be", "been", "being",
        "have", "has", "had", "do", "does", "did", "will", "would", "shall",
        "should", "can", "could", "may", "might", "must", "and", "but", "or",
        "not", "to", "of", "in", "for", "on", "with", "at", "by", "from",
        "up", "about", "into", "over", "after", "i", "you", "he", "she",
        "it", "we", "they", "what", "which", "who", "when", "where", "why",
        "how", "all", "any", "both", "each", "few", "more", "most", "other",
        "some", "such", "no", "nor", "too", "very", "want", "like", "need",
        "look", "see", "come", "go", "make", "take", "know", "get", "give"
    )

    /**
     * Identifies the language code ("de", "en", "ar") for a given text.
     * Uses a multi-stage approach combining script detection, ML Kit Language ID, patterns, and lexicons.
     */
    suspend fun detectLanguage(text: String): String {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return "de"

        // 1. Definite script matches
        if (arabicRegex.containsMatchIn(trimmed)) {
            return "ar"
        }
        if (germanCharsRegex.containsMatchIn(trimmed)) {
            return "de"
        }

        // 2. Tokenize and check against high-frequency words
        val words = trimmed.lowercase().split(Regex("\\s+")).filter { it.isNotBlank() }
        val germanMatchCount = words.count { it in commonGermanWords }
        val englishMatchCount = words.count { it in commonEnglishWords }

        if (germanMatchCount > englishMatchCount && germanMatchCount > 0) {
            return "de"
        }
        if (englishMatchCount > germanMatchCount && englishMatchCount > 0) {
            return "en"
        }

        // 3. For short queries or single/two words, rely on instant deterministic linguistic patterns
        // (ML Kit Language Identification requires full paragraphs/sentences and triggers native mlock allocations)
        val lowerText = trimmed.lowercase()

        // Distinct German letter combinations: "sch", "tsch", "tz", "pf"
        val hasGermanDigraphs = lowerText.contains("sch") ||
                lowerText.contains("tsch") ||
                lowerText.contains("tz") ||
                lowerText.contains("pf")

        // Characteristic German prefixes (e.g. verstehen, bekommen, gefallen, entgehen)
        val hasGermanPrefix = lowerText.startsWith("ver") ||
                lowerText.startsWith("zer") ||
                lowerText.startsWith("ge") ||
                lowerText.startsWith("be") ||
                lowerText.startsWith("ent") ||
                lowerText.startsWith("emp") ||
                lowerText.startsWith("miss")

        // Characteristic German noun/adjective suffixes
        val hasGermanSuffix = lowerText.endsWith("ung") ||
                lowerText.endsWith("keit") ||
                lowerText.endsWith("heit") ||
                lowerText.endsWith("schaft") ||
                lowerText.endsWith("chen") ||
                lowerText.endsWith("lein") ||
                lowerText.endsWith("lich") ||
                lowerText.endsWith("isch") ||
                lowerText.endsWith("haft") ||
                lowerText.endsWith("bar")

        // German verb infinitive endings (-en, -eln, -ern for words longer than 3 letters)
        val hasGermanVerbEnding = lowerText.length > 3 && (
                lowerText.endsWith("eln") ||
                lowerText.endsWith("ern") ||
                (lowerText.endsWith("en") && !lowerText.endsWith("tion") && !lowerText.endsWith("sion") && !lowerText.endsWith("even") && !lowerText.endsWith("seven") && !lowerText.endsWith("open"))
        )

        if (hasGermanDigraphs || hasGermanSuffix || (hasGermanPrefix && hasGermanVerbEnding)) {
            return "de"
        }

        // 4. Characteristic English Suffixes & Patterns
        val hasEnglishSuffix = lowerText.endsWith("ing") ||
                lowerText.endsWith("tion") ||
                lowerText.endsWith("sion") ||
                lowerText.endsWith("ly") ||
                lowerText.endsWith("ed") ||
                lowerText.endsWith("ness") ||
                lowerText.endsWith("ment") ||
                lowerText.endsWith("able") ||
                lowerText.endsWith("ible") ||
                lowerText.endsWith("ful") ||
                lowerText.endsWith("less") ||
                lowerText.endsWith("ous") ||
                lowerText.endsWith("ive")

        val hasEnglishDigraphs = lowerText.contains("th") ||
                lowerText.contains("wh") ||
                lowerText.contains("ea") ||
                lowerText.contains("oo") ||
                lowerText.contains("ight") ||
                lowerText.contains("ough")

        if (hasEnglishSuffix || hasEnglishDigraphs) {
            return "en"
        }

        // 5. ML Kit Language Identification for longer sentences (3+ words or 25+ chars)
        if (words.size >= 3 || trimmed.length >= 25) {
            try {
                val identifier = getLanguageIdentifier()
                if (identifier != null) {
                    val detected = identifier.identifyLanguage(trimmed).await()
                    when (detected.lowercase()) {
                        "de", "ger" -> return "de"
                        "ar", "ara" -> return "ar"
                        "en", "eng" -> return "en"
                    }
                }
            } catch (_: Throwable) {
                // If ML Kit throws or fails mlock, smoothly continue to fallback
            }
        }

        // 6. Ambiguous Latin fallback
        // When text consists of plain Latin characters without special German characters or patterns,
        // bias toward "en" as a safe fallback for Arab learners typing English search queries.
        val isPlainLatin = trimmed.all { it in 'a'..'z' || it in 'A'..'Z' || it.isWhitespace() || it in "-'" }
        if (isPlainLatin) {
            return "en"
        }

        return "de"
    }
}
