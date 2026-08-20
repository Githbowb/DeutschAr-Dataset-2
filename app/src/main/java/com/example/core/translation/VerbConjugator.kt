package com.example.core.translation

import com.example.core.database.GermanVerbEntity

object VerbConjugator {

    private val KNOWN_NON_VERBS = setOf(
        "garten", "kissen", "regen", "wagen", "boden", "schaden", "hafen", "eisen",
        "zeichen", "magen", "faden", "besen", "bogen", "becken", "wesen", "morgen",
        "kuchen", "knochen", "rücken", "kasten", "namen", "leben", "offen", "selten",
        "innen", "außen", "unten", "oben", "hinten", "vorn", "vorne", "schon", "eben",
        "draußen", "drinnen", "zusammen", "wegen", "neben", "zwischen", "gegen", "ohne",
        "tisch", "stuhl", "mann", "tag", "monat", "vater", "bruder", "sohn", "freund",
        "apfel", "lehrer", "arzt", "hund", "weg", "kaffee", "frau", "mutter", "schwester",
        "tochter", "schule", "stadt", "sonne", "nacht", "woche", "zeit", "sprache",
        "arbeit", "tür", "katze", "frage", "haus", "buch", "kind", "auto", "wasser",
        "brot", "jahr", "bild", "zimmer", "geld", "land", "fenster", "mädchen", "problem",
        "gut", "schlecht", "groß", "klein", "schön", "neu", "alt", "schnell", "langsam",
        "einfach", "schwer", "heute", "gestern", "immer", "nie", "oft", "vielleicht",
        "frauen", "schulen", "städte", "tische", "stühle", "männer", "tage", "kinder",
        "bücher", "häuser", "autos", "bilder", "fenster", "katzen", "fragen", "türen",
        "sonnen", "nächte", "wochen", "zeiten", "sprachen", "arbeiten", "monate", "väter",
        "brüder", "söhne", "freunde", "äpfel", "lehrer", "ärzte", "hunde", "wege", "kaffees"
    )

    private val GERMAN_VERB_INFINITIVES = setOf(
        "gehen", "kommen", "sehen", "sprechen", "essen", "trinken", "haben", "sein",
        "machen", "lernen", "wohnen", "schreiben", "lesen", "fahren", "schlafen",
        "arbeiten", "kaufen", "fragen", "antworten", "helfen", "verstehen", "wissen",
        "brauchen", "finden", "geben", "nehmen", "bleiben", "stehen", "liegen",
        "bringen", "denken", "kennen", "fliegen", "schwimmen", "öffnen", "schließen",
        "beginnen", "verlieren", "gewinnen", "treffen", "halten", "lassen", "fallen",
        "steigen", "laufen", "tragen", "waschen", "vergessen", "sterben", "wachsen",
        "passieren", "geschehen", "ankommen", "aufstehen", "wandern", "spielen", "suchen",
        "lieben", "reisen", "kochen", "backen", "studieren", "besuchen", "bestellen",
        "erklären", "bezahlen", "zahlen", "verkaufen", "hören", "zuhören", "sagen",
        "zeigen", "nutzen", "benutzen", "warten", "erzählen", "sitzen", "stellen",
        "legen", "hängen", "hoffen", "wünschen", "fühlen", "glauben", "meinen",
        "erinnern", "senden", "schicken", "erhalten", "bekommen", "unterrichten",
        "lehren", "bauen", "zeichnen", "malen", "singen", "tanzen", "lachen", "weinen",
        "lächeln", "werfen", "fangen", "wählen", "auswählen", "anziehen", "ausziehen",
        "aufwachen", "wecken", "reparieren", "putzen", "duschen", "baden", "funktionieren",
        "gehören", "kosten", "versuchen", "probieren", "drücken", "ziehen", "schneiden",
        "schenken", "feiern", "gratulieren", "einladen", "teilnehmen", "abfahren",
        "mitkommen", "mitmachen", "anrufen", "telefonieren", "vorstellen", "überlegen",
        "vereinbaren", "buchen", "reservieren", "verabreden", "interessieren", "gefallen",
        "schmecken", "fehlen", "passen", "danken", "gehorchen", "folgen", "drohen",
        "schaden", "vertrauen", "nützen", "wehtun", "überweisen", "abholen", "ausfüllen",
        "aufhören", "anfangen", "einsteigen", "aussteigen", "umsteigen", "mitbringen",
        "einkaufen", "fernsehen", "aufräumen", "vorbereiten", "spazierengehen",
        "kennenlernen", "klingeln", "ändern", "verbessern", "erleben", "erreichen",
        "gewöhnen", "entschuldigen", "bedanken", "beschweren", "freuen", "fürchten",
        "ärgern", "beeilen", "konzentrieren", "kümmern", "streiten", "unterhalten",
        "überraschen", "verstecken", "verlieben", "verspäten", "wundern", "verlassen",
        "verzeihen", "versprechen", "verbinden", "verbringen", "verbrennen", "verbieten",
        "verhandeln", "verändern", "empfehlen", "empfangen", "entdecken", "entscheiden",
        "entwickeln", "entstehen", "erfinden", "erlauben", "erledigen", "erschrecken",
        "erwarten", "erziehen", "erkennen", "gelingen", "genügen", "bestehen",
        "betragen", "bewegen", "beweisen", "bewerben", "bedeuten", "begleiten",
        "behalten", "behandeln", "behaupten", "bemerken", "beraten", "berichten",
        "beschreiben", "besichtigen", "bieten", "bitten", "brennen", "decken", "dienen",
        "drucken", "dürfen", "können", "müssen", "sollen", "wollen", "mögen", "tun",
        "rennen", "springen", "tauchen", "segeln", "rudern", "reiten", "parken",
        "bremsen", "hupen", "tanken", "waschen", "bügeln", "nähen", "stricken",
        "schälen", "braten", "grillen", "rühren", "mischen", "gießen", "servieren",
        "schmecken", "riechen", "atmen", "husten", "niesen", "gähnen", "träumen",
        "ausruhen", "entspannen", "wehtun", "bluten", "heilen", "pflegen", "untersuchen",
        "operieren", "verletzen", "retten", "schützen", "verteidigen", "kämpfen",
        "siegen", "angreifen", "schlagen", "treten", "schießen", "töten", "zerstören",
        "beschädigen", "verhindern", "verbieten", "erlauben", "befehlen", "bitten",
        "danken", "entschuldigen", "begrüßen", "verabschieden", "streiten", "diskutieren",
        "berichten", "informieren", "mitteilen", "beantworten", "wiederholen", "übersetzen",
        "korrigieren", "prüfen", "testen", "kontrollieren", "messen", "wiegen", "zählen",
        "rechnen", "berechnen", "schätzen", "planen", "organisieren", "leiten", "führen",
        "folgen", "gehorchen", "dienen", "helfen", "unterstützen", "begleiten", "führen"
    )

    private val IRREGULAR_FORMS_TO_INFINITIVE = mapOf(
        "bin" to "sein", "bist" to "sein", "ist" to "sein", "sind" to "sein", "seid" to "sein", "war" to "sein", "gewesen" to "sein", "wäre" to "sein",
        "habe" to "haben", "hast" to "haben", "hat" to "haben", "habt" to "haben", "hatte" to "haben", "gehabt" to "haben", "hätte" to "haben",
        "werde" to "werden", "wirst" to "werden", "wird" to "werden", "werdet" to "werden", "wurde" to "werden", "geworden" to "werden", "würde" to "werden",
        "kann" to "können", "kannst" to "können", "könnt" to "können", "konnte" to "können", "gekonnt" to "können",
        "muss" to "müssen", "musst" to "müssen", "müsst" to "müssen", "musste" to "müssen", "gemusst" to "müssen",
        "darf" to "dürfen", "darfst" to "dürfen", "dürft" to "dürfen", "durfte" to "dürfen", "gedurft" to "dürfen",
        "will" to "wollen", "willst" to "wollen", "wollt" to "wollen", "wollte" to "wollen", "gewollt" to "wollen",
        "soll" to "sollen", "sollst" to "sollen", "sollt" to "sollen", "sollte" to "sollen", "gesollt" to "sollen",
        "mag" to "mögen", "magst" to "mögen", "mögt" to "mögen", "mochte" to "mögen", "gemocht" to "mögen", "möchte" to "mögen", "möchtest" to "mögen", "möchten" to "mögen", "möchtet" to "mögen",
        "weiß" to "wissen", "weißt" to "wissen", "wisst" to "wissen", "wusste" to "wissen", "gewusst" to "wissen",
        "gehe" to "gehen", "gehst" to "gehen", "geht" to "gehen", "ging" to "gehen", "gegangen" to "gehen", "ginge" to "gehen",
        "komme" to "kommen", "kommst" to "kommen", "kommt" to "kommen", "kam" to "kommen", "gekommen" to "kommen", "käme" to "kommen",
        "sehe" to "sehen", "siehst" to "sehen", "sieht" to "sehen", "seht" to "sehen", "sah" to "sehen", "gesehen" to "sehen", "sähe" to "sehen",
        "lese" to "lesen", "liest" to "lesen", "lest" to "lesen", "las" to "lesen", "gelesen" to "lesen", "läse" to "lesen",
        "spreche" to "sprechen", "sprichst" to "sprechen", "spricht" to "sprechen", "sprecht" to "sprechen", "sprach" to "sprechen", "gesprochen" to "sprechen", "spräche" to "sprechen",
        "nehme" to "nehmen", "nimmst" to "nehmen", "nimmt" to "nehmen", "nehmt" to "nehmen", "nahm" to "nehmen", "genommen" to "nehmen", "nähme" to "nehmen",
        "gebe" to "geben", "gibst" to "geben", "gibt" to "geben", "gebt" to "geben", "gab" to "geben", "gegeben" to "geben", "gäbe" to "geben",
        "helfe" to "helfen", "hilfst" to "helfen", "hilft" to "helfen", "helft" to "helfen", "half" to "helfen", "geholfen" to "helfen", "hülfe" to "helfen",
        "fahre" to "fahren", "fährst" to "fahren", "fährt" to "fahren", "fahrt" to "fahren", "fuhr" to "fahren", "gefahren" to "fahren", "führe" to "fahren",
        "laufe" to "laufen", "läufst" to "laufen", "läuft" to "laufen", "lauft" to "laufen", "lief" to "laufen", "gelaufen" to "laufen",
        "schlafe" to "schlafen", "schläfst" to "schlafen", "schläft" to "schlafen", "schlaft" to "schlafen", "schlief" to "schlafen", "geschlafen" to "schlafen",
        "trage" to "tragen", "trägst" to "tragen", "trägt" to "tragen", "tragt" to "tragen", "trug" to "tragen", "getragen" to "tragen",
        "wasche" to "waschen", "wäschst" to "waschen", "wäscht" to "waschen", "wascht" to "waschen", "wusch" to "waschen", "gewaschen" to "waschen",
        "esse" to "essen", "isst" to "essen", "esst" to "essen", "aß" to "essen", "gegessen" to "essen", "äße" to "essen",
        "trinke" to "trinken", "trinkst" to "trinken", "trinkt" to "trinken", "trank" to "trinken", "getrunken" to "trinken",
        "schreibe" to "schreiben", "schreibst" to "schreiben", "schreibt" to "schreiben", "schrieb" to "schreiben", "geschrieben" to "schreiben",
        "bleibe" to "bleiben", "bleibst" to "bleiben", "bleibt" to "bleiben", "blieb" to "bleiben", "geblieben" to "bleiben",
        "finde" to "finden", "findest" to "finden", "findet" to "finden", "fand" to "finden", "gefunden" to "finden",
        "stehe" to "stehen", "stehst" to "stehen", "steht" to "stehen", "stand" to "stehen", "gestanden" to "stehen",
        "liege" to "liegen", "liegst" to "liegen", "liegt" to "liegen", "lag" to "liegen", "gelegen" to "liegen",
        "bringe" to "bringen", "bringst" to "bringen", "bringt" to "bringen", "brachte" to "bringen", "gebracht" to "bringen",
        "denke" to "denken", "denkst" to "denken", "denkt" to "denken", "dachte" to "denken", "gedacht" to "denken",
        "kenne" to "kennen", "kennst" to "kennen", "kennt" to "kennen", "kannte" to "kennen", "gekannt" to "kennen",
        "fliege" to "fliegen", "fliegst" to "fliegen", "fliegt" to "fliegen", "flog" to "fliegen", "geflogen" to "fliegen",
        "schwimme" to "schwimmen", "schwimmst" to "schwimmen", "schwimmt" to "schwimmen", "schwamm" to "schwimmen", "geschwommen" to "schwimmen",
        "öffne" to "öffnen", "öffnest" to "öffnen", "öffnet" to "öffnen", "öffnete" to "öffnen", "geöffnet" to "öffnen",
        "schließe" to "schließen", "schließt" to "schließen", "schloss" to "schließen", "geschlossen" to "schließen",
        "verstehe" to "verstehen", "verstehst" to "verstehen", "versteht" to "verstehen", "verstand" to "verstehen", "verstanden" to "verstehen",
        "beginne" to "beginnen", "beginnst" to "beginnen", "beginnt" to "beginnen", "begann" to "beginnen", "begonnen" to "beginnen",
        "verliere" to "verlieren", "verlierst" to "verlieren", "verliert" to "verlieren", "verlor" to "verlieren", "verloren" to "verlieren",
        "gewinne" to "gewinnen", "gewinnst" to "gewinnen", "gewinnt" to "gewinnen", "gewann" to "gewinnen", "gewonnen" to "gewinnen",
        "treffe" to "treffen", "triffst" to "treffen", "trifft" to "treffen", "traf" to "treffen", "getroffen" to "treffen",
        "halte" to "halten", "hältst" to "halten", "hält" to "halten", "haltet" to "halten", "hielt" to "halten", "gehalten" to "halten",
        "lasse" to "lassen", "lässt" to "lassen", "lasst" to "lassen", "ließ" to "lassen", "gelassen" to "lassen",
        "falle" to "fallen", "fällst" to "fallen", "fällt" to "fallen", "fallt" to "fallen", "fiel" to "fallen", "gefallen" to "fallen"
    )

    private val ENGLISH_VERBS = setOf(
        "go", "come", "see", "look", "watch", "speak", "talk", "say", "tell", "eat", "drink",
        "have", "be", "do", "make", "learn", "study", "live", "stay", "reside", "write", "read",
        "drive", "ride", "travel", "fly", "sleep", "work", "buy", "purchase", "sell", "pay",
        "ask", "answer", "reply", "help", "assist", "understand", "know", "need", "want", "find",
        "give", "take", "bring", "fetch", "get", "play", "cook", "bake", "clean", "wash", "listen",
        "hear", "open", "close", "shut", "start", "begin", "finish", "end", "stop", "call", "phone",
        "meet", "visit", "search", "seek", "explain", "describe", "change", "try", "attempt",
        "use", "hope", "wish", "feel", "think", "remember", "forget", "send", "receive", "show",
        "teach", "win", "lose", "cut", "build", "draw", "paint", "sing", "dance", "laugh", "cry",
        "smile", "grow", "fall", "rise", "throw", "catch", "choose", "wear", "wake", "break",
        "fix", "repair", "arrive", "leave", "depart", "enter", "exit", "return", "wait", "run",
        "walk", "swim", "climb", "jump", "sit", "stand", "lie", "put", "place", "set", "order",
        "deliver", "sign", "print", "love", "like", "hate", "prefer", "enjoy", "invite", "thank",
        "spend", "save", "smoke", "taste", "smell", "breathe", "cough", "touch", "hold", "carry"
    )

    private val ARABIC_VERB_ROOTS_AND_FORMS = setOf(
        "ذهب", "يذهب", "اذهب", "راح", "يروح",
        "جاء", "يجيء", "أتى", "يأتي", "تعال",
        "رأى", "يرى", "شاهد", "يشاهد", "انظر", "شوف", "يشوف",
        "تكلم", "يتكلم", "تحدث", "يتحدث", "قال", "يقول", "حكى", "يحكي",
        "أكل", "يأكل", "تناول", "يتناول", "كل",
        "شرب", "يشرب", "اشرب",
        "ملك", "يملك", "عنده", "لديه",
        "كان", "يكون",
        "فعل", "يفعل", "صنع", "يصنع", "عمل", "يعمل", "سوى", "يسوي",
        "درس", "يدرس", "تعلم", "يتعلم", "ذاكر", "يذاكر",
        "سكن", "يسكن", "عاش", "يعيش", "أقام", "يقيم",
        "كتب", "يكتب", "اكتب",
        "قرأ", "يقرأ", "اقرأ",
        "سافر", "يسافر", "قاد", "يقود", "ركب", "يركب",
        "نام", "ينام", "استيقظ", "يستيقظ",
        "اشترى", "يشتري", "باع", "يبيع", "دفع", "يدفع",
        "سأل", "يسأل", "أجاب", "يجيب", "رد", "يرد",
        "ساعد", "يساعد", "أعان", "يعين",
        "فهم", "يفهم", "استوعب", "يستوعب",
        "عرف", "يعرف", "علم", "يعلم",
        "احتاج", "يحتاج", "طلب", "يطلب",
        "وجد", "يجد", "عثر", "يعثر",
        "أعطى", "يعطي", "منح", "يمنح",
        "أخذ", "يأخذ", "جلب", "يجلب", "أحضر", "يحضر",
        "لعب", "يلعب", "العب",
        "طبخ", "يطبخ", "خبز", "يخبز",
        "نظف", "ينظف", "غسل", "يغسل",
        "سمع", "يسمع", "استمع", "يستمع",
        "فتح", "يفتح", "أغلق", "يغلق", "سكر", "يسكر",
        "بدأ", "يبدأ", "انتهى", "ينتهي", "أنهى", "ينهي", "وقف", "يوقف",
        "اتصل", "يتصل", "نادى", "ينادي",
        "التقى", "يلتقي", "قابل", "يقابل",
        "زار", "يزور", "استقبل", "يستقبل",
        "بحث", "يبحث", "دور", "يدور",
        "شرح", "يشرح", "وضح", "يوضح", "فسر", "يفسر",
        "غير", "يغير", "بدل", "يبدل",
        "حاول", "يحاول", "جرب", "يجرب",
        "استخدم", "يستخدم", "استعمل", "يستعمل",
        "تمنى", "يتمنى", "أمل", "يأمل",
        "شعر", "يشعر", "حس", "يحس",
        "فكر", "يفكر", "ظن", "يظن", "اعتقد", "يعتقد",
        "تذكر", "يتذكر", "نسي", "ينسى",
        "أرسل", "يرسل", "استلم", "يستلم",
        "عرض", "يعرض", "أظهر", "يظهر",
        "فاز", "يفوز", "ربح", "يربح", "خسر", "يخسر",
        "قطع", "يقطع", "بنى", "يبني",
        "رسم", "يرسم", "لون", "يلون",
        "غنى", "يغني", "رقص", "يرقص",
        "ضحك", "يضحك", "بكى", "يبكي", "ابتسم", "يبتسم",
        "وصل", "يصل", "غادر", "يغادر", "ترك", "يترك", "خرج", "يخرج", "دخل", "يدخل", "رجع", "يرجع", "عاد", "يعود",
        "انتظر", "ينتظر", "ركض", "يركض", "جرى", "يجري", "مشى", "يمشي",
        "سبح", "يسبح", "عوم", "يعوم", "تسلق", "يتسلق", "قفز", "يقفز",
        "جلس", "يجلس", "قعد", "يقعد", "وقف", "يقف", "قام", "يقوم", "استلقى", "يستلقي",
        "وضع", "يضع", "حط", "يحط", "وقع", "يوقع", "طبع", "يطبع",
        "أحب", "يحب", "كره", "يكره", "فضل", "يفضل", "عزم", "يعزم", "دعا", "يدعو", "شكر", "يشكر", "اعتذر", "يعتذر",
        "صرف", "يصرف", "وفر", "يوفر", "دخن", "يدخن", "تذوق", "يتذوق", "شم", "يشم", "تنفس", "يتنفس", "سعل", "يسعل",
        "لمس", "يلمس", "مسك", "يمسك", "حمل", "يحمل", "شال", "يشيل"
    )

    /**
     * Checks if a German string strictly represents a genuine verb.
     */
    fun isGermanVerb(word: String): Boolean {
        val clean = word.trim().lowercase().removePrefix("zu ").trim()
        if (clean.isBlank() || clean.length < 2) return false

        if (KNOWN_NON_VERBS.contains(clean)) return false
        if (IRREGULAR_FORMS_TO_INFINITIVE.containsKey(clean)) return true
        if (GERMAN_VERB_INFINITIVES.contains(clean)) return true

        // Verbs ending in -ieren (e.g. studieren, reservieren, fotografieren)
        if (clean.endsWith("ieren") && clean.length >= 6) {
            return true
        }

        // Inseparable/separable prefixes attached to known verb infinitives
        val prefixes = listOf("be", "ver", "er", "ent", "zer", "ge", "miss", "an", "auf", "aus", "ein", "mit", "nach", "vor", "zu", "ab", "über", "unter")
        for (p in prefixes) {
            if (clean.startsWith(p) && clean.length > p.length + 3) {
                val base = clean.removePrefix(p)
                if (GERMAN_VERB_INFINITIVES.contains(base)) {
                    return true
                }
            }
        }

        return false
    }

    /**
     * Checks if an English query is a verb.
     */
    fun isEnglishVerb(word: String): Boolean {
        val clean = word.trim().lowercase()
        if (clean.startsWith("to ") && clean.length > 3) return true
        val base = clean.removePrefix("to ").trim()
        return ENGLISH_VERBS.contains(base)
    }

    /**
     * Checks if an Arabic query is a verb.
     */
    fun isArabicVerb(word: String): Boolean {
        val clean = word.trim().removePrefix("أن ").trim()
        return ARABIC_VERB_ROOTS_AND_FORMS.contains(clean)
    }

    /**
     * Resolves any German verb form back to its canonical infinitive form.
     */
    fun resolveInfinitive(word: String): String {
        val clean = word.trim().lowercase().removePrefix("zu ").trim()
        if (clean.isBlank()) return word

        IRREGULAR_FORMS_TO_INFINITIVE[clean]?.let { return it }

        if (clean == "sein" || clean == "tun") return clean
        if (GERMAN_VERB_INFINITIVES.contains(clean) || clean.endsWith("ieren")) {
            return clean
        }

        if (clean.endsWith("en") || clean.endsWith("eln") || clean.endsWith("ern")) {
            return clean
        }

        // Participle starting with "ge"
        if (clean.startsWith("ge") && (clean.endsWith("t") || clean.endsWith("et"))) {
            val stem = clean.removePrefix("ge").removeSuffix("et").removeSuffix("t")
            if (stem.isNotBlank()) return "${stem}en"
        }

        // Past tense regular with "te"
        if (clean.endsWith("tete") || clean.endsWith("tet")) {
            val stem = clean.removeSuffix("tete").removeSuffix("tet")
            if (stem.isNotBlank()) return "${stem}ten"
        }
        if (clean.endsWith("te") || clean.endsWith("test") || clean.endsWith("ten")) {
            val stem = clean.removeSuffix("test").removeSuffix("ten").removeSuffix("te")
            if (stem.isNotBlank()) return "${stem}en"
        }

        // Present tense regular conjugated forms (e.g. spielt -> spielen, arbeite -> arbeiten)
        if (clean.endsWith("st")) {
            val stem = clean.removeSuffix("st")
            if (stem.isNotBlank()) return "${stem}en"
        }
        if (clean.endsWith("t") && !clean.endsWith("st")) {
            val stem = clean.removeSuffix("t")
            if (stem.endsWith("e")) return "${stem}n"
            if (stem.isNotBlank()) return "${stem}en"
        }
        if (clean.endsWith("e")) {
            val stem = clean.removeSuffix("e")
            if (stem.isNotBlank()) return "${stem}en"
        }

        return if (clean.endsWith("n")) clean else "${clean}en"
    }

    /**
     * Builds a complete [VerbConjugationResult] for a verb, displaying all 6 Präsens pronouns
     * (ich, du, er/sie/es, wir, ihr, sie/Sie) using database records or morphological derivation.
     */
    fun createConjugation(
        infinitive: String,
        verbEntity: GermanVerbEntity? = null,
        english: String = "",
        arabic: String = ""
    ): VerbConjugationResult {
        val canonicalInfinitive = resolveInfinitive(infinitive).trim().lowercase()

        if (verbEntity != null) {
            val inf = verbEntity.infinitive.ifBlank { canonicalInfinitive }
            val (ich, du, er, wir, ihr, sie) = derivePrasensForms(inf)

            return VerbConjugationResult(
                infinitive = inf,
                auxiliary = verbEntity.auxiliary.ifBlank { if (isMovementOrStateChange(inf)) "sein" else "haben" },
                prasensIch = verbEntity.presentIch?.ifBlank { null } ?: ich,
                prasensDu = verbEntity.presentDu?.ifBlank { null } ?: du,
                prasensErSieEs = verbEntity.presentErSieEs?.ifBlank { null } ?: er,
                prasensWir = verbEntity.presentWir?.ifBlank { null } ?: wir,
                prasensIhr = verbEntity.presentIhr?.ifBlank { null } ?: ihr,
                prasensSie = verbEntity.presentSie?.ifBlank { null } ?: sie,
                prateritumIch = verbEntity.pastIch ?: derivePrateritum(inf),
                partizipZwei = verbEntity.partizipZwei ?: derivePartizipZwei(inf),
                konjunktivZweiIch = verbEntity.konjunktivZwei,
                imperativSingular = verbEntity.imperativSingularForm ?: "${ich.removeSuffix("e")}!",
                imperativPlural = verbEntity.imperativPluralForm ?: "${ihr}!",
                englishTranslation = english,
                arabicTranslation = arabic
            )
        }

        // Full rule-based derivation for any German verb
        val (ich, du, er, wir, ihr, sie) = derivePrasensForms(canonicalInfinitive)
        val auxiliary = if (isMovementOrStateChange(canonicalInfinitive)) "sein" else "haben"
        val partizipZwei = derivePartizipZwei(canonicalInfinitive)
        val prateritum = derivePrateritum(canonicalInfinitive)

        return VerbConjugationResult(
            infinitive = canonicalInfinitive,
            auxiliary = auxiliary,
            prasensIch = ich,
            prasensDu = du,
            prasensErSieEs = er,
            prasensWir = wir,
            prasensIhr = ihr,
            prasensSie = sie,
            prateritumIch = prateritum,
            partizipZwei = partizipZwei,
            imperativSingular = "${ich}!",
            imperativPlural = "${ihr}!",
            englishTranslation = english,
            arabicTranslation = arabic
        )
    }

    private fun derivePrasensForms(infinitive: String): SixPrasensForms {
        // Special irregular overrides
        when (infinitive) {
            "sein" -> return SixPrasensForms("bin", "bist", "ist", "sind", "seid", "sind")
            "haben" -> return SixPrasensForms("habe", "hast", "hat", "haben", "habt", "haben")
            "werden" -> return SixPrasensForms("werde", "wirst", "wird", "werden", "werdet", "werden")
            "können" -> return SixPrasensForms("kann", "kannst", "kann", "können", "könnt", "können")
            "müssen" -> return SixPrasensForms("muss", "musst", "muss", "müssen", "müsst", "müssen")
            "dürfen" -> return SixPrasensForms("darf", "darfst", "darf", "dürfen", "dürft", "dürfen")
            "wollen" -> return SixPrasensForms("will", "willst", "will", "wollen", "wollt", "wollen")
            "sollen" -> return SixPrasensForms("soll", "sollst", "soll", "sollen", "sollt", "sollen")
            "mögen" -> return SixPrasensForms("mag", "magst", "mag", "mögen", "mögt", "mögen")
            "wissen" -> return SixPrasensForms("weiß", "weißt", "weiß", "wissen", "wisst", "wissen")
            "sehen" -> return SixPrasensForms("sehe", "siehst", "sieht", "sehen", "seht", "sehen")
            "lesen" -> return SixPrasensForms("lese", "liest", "liest", "lesen", "lest", "lesen")
            "sprechen" -> return SixPrasensForms("spreche", "sprichst", "spricht", "sprechen", "sprecht", "sprechen")
            "nehmen" -> return SixPrasensForms("nehme", "nimmst", "nimmt", "nehmen", "nehmt", "nehmen")
            "geben" -> return SixPrasensForms("gebe", "gibst", "gibt", "geben", "gebt", "geben")
            "helfen" -> return SixPrasensForms("helfe", "hilfst", "hilft", "helfen", "helft", "helfen")
            "fahren" -> return SixPrasensForms("fahre", "fährst", "fährt", "fahren", "fahrt", "fahren")
            "laufen" -> return SixPrasensForms("laufe", "läufst", "läuft", "laufen", "lauft", "laufen")
            "schlafen" -> return SixPrasensForms("schlafe", "schläfst", "schläft", "schlafen", "schlaft", "schlafen")
            "tragen" -> return SixPrasensForms("trage", "trägst", "trägt", "tragen", "tragt", "tragen")
            "waschen" -> return SixPrasensForms("wasche", "wäschst", "wäscht", "waschen", "wascht", "waschen")
            "essen" -> return SixPrasensForms("esse", "isst", "isst", "essen", "esst", "essen")
            "vergessen" -> return SixPrasensForms("vergesse", "vergisst", "vergisst", "vergessen", "vergesst", "vergessen")
            "treffen" -> return SixPrasensForms("treffe", "triffst", "trifft", "treffen", "trefft", "treffen")
            "halten" -> return SixPrasensForms("halte", "hältst", "hält", "halten", "haltet", "halten")
            "lassen" -> return SixPrasensForms("lasse", "lässt", "lässt", "lassen", "lasst", "lassen")
            "fallen" -> return SixPrasensForms("falle", "fällst", "fällt", "fallen", "fallt", "fallen")
        }

        // Handle verbs ending in -eln or -ern
        if (infinitive.endsWith("eln")) {
            val base = infinitive.removeSuffix("eln")
            val stem = "${base}l"
            return SixPrasensForms(
                ich = "${base}le",
                du = "${stem}st",
                erSieEs = "${stem}t",
                wir = infinitive,
                ihr = "${stem}t",
                sieSie = infinitive
            )
        }

        if (infinitive.endsWith("ern")) {
            val stem = infinitive.removeSuffix("n")
            return SixPrasensForms(
                ich = "${stem}e",
                du = "${stem}st",
                erSieEs = "${stem}t",
                wir = infinitive,
                ihr = "${stem}t",
                sieSie = infinitive
            )
        }

        val stem = if (infinitive.endsWith("en")) {
            infinitive.removeSuffix("en")
        } else if (infinitive.endsWith("n")) {
            infinitive.removeSuffix("n")
        } else {
            infinitive
        }

        val endsWithTD = stem.endsWith("t") || stem.endsWith("d") ||
                (stem.endsWith("m") && stem.length > 2 && !stem.endsWith("mm") && !isVowel(stem[stem.length - 2])) ||
                (stem.endsWith("n") && stem.length > 2 && !stem.endsWith("nn") && !isVowel(stem[stem.length - 2]))

        val endsWithSibilant = stem.endsWith("s") || stem.endsWith("ß") || stem.endsWith("z") ||
                stem.endsWith("tz") || stem.endsWith("x")

        val ich = "${stem}e"
        val du = when {
            endsWithTD -> "${stem}est"
            endsWithSibilant -> "${stem}t"
            else -> "${stem}st"
        }
        val erSieEs = when {
            endsWithTD -> "${stem}et"
            else -> "${stem}t"
        }
        val wir = infinitive
        val ihr = when {
            endsWithTD -> "${stem}et"
            else -> "${stem}t"
        }
        val sieSie = infinitive

        return SixPrasensForms(ich, du, erSieEs, wir, ihr, sieSie)
    }

    private fun isVowel(c: Char): Boolean {
        return c in "aeiouäöü"
    }

    private fun isMovementOrStateChange(infinitive: String): Boolean {
        val seinVerbs = setOf(
            "gehen", "kommen", "fahren", "fliegen", "laufen", "reisen",
            "rennen", "steigen", "fallen", "sterben", "wachsen", "bleiben",
            "sein", "werden", "passieren", "geschehen", "ankommen", "aufstehen",
            "schwimmen", "springen", "wandern", "einschlafen"
        )
        return seinVerbs.contains(infinitive) || infinitive.endsWith("gehen") ||
                infinitive.endsWith("kommen") || infinitive.endsWith("fahren")
    }

    private fun derivePartizipZwei(infinitive: String): String {
        when (infinitive) {
            "gehen" -> return "gegangen"
            "kommen" -> return "gekommen"
            "sehen" -> return "gesehen"
            "lesen" -> return "gelesen"
            "sprechen" -> return "gesprochen"
            "nehmen" -> return "genommen"
            "geben" -> return "gegeben"
            "helfen" -> return "geholfen"
            "fahren" -> return "gefahren"
            "laufen" -> return "gelaufen"
            "schlafen" -> return "geschlafen"
            "essen" -> return "gegessen"
            "trinken" -> return "getrunken"
            "schreiben" -> return "geschrieben"
            "bleiben" -> return "geblieben"
            "finden" -> return "gefunden"
            "fliegen" -> return "geflogen"
            "schwimmen" -> return "geschwommen"
            "stehen" -> return "gestanden"
            "liegen" -> return "gelegen"
            "bringen" -> return "gebracht"
            "denken" -> return "gedacht"
            "kennen" -> return "gekannt"
            "wissen" -> return "gewusst"
            "haben" -> return "gehabt"
            "sein" -> return "gewesen"
            "werden" -> return "geworden"
        }

        if (infinitive.startsWith("be") || infinitive.startsWith("ver") ||
            infinitive.startsWith("er") || infinitive.startsWith("ent") ||
            infinitive.startsWith("zer") || infinitive.startsWith("ge")
        ) {
            val stem = infinitive.removeSuffix("en").removeSuffix("n")
            return "${stem}t"
        }

        if (infinitive.endsWith("ieren")) {
            return "${infinitive.removeSuffix("en")}t"
        }
        val stem = infinitive.removeSuffix("en").removeSuffix("n")
        val suffix = if (stem.endsWith("t") || stem.endsWith("d")) "et" else "t"
        return "ge${stem}$suffix"
    }

    private fun derivePrateritum(infinitive: String): String {
        when (infinitive) {
            "gehen" -> return "ging"
            "kommen" -> return "kam"
            "sehen" -> return "sah"
            "lesen" -> return "las"
            "sprechen" -> return "sprach"
            "nehmen" -> return "nahm"
            "geben" -> return "gab"
            "helfen" -> return "half"
            "fahren" -> return "fuhr"
            "laufen" -> return "lief"
            "schlafen" -> return "schlief"
            "essen" -> return "aß"
            "trinken" -> return "trank"
            "schreiben" -> return "schrieb"
            "bleiben" -> return "blieb"
            "finden" -> return "fand"
            "fliegen" -> return "flog"
            "schwimmen" -> return "schwamm"
            "stehen" -> return "stand"
            "liegen" -> return "lag"
            "bringen" -> return "brachte"
            "denken" -> return "dachte"
            "kennen" -> return "kannte"
            "wissen" -> return "wusste"
            "haben" -> return "hatte"
            "sein" -> return "war"
            "werden" -> return "wurde"
        }
        val stem = infinitive.removeSuffix("en").removeSuffix("n")
        val suffix = if (stem.endsWith("t") || stem.endsWith("d")) "ete" else "te"
        return "${stem}$suffix"
    }

    private data class SixPrasensForms(
        val ich: String,
        val du: String,
        val erSieEs: String,
        val wir: String,
        val ihr: String,
        val sieSie: String
    )
}
