package com.example.core.translation

/**
 * Classification enum for German function words (non-nouns).
 */
enum class FunctionWordType(val displayNameAr: String, val displayNameDe: String) {
    PRONOUN("ضمير", "Pronomen"),
    PREPOSITION("حرف جر", "Präposition"),
    ARTICLE("أداة تعريف / تنكير", "Artikel"),
    AUX_VERB("فعل مساعد / ناقص", "Hilfsverb / Modalverb")
}

/**
 * Static lookup registry for high-frequency German function words (closed word classes).
 *
 * This provides a deterministic POS Pre-Check before querying the German Nouns database.
 * If an input word is identified as a function word (pronoun, preposition, article, auxiliary/modal verb),
 * it immediately bypasses the local nouns database and routes to ML Kit for translation,
 * preventing accidental substring/nominalization collisions and ensuring no article (der/die/das)
 * is assigned to non-noun words.
 */
object GermanFunctionWords {

    private val functionWordMap: Map<String, FunctionWordType> = buildMap {
        // --- 1. PRONOUNS (Personal, Reflexive, Possessive, Demonstrative, Relative, Indefinite) ---
        val pronouns = listOf(
            // Personal Pronouns (Nominative, Accusative, Dative, Genitive)
            "ich", "du", "er", "sie", "es", "wir", "ihr",
            "mich", "dich", "ihn", "uns", "euch",
            "mir", "dir", "ihm", "ihnen",
            "meiner", "deiner", "seiner", "ihrer", "unser", "euer",
            // Reflexive
            "sich",
            // Possessive Pronouns
            "mein", "meine", "meinen", "meinem", "meines",
            "dein", "deine", "deinen", "deinem", "deines",
            "sein", "seine", "seinen", "seinem", "seines",
            "ihre", "ihren", "ihrem", "ihres",
            "unsere", "unseren", "unserem", "unseres",
            "eure", "euren", "eurem", "eures",
            // Demonstrative & Indefinite
            "dieser", "diese", "dieses", "diesen", "diesem", "dies",
            "jener", "jene", "jenes", "jenen", "jenem",
            "man", "jemand", "jemandem", "jemanden", "niemand", "niemandem", "niemanden",
            "etwas", "nichts", "alles", "alle", "allen", "allem", "aller",
            "jeder", "jede", "jedes", "jedem", "jeden",
            "welcher", "welche", "welches", "welchen", "welchem",
            "wer", "wen", "wem", "wessen", "was"
        )
        for (w in pronouns) {
            put(w.lowercase(), FunctionWordType.PRONOUN)
        }

        // --- 2. PREPOSITIONS ---
        val prepositions = listOf(
            // Accusative
            "durch", "für", "fuer", "gegen", "ohne", "um", "bis", "entlang",
            // Dative
            "aus", "bei", "mit", "nach", "seit", "von", "zu", "gegenüber", "gegenueber", "außer", "ausser", "gemäß", "gemaess",
            // Two-Way (Wechselpräpositionen)
            "an", "auf", "hinter", "in", "neben", "über", "ueber", "unter", "vor", "zwischen",
            // Genitive & Others
            "während", "waehrend", "wegen", "trotz", "statt", "anstatt", "innerhalb", "außerhalb", "ausserhalb",
            "inmitten", "mittels", "unweit", "ab",
            // Contracted prepositions
            "im", "ins", "am", "ans", "zum", "zur", "vom", "beim", "übers", "uebers", "unters", "fürs", "fuers"
        )
        for (w in prepositions) {
            put(w.lowercase(), FunctionWordType.PREPOSITION)
        }

        // --- 3. ARTICLES / DETERMINERS ---
        val articles = listOf(
            // Definite articles
            "der", "die", "das", "des", "dem", "den",
            // Indefinite articles
            "ein", "eine", "einen", "einem", "einer", "eines",
            // Negative articles
            "kein", "keine", "keinen", "keinem", "keiner", "keines"
        )
        for (w in articles) {
            put(w.lowercase(), FunctionWordType.ARTICLE)
        }

        // --- 4. AUXILIARY & MODAL VERBS ---
        val auxAndModals = listOf(
            // sein
            "sein", "bin", "bist", "ist", "sind", "seid", "war", "warst", "waren", "wart", "gewesen", "wäre", "waere", "wären", "waeren",
            // haben
            "haben", "habe", "hast", "hat", "habt", "hatte", "hattest", "hatten", "hattet", "gehabt", "hätte", "haette", "hätten", "haetten",
            // werden
            "werden", "werde", "wirst", "wird", "werdet", "wurde", "wurdest", "wurden", "wurdet", "geworden", "worden", "würde", "wuerde", "würden", "wuerden",
            // können
            "können", "koennen", "kann", "kannst", "könnt", "koennt", "konnte", "konntest", "konnten", "konntet", "gekonnt", "könnte", "koennte", "könnten", "koennten",
            // müssen
            "müssen", "muessen", "muss", "musst", "müsst", "muesst", "musste", "musstest", "mussten", "musstet", "gemusst", "müsste", "muesste", "müssten", "muessten",
            // dürfen
            "dürfen", "duerfen", "darf", "darfst", "dürft", "duerft", "durfte", "durftest", "durften", "durftet", "gedurft", "dürfte", "duerfte", "dürften", "duerften",
            // wollen
            "wollen", "will", "willst", "wollt", "wollte", "wolltest", "wollten", "wolltet", "gewollt",
            // sollen
            "sollen", "soll", "sollst", "sollt", "sollte", "solltest", "sollten", "solltet", "gesollt",
            // mögen / möchten
            "mögen", "moegen", "mag", "magst", "mögt", "moegt", "mochte", "mochtest", "mochten", "gemocht",
            "möchte", "moechte", "möchtest", "moechtest", "möchten", "moechten", "möchtet", "moechtet",
            // lassen
            "lassen", "lasse", "lässt", "laesst", "lasst", "ließ", "liess", "gelassen"
        )
        for (w in auxAndModals) {
            put(w.lowercase(), FunctionWordType.AUX_VERB)
        }
    }

    /**
     * Checks if a single word (case-insensitive) is a known German function word.
     * Returns the [FunctionWordType] if matched, or null otherwise.
     */
    fun classifyFunctionWord(word: String): FunctionWordType? {
        val clean = word.trim().lowercase()
        if (clean.isEmpty() || clean.contains(" ")) return null
        return functionWordMap[clean]
    }

    /**
     * Helper boolean check.
     */
    fun isFunctionWord(word: String): Boolean = classifyFunctionWord(word) != null
}
