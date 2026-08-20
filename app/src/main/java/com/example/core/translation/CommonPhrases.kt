package com.example.core.translation

object CommonPhrases {

    data class PhraseItem(
        val german: String,
        val english: String,
        val arabic: String
    )

    private val phrases = listOf(
        PhraseItem("ich will", "I want", "أريد / أرغب"),
        PhraseItem("ich möchte", "I would like", "أود / أرغب في"),
        PhraseItem("wie geht es dir", "How are you", "كيف حالك؟"),
        PhraseItem("wie geht's", "How is it going", "كيف الأمور؟"),
        PhraseItem("guten Morgen", "Good morning", "صباح الخير"),
        PhraseItem("guten Tag", "Good day / Hello", "طاب يومك / مرحباً"),
        PhraseItem("guten Abend", "Good evening", "مساء الخير"),
        PhraseItem("gute Nacht", "Good night", "تصبح على خير"),
        PhraseItem("danke", "Thank you", "شكراً"),
        PhraseItem("danke schön", "Thank you very much", "شكراً جزيلاً"),
        PhraseItem("vielen Dank", "Many thanks", "شكراً جزيلاً لك"),
        PhraseItem("bitte", "Please / You're welcome", "من فضلك / عفواً"),
        PhraseItem("bitte schön", "You're very welcome", "عفواً / أهلاً بك"),
        PhraseItem("auf Wiedersehen", "Goodbye", "مع السلامة / إلى اللقاء"),
        PhraseItem("tschüss", "Bye", "وداعاً / باي"),
        PhraseItem("entschuldigung", "Excuse me / Sorry", "معذرة / عذراً"),
        PhraseItem("tut mir leid", "I'm sorry", "أنا آسف"),
        PhraseItem("kein Problem", "No problem", "لا توجد مشكلة"),
        PhraseItem("ich verstehe", "I understand", "أنا أفهم"),
        PhraseItem("ich verstehe nicht", "I don't understand", "أنا لا أفهم"),
        PhraseItem("ich weiß nicht", "I don't know", "لا أعلم / لا أعرف"),
        PhraseItem("sprechen Sie Englisch", "Do you speak English?", "هل تتحدث الإنجليزية؟"),
        PhraseItem("sprechen Sie Deutsch", "Do you speak German?", "هل تتحدث الألمانية؟"),
        PhraseItem("wie heißt du", "What is your name?", "ما اسمك؟"),
        PhraseItem("ich heiße", "My name is", "اسمي هو"),
        PhraseItem("woher kommst du", "Where are you from?", "من أين أنت؟"),
        PhraseItem("ich komme aus", "I come from", "أنا قادم من"),
        PhraseItem("wo ist", "Where is...?", "أين يوجد...؟"),
        PhraseItem("wie viel kostet das", "How much does that cost?", "كم ثمن هذا؟"),
        PhraseItem("ich liebe dich", "I love you", "أنا أحبك"),
        PhraseItem("alles Gute", "All the best", "كل التوفيق / مع أطيب التمنيات"),
        PhraseItem("herzlichen Glückwunsch", "Congratulations", "تهانينا الحارة / مبارك")
    )

    fun findPhrase(query: String): PhraseItem? {
        val trimmed = query.trim().lowercase()
        if (trimmed.isBlank()) return null
        
        // Exact match
        val exact = phrases.firstOrNull {
            it.german.lowercase() == trimmed ||
            it.english.lowercase() == trimmed ||
            it.arabic.trim() == query.trim()
        }
        if (exact != null) return exact

        // Only allow prefix/contains matching for multi-word input phrases
        if (trimmed.contains(" ")) {
            return phrases.firstOrNull {
                it.german.lowercase().startsWith(trimmed) ||
                trimmed.startsWith(it.german.lowercase())
            }
        }
        return null
    }
}
