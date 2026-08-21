package com.example.core.translation

import com.example.core.database.ExampleEntity
import com.example.core.database.WordEntity

/**
 * Pre-seeded high-accuracy dictionary lexicon.
 * Contains grammatical parts of speech, exact Arabic/English translations, and verified example sentences.
 * Prioritized over general ML Kit machine translation.
 */
object DictionaryLexicon {

    data class LexiconEntry(
        val german: String,
        val article: String? = null,
        val plural: String? = null,
        val wordType: String, // "preposition", "conjunction", "adverb", "pronoun", "question_word", "numeral", "noun", "verb", "adjective"
        val posLabel: String, // e.g. "PRÄPOSITION / حرف جر", "KONJUNKTION / حرف عطف"
        val arabic: String,
        val english: String,
        val exampleDe: String? = null,
        val exampleAr: String? = null
    ) {
        fun toWordEntity(id: Long = 0): WordEntity = WordEntity(
            id = id,
            germanWord = german,
            article = article,
            plural = plural,
            wordType = wordType,
            arabicTranslation = arabic,
            pos = wordType
        )

        fun toExampleEntity(id: Long = 0, wordId: Long = 0): ExampleEntity? {
            if (exampleDe.isNullOrBlank() || exampleAr.isNullOrBlank()) return null
            return ExampleEntity(
                id = id,
                wordId = wordId,
                germanSentence = exampleDe,
                arabicTranslation = exampleAr
            )
        }
    }

    val allEntries: List<LexiconEntry> = listOf(
        // ================= PREPOSITIONS (حروف الجر) =================
        LexiconEntry("auf", null, null, "preposition", "PRÄPOSITION / حرف جر", "على / فوق / إلى", "on / upon / at", "Das Buch liegt auf dem Tisch.", "الكتاب موضوع على الطاولة."),
        LexiconEntry("in", null, null, "preposition", "PRÄPOSITION / حرف جر", "في / بـ / إلى داخل", "in / into / inside", "Ich bin in der Schule.", "أنا في المدرسة."),
        LexiconEntry("mit", null, null, "preposition", "PRÄPOSITION / حرف جر", "مع / بـ / بواسطة", "with / by", "Ich fahre mit dem Bus.", "أسافر بواسطة الحافلة."),
        LexiconEntry("zu", null, null, "preposition", "PRÄPOSITION / حرف جر", "إلى / لـ / نحو", "to / at / towards", "Ich gehe zu Fuß.", "أذهب مشياً على الأقدام."),
        LexiconEntry("von", null, null, "preposition", "PRÄPOSITION / حرف جر", "من / عن / لـ", "from / of / by", "Das ist ein Geschenk von mir.", "هذه هدية مني."),
        LexiconEntry("nach", null, null, "preposition", "PRÄPOSITION / حرف جر", "إلى / بعد / وفقاً لـ", "to / after / according to", "Wir fahren nach Berlin.", "نسافر إلى برلين."),
        LexiconEntry("bei", null, null, "preposition", "PRÄPOSITION / حرف جر", "عند / لدى / قرب", "at / with / near", "Er wohnt bei seinen Eltern.", "يسكن عند والديه."),
        LexiconEntry("für", null, null, "preposition", "PRÄPOSITION / حرف جر", "لـ / لأجل / في سبيل", "for", "Das ist für dich.", "هذا لأجلك."),
        LexiconEntry("ohne", null, null, "preposition", "PRÄPOSITION / حرف جر", "بدون / بلا / دون", "without", "Ohne dich kann ich nicht gehen.", "بدونك لا أستطيع الذهاب."),
        LexiconEntry("durch", null, null, "preposition", "PRÄPOSITION / حرف جر", "من خلال / عبر / بواسطة", "through / by", "Wir gehen durch den Park.", "نمشي عبر الحديقة."),
        LexiconEntry("über", null, null, "preposition", "PRÄPOSITION / حرف جر", "فوق / عن / عبر", "over / above / about", "Wir sprechen über das Problem.", "نتحدث عن المشكلة."),
        LexiconEntry("unter", null, null, "preposition", "PRÄPOSITION / حرف جر", "تحت / بين / أسفل", "under / below / among", "Die Katze ist unter dem Tisch.", "القطة تحت الطاولة."),
        LexiconEntry("vor", null, null, "preposition", "PRÄPOSITION / حرف جر", "أمام / قبل", "in front of / before", "Er steht vor der Tür.", "يقف أمام الباب."),
        LexiconEntry("hinter", null, null, "preposition", "PRÄPOSITION / حرف جر", "خلف / وراء", "behind", "Der Garten ist hinter dem Haus.", "الحديقة خلف المنزل."),
        LexiconEntry("neben", null, null, "preposition", "PRÄPOSITION / حرف جر", "بجانب / إلى جوار", "next to / beside", "Er sitzt neben mir.", "يجلس بجانبي."),
        LexiconEntry("zwischen", null, null, "preposition", "PRÄPOSITION / حرف جر", "بين", "between", "Das Bild hängt zwischen zwei Fenstern.", "الصورة معلقة بين نافذتين."),
        LexiconEntry("an", null, null, "preposition", "PRÄPOSITION / حرف جر", "على / عند / بجوار", "at / on / to", "Das Bild hängt an der Wand.", "الصورة معلقة على الحائط."),
        LexiconEntry("aus", null, null, "preposition", "PRÄPOSITION / حرف جر", "من / من أصل", "from / out of", "Ich komme aus Deutschland.", "أنا قادم من ألمانيا."),
        LexiconEntry("seit", null, null, "preposition", "PRÄPOSITION / حرف جر", "منذ", "since / for", "Ich lerne seit einem Jahr Deutsch.", "أتعلم الألمانية منذ سنة."),
        LexiconEntry("ab", null, null, "preposition", "PRÄPOSITION / حرف جر", "ابتداءً من / من", "from / starting from", "Ab morgen arbeite ich hier.", "ابتداءً من الغد أعمل هنا."),
        LexiconEntry("bis", null, null, "preposition", "PRÄPOSITION / حرف جر", "حتى / إلى غاية", "until / up to", "Bis morgen!", "إلى الغد!"),
        LexiconEntry("gegen", null, null, "preposition", "PRÄPOSITION / حرف جر", "ضد / مقابل / نحو", "against / towards", "Das Spiel war Deutschland gegen Frankreich.", "كانت المباراة ألمانيا ضد فرنسا."),
        LexiconEntry("um", null, null, "preposition", "PRÄPOSITION / حرف جر", "حول / عند / في تمام", "around / at", "Der Zug kommt um 8 Uhr an.", "يصل القطار في تمام الساعة الثامنة."),
        LexiconEntry("während", null, null, "preposition", "PRÄPOSITION / حرف جر", "خلال / أثناء", "during / while", "Während des Essens sprechen wir nicht.", "أثناء الأكل لا نتحدث."),
        LexiconEntry("trotz", null, null, "preposition", "PRÄPOSITION / حرف جر", "على الرغم من / بالرغم من", "despite / in spite of", "Trotz des Regens gehen wir spazieren.", "بالرغم من المطر نذهب للمشي."),
        LexiconEntry("wegen", null, null, "preposition", "PRÄPOSITION / حرف جر", "بسبب / لأجل", "because of / due to", "Wegen der Kälte bleibe ich zu Hause.", "بسبب البرد أبقى في المنزل."),

        // ================= CONJUNCTIONS (حروف العطف والربط) =================
        LexiconEntry("und", null, null, "conjunction", "KONJUNKTION / حرف عطف", "و / بالإضافة إلى", "and", "Ich trinke Kaffee und esse Brot.", "أشرب القهوة وآكل الخبز."),
        LexiconEntry("aber", null, null, "conjunction", "KONJUNKTION / حرف عطف", "لكن / غير أن", "but / however", "Er ist klein, aber stark.", "هو صغير لكنه قوي."),
        LexiconEntry("oder", null, null, "conjunction", "KONJUNKTION / حرف عطف", "أو / أم", "or", "Tee oder Kaffee?", "شاي أم قهوة؟"),
        LexiconEntry("weil", null, null, "conjunction", "KONJUNKTION / أداة ربط سببية", "لأن / بسبب", "because", "Ich lerne Deutsch, weil ich in Berlin wohne.", "أتعلم الألمانية لأني أسكن في برلين."),
        LexiconEntry("dass", null, null, "conjunction", "KONJUNKTION / أداة ربط", "أنّ / بأنّ", "that", "Ich weiß, dass du recht hast.", "أعلم أنك على حق."),
        LexiconEntry("denn", null, null, "conjunction", "KONJUNKTION / أداة ربط", "لأن / إذ", "because / for", "Ich bleibe hier, denn es regnet.", "سأبقى هنا لأنها تمطر."),
        LexiconEntry("obwohl", null, null, "conjunction", "KONJUNKTION / أداة ربط استدراكية", "على الرغم من / مع أن", "although / even though", "Obwohl er müde ist, arbeitet er weiter.", "على الرغم من أنه متعب، يستمر في العمل."),
        LexiconEntry("wenn", null, null, "conjunction", "KONJUNKTION / أداة شرط", "إذا / لو / عندما", "if / when", "Wenn es regnet, nehme ich einen Schirm.", "إذا أمطرت آخذ مظلة."),
        LexiconEntry("als", null, null, "conjunction", "KONJUNKTION / أداة ربط زمنية", "عندما / حينما / كـ", "when / as / than", "Als ich ein Kind war, lebte ich im Dorf.", "عندما كنت طفلاً عشت في القرية."),
        LexiconEntry("damit", null, null, "conjunction", "KONJUNKTION / أداة ربط غائية", "لكي / حتى", "so that / in order that", "Ich lerne fleißig, damit ich die Prüfung bestehe.", "أدرس باجتهاد لكي أنجح في الامتحان."),
        LexiconEntry("ob", null, null, "conjunction", "KONJUNKTION / أداة ربط", "ما إذا كان / هل", "whether / if", "Ich weiß nicht, ob er kommt.", "لا أعلم ما إذا كان سيأتي."),
        LexiconEntry("sondern", null, null, "conjunction", "KONJUNKTION / حرف عطف وإضراب", "بل / ولكن", "but rather", "Nicht ich, sondern er hat das gesagt.", "لست أنا بل هو من قال ذلك."),

        // ================= ADVERBS (الظروف والأحوال) =================
        LexiconEntry("heute", null, null, "adverb", "ADVERB / ظرف زمان", "اليوم", "today", "Heute ist das Wetter sehr schön.", "اليوم الطقس جميل جداً."),
        LexiconEntry("morgen", null, null, "adverb", "ADVERB / ظرف زمان", "غداً", "tomorrow", "Morgen habe ich frei.", "غداً لدي يوم عطلة."),
        LexiconEntry("gestern", null, null, "adverb", "ADVERB / ظرف زمان", "أمس / البارحة", "yesterday", "Gestern war ich im Kino.", "أمس كنت في السينما."),
        LexiconEntry("immer", null, null, "adverb", "ADVERB / ظرف تكرار", "دائماً / أبداً", "always", "Er ist immer pünktlich.", "هو دائماً منضبط في المواعيد."),
        LexiconEntry("nie", null, null, "adverb", "ADVERB / ظرف نفي", "أبداً / مطلقاً / لا أبداً", "never", "Ich habe das nie gesehen.", "لم أر ذلك أبداً."),
        LexiconEntry("oft", null, null, "adverb", "ADVERB / ظرف تكرار", "غالباً / كثيراً ما", "often", "Wir gehen oft spazieren.", "نحن نذهب كثيراً للمشي."),
        LexiconEntry("manchmal", null, null, "adverb", "ADVERB / ظرف تكرار", "أحياناً / بعض الأحيان", "sometimes", "Manchmal bleibe ich zu Hause.", "أحياناً أبقى في المنزل."),
        LexiconEntry("hier", null, null, "adverb", "ADVERB / ظرف مكان", "هنا", "here", "Hier ist mein Platz.", "هنا مكاني."),
        LexiconEntry("dort", null, null, "adverb", "ADVERB / ظرف مكان", "هناك", "there", "Dort drüben steht ein Baum.", "هناك يقف شجرة."),
        LexiconEntry("jetzt", null, null, "adverb", "ADVERB / ظرف زمان", "الآن / حالياً", "now", "Jetzt beginnt der Unterricht.", "الآن يبدأ الدرس."),
        LexiconEntry("schon", null, null, "adverb", "ADVERB / ظرف", "بالفعل / مسبقاً", "already", "Bist du schon fertig?", "هل انتهيت بالفعل؟"),
        LexiconEntry("noch", null, null, "adverb", "ADVERB / ظرف", "ما زال / بعد / أيضاً", "still / yet", "Er schläft noch.", "هو ما زال نائماً."),
        LexiconEntry("sehr", null, null, "adverb", "ADVERB / ظرف درجة", "جداً / للغاية", "very / highly", "Danke sehr!", "شكراً جزيلاً!"),
        LexiconEntry("auch", null, null, "adverb", "ADVERB / أداة عطف وتوكيد", "أيضاً / كذلك", "also / too", "Ich komme auch mit.", "أنا أيضاً سآتي معك."),
        LexiconEntry("nicht", null, null, "adverb", "PARTIKEL / أداة نفي", "لا / ليس / ما", "not", "Ich verstehe das nicht.", "أنا لا أفهم ذلك."),
        LexiconEntry("gern", null, null, "adverb", "ADVERB / ظرف", "بسرور / برغبة", "gladly / with pleasure", "Ich trinke gern Tee.", "أحب شرب الشاي."),
        LexiconEntry("vielleicht", null, null, "adverb", "ADVERB / ظرف احتمالية", "ربما / لعل", "maybe / perhaps", "Vielleicht kommt er morgen.", "ربما يأتي غداً."),
        LexiconEntry("zusammen", null, null, "adverb", "ADVERB / ظرف", "معاً / سوياً", "together", "Wir lernen zusammen.", "نتعلم معاً."),
        LexiconEntry("wieder", null, null, "adverb", "ADVERB / ظرف تكرار", "مرة أخرى / مجدداً", "again", "Komm bitte bald wieder!", "عد قريباً مجدداً من فضلك!"),
        LexiconEntry("bald", null, null, "adverb", "ADVERB / ظرف زمان", "قريباً / عما قريب", "soon", "Ich bin bald da.", "سأكون هناك قريباً."),

        // ================= PRONOUNS & QUESTION WORDS (الضمائر وأدوات الاستفهام) =================
        LexiconEntry("ich", null, null, "pronoun", "PRONOMEN / ضمير شخصي", "أنا", "I", "Ich lerne Deutsch.", "أنا أتعلم الألمانية."),
        LexiconEntry("du", null, null, "pronoun", "PRONOMEN / ضمير شخصي", "أنتَ / أنتِ", "you", "Wie heißt du?", "ما اسمك؟"),
        LexiconEntry("er", null, null, "pronoun", "PRONOMEN / ضمير شخصي", "هو", "he", "Er ist mein Freund.", "هو صديقي."),
        LexiconEntry("sie", null, null, "pronoun", "PRONOMEN / ضمير شخصي", "هي / هم / هنّ", "she / they", "Sie liest ein Buch.", "هي تقرأ كتاباً."),
        LexiconEntry("es", null, null, "pronoun", "PRONOMEN / ضمير شخصي", "هو / هي (للمحايد)", "it", "Es ist sehr kalt.", "الجو بارد جداً."),
        LexiconEntry("wir", null, null, "pronoun", "PRONOMEN / ضمير شخصي", "نحن", "we", "Wir lernen zusammen.", "نحن نتعلم معاً."),
        LexiconEntry("ihr", null, null, "pronoun", "PRONOMEN / ضمير شخصي", "أنتم / أنتنّ", "you (plural)", "Woher kommt ihr?", "من أين أنتم؟"),
        LexiconEntry("Sie", null, null, "pronoun", "PRONOMEN / ضمير احترام", "حضرتك / سيادتكم", "You (formal)", "Sprechen Sie Deutsch?", "هل تتحدث حضرتكم الألمانية؟"),
        LexiconEntry("man", null, null, "pronoun", "PRONOMEN / ضمير غير محدد", "المرء / الإنسان / الواحد", "one / you", "Wie sagt man das auf Deutsch?", "كيف يقول المرء ذلك بالألمانية؟"),
        LexiconEntry("wer", null, null, "question_word", "FRAGEWORT / اسم استفهام", "مَن", "who", "Wer ist das?", "مَن هذا؟"),
        LexiconEntry("was", null, null, "question_word", "FRAGEWORT / اسم استفهام", "ماذا / ما", "what", "Was machst du?", "ماذا تفعل؟"),
        LexiconEntry("wo", null, null, "question_word", "FRAGEWORT / اسم استفهام", "أين", "where", "Wo wohnst du?", "أين تسكن؟"),
        LexiconEntry("wie", null, null, "question_word", "FRAGEWORT / اسم استفهام", "كيف / كم", "how", "Wie geht es dir?", "كيف حالك؟"),
        LexiconEntry("warum", null, null, "question_word", "FRAGEWORT / اسم استفهام", "لماذا / لِمَ", "why", "Warum lernst du Deutsch?", "لماذا تتعلم الألمانية؟"),
        LexiconEntry("wann", null, null, "question_word", "FRAGEWORT / اسم استفهام", "متى", "when", "Wann kommst du?", "متى تأتي؟"),
        LexiconEntry("wohin", null, null, "question_word", "FRAGEWORT / اسم استفهام", "إلى أين", "where to", "Wohin fährst du?", "إلى أين أنت مسافر؟"),
        LexiconEntry("woher", null, null, "question_word", "FRAGEWORT / اسم استفهام", "من أين", "where from", "Woher kommst du?", "من أين أنت؟"),

        // ================= NUMERALS (الأعداد) =================
        LexiconEntry("eins", null, null, "numeral", "ZAHLWORT / عدد", "واحد (1)", "one (1)"),
        LexiconEntry("zwei", null, null, "numeral", "ZAHLWORT / عدد", "اثنان (2)", "two (2)"),
        LexiconEntry("drei", null, null, "numeral", "ZAHLWORT / عدد", "ثلاثة (3)", "three (3)"),
        LexiconEntry("vier", null, null, "numeral", "ZAHLWORT / عدد", "أربعة (4)", "four (4)"),
        LexiconEntry("fünf", null, null, "numeral", "ZAHLWORT / عدد", "خمسة (5)", "five (5)"),
        LexiconEntry("sechs", null, null, "numeral", "ZAHLWORT / عدد", "ستة (6)", "six (6)"),
        LexiconEntry("sieben", null, null, "numeral", "ZAHLWORT / عدد", "سبعة (7)", "seven (7)"),
        LexiconEntry("acht", null, null, "numeral", "ZAHLWORT / عدد", "ثمانية (8)", "eight (8)"),
        LexiconEntry("neun", null, null, "numeral", "ZAHLWORT / عدد", "تسعة (9)", "nine (9)"),
        LexiconEntry("zehn", null, null, "numeral", "ZAHLWORT / عدد", "عشرة (10)", "ten (10)"),

        // ================= HIGH-FREQUENCY NOUNS & ESSENTIAL VOCABULARY =================
        LexiconEntry("Kellner", "der", "Kellner", "noun", "NOMEN / اسم مذكر", "نادل / جرسون", "waiter / server", "Der Kellner bringt die Speisekarte.", "النادل يحضر قائمة الطعام."),
        LexiconEntry("Kellnerin", "die", "Kellnerinnen", "noun", "NOMEN / اسم مؤنث", "نادلة / جرسونة", "waitress", "Die Kellnerin nimmt die Bestellung auf.", "النادلة تأخذ الطلب."),
        LexiconEntry("Rechnung", "die", "Rechnungen", "noun", "NOMEN / اسم مؤنث", "فاتورة / حساب", "bill / check / invoice", "Die Rechnung, bitte!", "الحساب، من فضلك!"),
        LexiconEntry("Speisekarte", "die", "Speisekarten", "noun", "NOMEN / اسم مؤنث", "قائمة طعام / منيو", "menu", "Könnten wir bitte die Speisekarte haben?", "هل يمكننا الحصول على قائمة الطعام من فضلك؟"),
        LexiconEntry("Küche", "die", "Küchen", "noun", "NOMEN / اسم مؤنث", "مطبخ", "kitchen / cuisine", "Die Küche ist sehr modern.", "المطبخ حديث جداً."),
        LexiconEntry("Zimmer", "das", "Zimmer", "noun", "NOMEN / اسم محايد", "غرفة / حجرة", "room", "Ich habe ein Zimmer im Hotel gebucht.", "حجزت غرفة في الفندق."),
        LexiconEntry("Hotel", "das", "Hotels", "noun", "NOMEN / اسم محايد", "فندق", "hotel", "Das Hotel liegt im Stadtzentrum.", "الفندق يقع في وسط المدينة."),
        LexiconEntry("Restaurant", "das", "Restaurants", "noun", "NOMEN / اسم محايد", "مطعم", "restaurant", "Wir essen heute im Restaurant.", "نحن نأكل اليوم في المطعم."),
        LexiconEntry("Bahnhof", "der", "Bahnhöfe", "noun", "NOMEN / اسم مذكر", "محطة قطار", "train station", "Ich treffe dich am Bahnhof.", "سأقابلك في محطة القطار."),
        LexiconEntry("Flughafen", "der", "Flughäfen", "noun", "NOMEN / اسم مذكر", "مطار", "airport", "Wir fahren jetzt zum Flughafen.", "نحن ذاهبون الآن إلى المطار.")
    )

    private val germanMap: Map<String, LexiconEntry> by lazy {
        allEntries.associateBy { it.german.lowercase() }
    }

    private val englishMap: Map<String, LexiconEntry> by lazy {
        allEntries.associateBy { it.english.lowercase().substringBefore("/").trim() }
    }

    /**
     * Exact lookup by German word (case-insensitive).
     */
    fun findByGerman(word: String): LexiconEntry? {
        val clean = word.trim().lowercase()
        return germanMap[clean]
    }

    /**
     * Exact lookup by English word (case-insensitive).
     */
    fun findByEnglish(word: String): LexiconEntry? {
        val clean = word.trim().lowercase()
        return englishMap[clean] ?: allEntries.firstOrNull {
            it.english.lowercase().split("/").any { part -> part.trim() == clean }
        }
    }

    /**
     * Exact lookup by Arabic word.
     */
    fun findByArabic(word: String): LexiconEntry? {
        val clean = word.trim()
        return allEntries.firstOrNull {
            it.arabic.split("/").any { part -> part.trim() == clean } || it.arabic.trim() == clean
        }
    }

    /**
     * Finds exact match across any of the 3 languages.
     */
    fun findExactAny(query: String): LexiconEntry? {
        val clean = query.trim()
        return findByGerman(clean) ?: findByArabic(clean) ?: findByEnglish(clean)
    }

    fun getPosLabel(wordType: String?, fallback: String = "WORT"): String {
        return when (wordType?.lowercase()?.trim()) {
            "preposition", "präposition" -> "PRÄPOSITION / حرف جر"
            "conjunction", "konjunktion" -> "KONJUNKTION / حرف عطف"
            "adverb" -> "ADVERB / ظرف"
            "pronoun", "pronomen" -> "PRONOMEN / ضمير"
            "question_word", "fragewort" -> "FRAGEWORT / اسم استفهام"
            "numeral", "zahlwort", "zahl" -> "ZAHLWORT / عدد"
            "noun", "nomen" -> "NOMEN / اسم"
            "verb" -> "VERB / فعل"
            "adjective", "adjektiv" -> "ADJEKTIV / صفة"
            "phrase", "ausdruck" -> "AUSDRUCK / تعبير"
            else -> fallback
        }
    }
}
