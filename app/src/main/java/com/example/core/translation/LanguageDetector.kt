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
        "der", "die", "das", "des", "dem", "den", "ein", "eine", "einer", "einem", "einen", "eines",
        "kein", "keine", "keinen", "keinem", "keiner", "keines",
        "ich", "du", "er", "sie", "es", "wir", "ihr", "mich", "dich", "ihn", "uns", "euch",
        "mir", "dir", "ihm", "ihnen", "mein", "meine", "meinen", "dein", "deine", "sein", "seine",
        "und", "oder", "aber", "denn", "weil", "dass", "wenn", "ob", "obwohl", "als", "damit", "sondern",
        "nicht", "ist", "sind", "war", "waren", "haben", "hat", "hatte", "hatten", "sein", "werden",
        "wird", "wurde", "wurden", "mit", "von", "zu", "in", "auf", "für", "an", "nach",
        "bei", "aus", "über", "unter", "vor", "hinter", "neben", "zwischen", "durch", "ohne", "gegen", "um",
        "seit", "ab", "bis", "während", "trotz", "wegen",
        "wie", "was", "wo", "wer", "warum", "wann", "wohin", "woher", "welche", "welcher", "welches",
        "will", "wollen", "kann", "können", "muss", "müssen", "soll", "sollen", "darf", "dürfen",
        "möchte", "möchten", "gut", "sehr", "viel", "hier", "dort", "jetzt", "immer", "nie", "oft",
        "manchmal", "schon", "noch", "auch", "gern", "gerne", "vielleicht", "zusammen", "wieder", "bald",
        "heute", "morgen", "gestern", "eins", "zwei", "drei", "vier", "fünf", "sechs", "sieben", "acht", "neun", "zehn",
        "hallo", "tschüss", "bitte", "danke", "ja", "nein"
    )

    private val commonEnglishWords = setOf(
        "the", "a", "an", "is", "are", "was", "were", "be", "been", "being",
        "have", "has", "had", "do", "does", "did", "will", "would", "shall",
        "should", "can", "could", "may", "might", "must", "and", "but", "or",
        "not", "to", "of", "in", "for", "on", "with", "at", "by", "from",
        "up", "about", "into", "over", "after", "i", "you", "he", "she",
        "it", "we", "they", "them", "their", "theirs", "my", "mine", "your", "yours",
        "his", "her", "hers", "our", "ours", "this", "that", "these", "those",
        "what", "which", "who", "whom", "whose", "when", "where", "why", "how",
        "all", "any", "both", "each", "few", "more", "most", "other", "some", "such",
        "no", "nor", "too", "very", "want", "like", "need", "look", "see", "come",
        "go", "make", "take", "know", "get", "give", "please", "thanks", "thank", "hello"
    )

    /**
     * High-frequency German core vocabulary (including common nouns, professions, food, places).
     * Prevents false classification of short German words without umlauts (e.g. "kellner").
     */
    private val knownGermanVocabulary = setOf(
        "kellner", "kellnerin", "kellnerinnen", "kellners", "kellnern",
        "arzt", "ärztin", "ärzte", "ärztinnen", "lehrer", "lehrerin", "lehrerinnen", "schüler", "schülerin",
        "student", "studentin", "studenten", "studentinnen", "koch", "köchin", "köche",
        "fahrer", "bäcker", "verkäufer", "verkäuferin", "meister", "arbeiter", "kollege", "kollegin",
        "chef", "chefin", "kunde", "kundin", "gast", "gäste", "ober", "bedienung",
        "tisch", "tische", "stuhl", "stühle", "bett", "betten", "schrank", "schränke", "sofa", "sofas",
        "tür", "türen", "fenster", "wand", "wände", "boden", "böden", "decke", "decken",
        "zimmer", "küche", "küchen", "bad", "bäder", "balkon", "balkone", "garten", "gärten",
        "haus", "häuser", "wohnung", "wohnungen", "hotel", "hotels", "restaurant", "restaurants",
        "café", "cafes", "kino", "kinos", "theater", "museum", "museen", "park", "parks",
        "bahnhof", "bahnhöfe", "flughafen", "flughäfen", "haltestelle", "haltestellen",
        "zug", "züge", "bahn", "bahnen", "bus", "busse", "auto", "autos", "fahrrad", "fahrräder",
        "taxi", "taxis", "flugzeug", "flugzeuge", "schiff", "schiffe", "straße", "straßen",
        "weg", "wege", "platz", "plätze", "brücke", "brücken", "stadt", "städte", "dorf", "dörfer", "land", "länder",
        "wasser", "brot", "brote", "brötchen", "milch", "kaffee", "tee", "zucker", "salz", "pfeffer",
        "butter", "käse", "fleisch", "fisch", "fische", "obst", "gemüse", "apfel", "äpfel",
        "banane", "bananen", "orange", "orangen", "kartoffel", "kartoffeln", "tomate", "tomaten",
        "salat", "salate", "suppe", "suppen", "kuchen", "eis", "speisekarte", "speisekarten",
        "rechnung", "rechnungen", "bestellung", "bestellungen", "trinkgeld", "geld", "gelder",
        "preis", "preise", "euro", "cent", "hund", "hunde", "katze", "katzen", "vogel", "vögel",
        "pferd", "pferde", "kuh", "kühe", "schaf", "schafe", "buch", "bücher", "heft", "hefte",
        "stift", "stifte", "bleistift", "kugelschreiber", "tasche", "taschen", "rucksack", "koffer",
        "handy", "handys", "computer", "uhr", "uhren", "brille", "schlüssel", "kleid", "kleider",
        "hose", "hosen", "hemd", "hemden", "jacke", "jacken", "mantel", "mäntel", "schuh", "schuhe",
        "kopf", "köpfe", "auge", "augen", "ohr", "ohren", "nase", "nasen", "mund", "münder",
        "zahn", "zähne", "hals", "hälse", "arm", "arme", "hand", "hände", "finger", "bein", "beine",
        "fuß", "füße", "herz", "herzen", "magen", "magen", "körper",
        "tag", "tage", "nacht", "nächte", "morgen", "abend", "abende", "woche", "wochen",
        "monat", "monate", "jahr", "jahre", "stunde", "stunden", "minute", "minuten", "sekunde", "sekunden",
        "zeit", "zeiten", "uhrzeit", "frühling", "sommer", "herbst", "winter",
        "montag", "dienstag", "mittwoch", "donnerstag", "freitag", "samstag", "sonntag",
        "januar", "februar", "märz", "april", "mai", "juni", "juli", "august", "september", "oktober", "november", "dezember",
        "vater", "väter", "mutter", "mütter", "eltern", "bruder", "brüder", "schwester", "schwestern",
        "sohn", "söhne", "tochter", "töchter", "mann", "männer", "frau", "frauen", "kind", "kinder",
        "mädchen", "junge", "jungen", "freund", "freunde", "freundin", "freundinnen",
        "schule", "schulen", "universität", "universitäten", "unterricht", "prüfung", "prüfungen",
        "arbeit", "arbeiten", "beruf", "berufe", "firma", "firmen", "büro", "büros",
        "frage", "fragen", "antwort", "antworten", "problem", "probleme", "hilfe", "beispiel", "beispiele",
        "leben", "sonne", "mond", "sterne", "wetter", "regen", "schnee", "wind"
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

        val lowerText = trimmed.lowercase()
        val words = lowerText.split(Regex("\\s+")).filter { it.isNotBlank() }

        // 2. Pre-check against verified local German lexicons & dictionaries
        // Check exact match in DictionaryLexicon, GermanFunctionWords, VerbConjugator, and CommonPhrases
        if (DictionaryLexicon.findByGerman(lowerText) != null) {
            return "de"
        }
        if (GermanFunctionWords.isFunctionWord(lowerText)) {
            return "de"
        }
        if (VerbConjugator.isGermanVerb(lowerText)) {
            return "de"
        }
        if (CommonPhrases.findPhrase(lowerText) != null) {
            return "de"
        }
        if (knownGermanVocabulary.contains(lowerText)) {
            return "de"
        }

        // For multi-word input, check if any token is a known German noun/verb/function word
        if (words.size > 1) {
            val anyGermanLexiconMatch = words.any {
                knownGermanVocabulary.contains(it) ||
                GermanFunctionWords.isFunctionWord(it) ||
                DictionaryLexicon.findByGerman(it) != null ||
                VerbConjugator.isGermanVerb(it)
            }
            val anyEnglishLexiconMatch = words.any { it in commonEnglishWords }
            if (anyGermanLexiconMatch && !anyEnglishLexiconMatch) {
                return "de"
            }
        }

        // 3. Tokenize and check against high-frequency words
        val germanMatchCount = words.count { it in commonGermanWords || it in knownGermanVocabulary }
        val englishMatchCount = words.count { it in commonEnglishWords }

        if (germanMatchCount > englishMatchCount && germanMatchCount > 0) {
            return "de"
        }
        if (englishMatchCount > germanMatchCount && englishMatchCount > 0) {
            return "en"
        }

        // 4. Linguistic patterns & German morphology
        // German digraphs / trigraphs: "sch", "tsch", "tz", "pf", "ck"
        val hasGermanDigraphs = lowerText.contains("sch") ||
                lowerText.contains("tsch") ||
                lowerText.contains("tz") ||
                lowerText.contains("pf") ||
                lowerText.contains("ck")

        // Characteristic German prefixes (e.g. verstehen, bekommen, gefallen, entgehen, aufstehen)
        val hasGermanPrefix = lowerText.startsWith("ver") ||
                lowerText.startsWith("zer") ||
                lowerText.startsWith("ge") ||
                lowerText.startsWith("be") ||
                lowerText.startsWith("ent") ||
                lowerText.startsWith("emp") ||
                lowerText.startsWith("miss") ||
                lowerText.startsWith("aus") ||
                lowerText.startsWith("ein") ||
                lowerText.startsWith("mit")

        // Characteristic German noun/adjective suffixes (including -in, -er for professions like Kellner, Kellnerin)
        val hasGermanSuffix = lowerText.endsWith("ung") ||
                lowerText.endsWith("keit") ||
                lowerText.endsWith("heit") ||
                lowerText.endsWith("schaft") ||
                lowerText.endsWith("chen") ||
                lowerText.endsWith("lein") ||
                lowerText.endsWith("lich") ||
                lowerText.endsWith("isch") ||
                lowerText.endsWith("haft") ||
                lowerText.endsWith("bar") ||
                lowerText.endsWith("sam") ||
                lowerText.endsWith("tum") ||
                lowerText.endsWith("ismus") ||
                (lowerText.length >= 6 && lowerText.endsWith("erin")) ||
                (lowerText.length >= 5 && lowerText.endsWith("ner")) ||
                (lowerText.length >= 5 && lowerText.endsWith("ler"))

        // German verb infinitive endings (-en, -eln, -ern for words longer than 3 letters)
        val hasGermanVerbEnding = lowerText.length > 3 && (
                lowerText.endsWith("eln") ||
                lowerText.endsWith("ern") ||
                (lowerText.endsWith("en") && !lowerText.endsWith("tion") && !lowerText.endsWith("sion") && !lowerText.endsWith("even") && !lowerText.endsWith("seven") && !lowerText.endsWith("open"))
        )

        if (hasGermanDigraphs || hasGermanSuffix || (hasGermanPrefix && hasGermanVerbEnding)) {
            return "de"
        }

        // 5. Characteristic English Suffixes & Digraphs
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

        // 6. German Noun Capitalization Check:
        // In German, all common and proper nouns are capitalized (e.g. "Kellner", "Tisch", "Arzt").
        // In English, common nouns are lowercase. If a single word starts with uppercase and rest is lowercase,
        // and is not the English pronoun "I", it strongly points to German in this dictionary context.
        if (words.size == 1 && trimmed.length >= 3 && trimmed.first().isUpperCase() && trimmed.substring(1).all { it.isLowerCase() }) {
            if (trimmed != "I" && !commonEnglishWords.contains(lowerText)) {
                return "de"
            }
        }

        // 7. ML Kit Language Identification for longer sentences (3+ words or 25+ chars)
        if (words.size >= 3 || trimmed.length >= 25) {
            try {
                val identifier = getLanguageIdentifier()
                if (identifier != null) {
                    val detected = identifier.identifyLanguage(trimmed).await()
                    when (detected.lowercase()) {
                        "de", "ger" -> return "de"
                        "ar", "ara" -> return "ar"
                        "en", "eng" -> {
                            // If ML Kit says English, but German vocabulary is detected, prefer German
                            if (germanMatchCount > 0) return "de"
                            return "en"
                        }
                    }
                }
            } catch (_: Throwable) {
                // If ML Kit throws or fails, smoothly continue to fallback
            }
        }

        // 8. Dictionary App Orientation Fallback:
        // In "DeutschAr" (German-Arabic dictionary), plain Latin words without explicit English features
        // default to German ("de") so users can search German vocabulary without needing special accents.
        if (commonEnglishWords.contains(lowerText)) {
            return "en"
        }

        return "de"
    }
}

