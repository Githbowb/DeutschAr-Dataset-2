package com.example.core.database

import com.example.core.translation.DictionaryLexicon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DatabaseSeeder {

    suspend fun seedDatabaseIfEmpty(database: AppDatabase) = withContext(Dispatchers.IO) {
        val wordDao = database.wordDao()
        val nounDao = database.germanNounDao()
        val verbDao = database.germanVerbDao()
        val exampleDao = database.exampleDao()

        val existingWords = wordDao.getVocabularyList(5)
        if (existingWords.isEmpty()) {
            // 1. Initial High-Frequency German-Arabic Words
            val words = mutableListOf(
                // Verbs
                WordEntity(1, "gehen", null, null, "verb", "يذهب / يسير"),
                WordEntity(2, "kommen", null, null, "verb", "يأتي / يحضر"),
                WordEntity(3, "sehen", null, null, "verb", "يرى / يشاهد"),
                WordEntity(4, "sprechen", null, null, "verb", "يتكلم / يتحدث"),
                WordEntity(5, "essen", null, null, "verb", "يأكل / يتناول الطعام"),
                WordEntity(6, "trinken", null, null, "verb", "يشرب"),
                WordEntity(7, "haben", null, null, "verb", "يملك / لديه"),
                WordEntity(8, "sein", null, null, "verb", "يكون / يوجد"),
                WordEntity(9, "machen", null, null, "verb", "يفعل / يصنع / يعمل"),
                WordEntity(10, "lernen", null, null, "verb", "يتعلم / يدرس"),
                WordEntity(11, "wohnen", null, null, "verb", "يسكن / يقيم"),
                WordEntity(12, "schreiben", null, null, "verb", "يكتب"),
                WordEntity(13, "lesen", null, null, "verb", "يقرأ"),
                WordEntity(14, "fahren", null, null, "verb", "يقود / يسافر بالمركبة"),
                WordEntity(15, "schlafen", null, null, "verb", "ينام"),
                WordEntity(16, "arbeiten", null, null, "verb", "يعمل / يشتغل"),
                WordEntity(17, "kaufen", null, null, "verb", "يشتري"),
                WordEntity(18, "fragen", null, null, "verb", "يسأل"),
                WordEntity(19, "antworten", null, null, "verb", "يجيب / يرد"),
                WordEntity(20, "helfen", null, null, "verb", "يساعد / يعين"),
                WordEntity(21, "verstehen", null, null, "verb", "يفهم / يستوعب"),
                WordEntity(22, "wissen", null, null, "verb", "يعلم / يعرف"),
                WordEntity(23, "brauchen", null, null, "verb", "يحتاج إلى"),
                WordEntity(24, "finden", null, null, "verb", "يجد / يعثر على"),
                WordEntity(25, "geben", null, null, "verb", "يعطي / يمنح"),

                // Nouns (Masculine - der)
                WordEntity(26, "Tisch", "der", "Tische", "noun", "طاولة / منضدة"),
                WordEntity(27, "Stuhl", "der", "Stühle", "noun", "كرسي"),
                WordEntity(28, "Mann", "der", "Männer", "noun", "رجل"),
                WordEntity(29, "Tag", "der", "Tage", "noun", "يوم / نهار"),
                WordEntity(30, "Monat", "der", "Monate", "noun", "شهر"),
                WordEntity(31, "Vater", "der", "Väter", "noun", "أب / والد"),
                WordEntity(32, "Bruder", "der", "Brüder", "noun", "أخ"),
                WordEntity(33, "Sohn", "der", "Söhne", "noun", "ابن"),
                WordEntity(34, "Freund", "der", "Freunde", "noun", "صديق / رفيق"),
                WordEntity(35, "Apfel", "der", "Äpfel", "noun", "تفاحة"),
                WordEntity(36, "Lehrer", "der", "Lehrer", "noun", "معلم / مدرس"),
                WordEntity(37, "Arzt", "der", "Ärzte", "noun", "طبيب / دكتور"),
                WordEntity(38, "Hund", "der", "Hunde", "noun", "كلب"),
                WordEntity(39, "Weg", "der", "Wege", "noun", "طريق / مسار"),
                WordEntity(40, "Kaffee", "der", "Kaffees", "noun", "قهوة"),

                // Nouns (Feminine - die)
                WordEntity(41, "Frau", "die", "Frauen", "noun", "امرأة / سيدة / زوجة"),
                WordEntity(42, "Mutter", "die", "Mütter", "noun", "أم / والدة"),
                WordEntity(43, "Schwester", "die", "Schwestern", "noun", "أخت"),
                WordEntity(44, "Tochter", "die", "Töchter", "noun", "ابنة"),
                WordEntity(45, "Schule", "die", "Schulen", "noun", "مدرسة"),
                WordEntity(46, "Stadt", "die", "Städte", "noun", "مدينة"),
                WordEntity(47, "Sonne", "die", "Sonnen", "noun", "شمس"),
                WordEntity(48, "Nacht", "die", "Nächte", "noun", "ليل / ليلة"),
                WordEntity(49, "Woche", "die", "Wochen", "noun", "أسبوع"),
                WordEntity(50, "Zeit", "die", "Zeiten", "noun", "وقت / زمن"),
                WordEntity(51, "Sprache", "die", "Sprachen", "noun", "لغة"),
                WordEntity(52, "Arbeit", "die", "Arbeiten", "noun", "عمل / وظيفة"),
                WordEntity(53, "Tür", "die", "Türen", "noun", "باب"),
                WordEntity(54, "Katze", "die", "Katzen", "noun", "قطة"),
                WordEntity(55, "Frage", "die", "Fragen", "noun", "سؤال"),

                // Nouns (Neuter - das)
                WordEntity(56, "Haus", "das", "Häuser", "noun", "بيت / منزل"),
                WordEntity(57, "Buch", "das", "Bücher", "noun", "كتاب"),
                WordEntity(58, "Kind", "das", "Kinder", "noun", "طفل / ولد"),
                WordEntity(59, "Auto", "das", "Autos", "noun", "سيارة / مركبة"),
                WordEntity(60, "Wasser", "das", "Wasser", "noun", "ماء / مياه"),
                WordEntity(61, "Brot", "das", "Brote", "noun", "خبز"),
                WordEntity(62, "Jahr", "das", "Jahre", "noun", "سنة / عام"),
                WordEntity(63, "Bild", "das", "Bilder", "noun", "صورة / لوحة"),
                WordEntity(64, "Zimmer", "das", "Zimmer", "noun", "غرفة / حجرة"),
                WordEntity(65, "Geld", "das", "Gelder", "noun", "مال / نقود"),
                WordEntity(66, "Land", "das", "Länder", "noun", "بلد / دولة / أرض"),
                WordEntity(67, "Leben", "das", "Leben", "noun", "حياة / معيشة"),
                WordEntity(68, "Fenster", "das", "Fenster", "noun", "نافذة / شباك"),
                WordEntity(69, "Mädchen", "das", "Mädchen", "noun", "فتاة / بنت"),
                WordEntity(70, "Problem", "das", "Probleme", "noun", "مشكلة / معضلة"),

                // Adjectives & Adverbs
                WordEntity(71, "gut", null, null, "adjective", "جيد / طيب / حسن"),
                WordEntity(72, "schlecht", null, null, "adjective", "سيء / رديء"),
                WordEntity(73, "groß", null, null, "adjective", "كبير / عظيم / ضخم"),
                WordEntity(74, "klein", null, null, "adjective", "صغير / ضئيل"),
                WordEntity(75, "schön", null, null, "adjective", "جميل / رائع"),
                WordEntity(76, "neu", null, null, "adjective", "جديد / حديث"),
                WordEntity(77, "alt", null, null, "adjective", "قديم / مسن / كبير في السن"),
                WordEntity(78, "schnell", null, null, "adjective", "سريع"),
                WordEntity(79, "langsam", null, null, "adjective", "بطيء"),
                WordEntity(80, "einfach", null, null, "adjective", "بسيط / سهل"),
                WordEntity(81, "schwer", null, null, "adjective", "صعب / ثقيل"),
                WordEntity(82, "heute", null, null, "adverb", "اليوم"),
                WordEntity(83, "morgen", null, null, "adverb", "غداً"),
                WordEntity(84, "gestern", null, null, "adverb", "أمس / البارحة"),
                WordEntity(85, "immer", null, null, "adverb", "دائماً / أبداً")
            )

            // Add all entries from DictionaryLexicon with autoincrement ids
            var startId = 100L
            DictionaryLexicon.allEntries.forEach { entry ->
                if (words.none { it.germanWord.equals(entry.german, ignoreCase = true) }) {
                    words.add(entry.toWordEntity(startId++))
                }
            }

            wordDao.insertAll(words)

            // 2. German Noun Grammatical Entities
            val nouns = listOf(
                GermanNounEntity(1, "Tisch", "der", "Tische", "des Tisches", "dem Tisch", "den Tisch", "Nomen, maskulin"),
                GermanNounEntity(2, "Stuhl", "der", "Stühle", "des Stuhles", "dem Stuhl", "den Stuhl", "Nomen, maskulin"),
                GermanNounEntity(3, "Mann", "der", "Männer", "des Mannes", "dem Mann", "den Mann", "Nomen, maskulin"),
                GermanNounEntity(4, "Tag", "der", "Tage", "des Tages", "dem Tag", "den Tag", "Nomen, maskulin"),
                GermanNounEntity(5, "Vater", "der", "Väter", "des Vaters", "dem Vater", "den Vater", "Nomen, maskulin"),
                GermanNounEntity(6, "Frau", "die", "Frauen", "der Frau", "der Frau", "die Frau", "Nomen, feminin"),
                GermanNounEntity(7, "Mutter", "die", "Mütter", "der Mutter", "der Mutter", "die Mutter", "Nomen, feminin"),
                GermanNounEntity(8, "Schule", "die", "Schulen", "der Schule", "der Schule", "die Schule", "Nomen, feminin"),
                GermanNounEntity(9, "Stadt", "die", "Städte", "der Stadt", "der Stadt", "die Stadt", "Nomen, feminin"),
                GermanNounEntity(10, "Sonne", "die", "Sonnen", "der Sonne", "der Sonne", "die Sonne", "Nomen, feminin"),
                GermanNounEntity(11, "Haus", "das", "Häuser", "des Hauses", "dem Haus", "das Haus", "Nomen, neutral"),
                GermanNounEntity(12, "Buch", "das", "Bücher", "des Buches", "dem Buch", "das Buch", "Nomen, neutral"),
                GermanNounEntity(13, "Kind", "das", "Kinder", "des Kindes", "dem Kind", "das Kind", "Nomen, neutral"),
                GermanNounEntity(14, "Auto", "das", "Autos", "des Autos", "dem Auto", "das Auto", "Nomen, neutral"),
                GermanNounEntity(15, "Wasser", "das", "Wasser", "des Wassers", "dem Wasser", "das Wasser", "Nomen, neutral")
            )
            nounDao.insertNouns(nouns)

            // 3. German Verb Conjugation Entities
            val verbs = listOf(
                GermanVerbEntity(1, "gehen", "sein", "gehe", "gehst", "geht", "gehen", "geht", "gehen", "ging", "gegangen", "ginge", "geh!", "geht!"),
                GermanVerbEntity(2, "kommen", "sein", "komme", "kommst", "kommt", "kommen", "kommt", "kommen", "kam", "gekommen", "käme", "komm!", "kommt!"),
                GermanVerbEntity(3, "sehen", "haben", "sehe", "siehst", "sieht", "sehen", "seht", "sehen", "sah", "gesehen", "sähe", "sieh!", "seht!"),
                GermanVerbEntity(4, "sprechen", "haben", "spreche", "sprichst", "spricht", "sprechen", "sprecht", "sprechen", "sprach", "gesprochen", "spräche", "sprich!", "sprecht!"),
                GermanVerbEntity(5, "essen", "haben", "esse", "isst", "isst", "essen", "esst", "essen", "aß", "gegessen", "äße", "iss!", "esst!"),
                GermanVerbEntity(6, "trinken", "haben", "trinke", "trinkst", "trinkt", "trinken", "trinkt", "trinken", "trank", "getrunken", "tränke", "trink!", "trinkt!"),
                GermanVerbEntity(7, "haben", "haben", "habe", "hast", "hat", "haben", "habt", "haben", "hatte", "gehabt", "hätte", "hab!", "habt!"),
                GermanVerbEntity(8, "sein", "sein", "bin", "bist", "ist", "sind", "seid", "sind", "war", "gewesen", "wäre", "sei!", "seid!"),
                GermanVerbEntity(9, "machen", "haben", "mache", "machst", "macht", "machen", "macht", "machen", "machte", "gemacht", "machte", "mach!", "macht!"),
                GermanVerbEntity(10, "lernen", "haben", "lerne", "lernst", "lernt", "lernen", "lernt", "lernen", "lernte", "gelernt", "lernte", "lern!", "lernt!"),
                GermanVerbEntity(11, "schreiben", "haben", "schreibe", "schreibst", "schreibt", "schreiben", "schreibt", "schreiben", "schrieb", "geschrieben", "schriebe", "schreib!", "schreibt!"),
                GermanVerbEntity(12, "lesen", "haben", "lese", "liest", "liest", "lesen", "lest", "lesen", "las", "gelesen", "läse", "lies!", "lest!"),
                GermanVerbEntity(13, "fahren", "sein", "fahre", "fährst", "fährt", "fahren", "fahrt", "fahren", "fuhr", "gefahren", "führe", "fahr!", "fahrt!"),
                GermanVerbEntity(14, "arbeiten", "haben", "arbeite", "arbeitest", "arbeitet", "arbeiten", "arbeitet", "arbeiten", "arbeitete", "gearbeitet", "arbeitete", "arbeite!", "arbeitet!"),
                GermanVerbEntity(15, "helfen", "haben", "helfe", "hilfst", "hilft", "helfen", "helft", "helfen", "half", "geholfen", "hülfe", "hilf!", "helft!")
            )
            verbDao.insertVerbs(verbs)

            // 4. Example Sentences
            val examples = mutableListOf(
                ExampleEntity(1, 1, "Ich gehe jeden Tag zur Schule.", "أنا أذهب كل يوم إلى المدرسة."),
                ExampleEntity(2, 2, "Wann kommst du nach Hause?", "متى ستأتي إلى المنزل؟"),
                ExampleEntity(3, 3, "Ich sehe einen schönen Baum im Garten.", "أرى شجرة جميلة في الحديقة."),
                ExampleEntity(4, 4, "Er spricht fließend Deutsch und Arabisch.", "هو يتحدث الألمانية والعربية بطلاقة."),
                ExampleEntity(5, 5, "Wir essen gerne frisches Brot.", "نحن نحب أكل الخبز الطازج."),
                ExampleEntity(6, 6, "Möchtest du ein Glas Wasser trinken?", "هل ترغب في شرب كأس من الماء؟"),
                ExampleEntity(7, 7, "Ich habe heute viel Zeit zum Lernen.", "لدي اليوم متسع كبير من الوقت للدراسة."),
                ExampleEntity(8, 8, "Das Wetter ist heute sehr schön.", "الطقس جميل جداً اليوم."),
                ExampleEntity(9, 9, "Was machst du am Wochenende?", "ماذا تفعل في عطلة نهاية الأسبوع؟"),
                ExampleEntity(10, 10, "Sie lernt fleißig Deutsch für die Prüfung.", "هي تدرس الألمانية باجتهاد للاختبار."),
                ExampleEntity(11, 26, "Das Buch liegt auf dem Tisch.", "الكتاب موضوع على الطاولة."),
                ExampleEntity(12, 41, "Die Frau arbeitet als Ärztin im Krankenhaus.", "تعمل المرأة كطبيبة في المستشفى."),
                ExampleEntity(13, 56, "Das Haus hat einen großen und grünen Garten.", "البيت يحتوي على حديقة كبيرة وخضراء."),
                ExampleEntity(14, 57, "Ich lese ein sehr interessantes Buch.", "أنا أقرأ كتاباً ممتعاً للغاية."),
                ExampleEntity(15, 60, "Wasser ist lebensnotwendig für alle Menschen.", "الماء ضروري للحياة لجميع البشر.")
            )

            var exId = 100L
            DictionaryLexicon.allEntries.forEach { entry ->
                entry.toExampleEntity(id = exId++)?.let { examples.add(it) }
            }

            examples.forEach { exampleDao.insert(it) }
        } else {
            // If already seeded in an earlier app version, ensure all Lexicon entries (prepositions, conjunctions, etc.) exist
            if (wordDao.findByGermanWordExact("auf") == null) {
                var startId = 500L
                val missingWords = mutableListOf<WordEntity>()
                DictionaryLexicon.allEntries.forEach { entry ->
                    if (wordDao.findByGermanWordExact(entry.german) == null) {
                        missingWords.add(entry.toWordEntity(startId++))
                    }
                }
                if (missingWords.isNotEmpty()) {
                    wordDao.insertAll(missingWords)
                }
            }
        }
    }
}
