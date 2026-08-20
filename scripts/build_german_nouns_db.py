#!/usr/bin/env python3
"""
German Nouns Database Builder for Deutsch-Arabisch (deutschar.db)

Source: gambolputty/german-nouns (Compiled from WiktionaryDE)
Repository: https://github.com/gambolputty/german-nouns
URL: https://raw.githubusercontent.com/gambolputty/german-nouns/main/german_nouns/nouns.csv

This script parses all ~102,000 German nouns from `nouns.csv`,
extracts lemma, genus (mapped to der/die/das), pos, and flexion tables
(nominativ, genitiv, dativ, akkusativ singular and plural),
and creates a pre-built Room SQLite database in `app/src/main/assets/deutschar.db`.
"""

import os
import sys
import csv
import io
import time
import sqlite3
import subprocess

NOUNS_LOCAL_PATH = "/tmp/german-nouns/german_nouns/nouns.csv"
OUTPUT_DB_PATH = "app/src/main/assets/deutschar.db"

def ensure_nouns_csv():
    if os.path.exists(NOUNS_LOCAL_PATH) and os.path.getsize(NOUNS_LOCAL_PATH) > 10000000:
        print(f"Using local nouns CSV: {NOUNS_LOCAL_PATH} ({os.path.getsize(NOUNS_LOCAL_PATH)/(1024*1024):.2f} MB)")
        return NOUNS_LOCAL_PATH
    
    print("Cloning gambolputty/german-nouns repository...")
    subprocess.run(["git", "clone", "--depth", "1", "https://github.com/gambolputty/german-nouns", "/tmp/german-nouns"], check=True)
    return NOUNS_LOCAL_PATH

def build_database():
    t0 = time.time()
    csv_file = ensure_nouns_csv()
    
    os.makedirs(os.path.dirname(OUTPUT_DB_PATH), exist_ok=True)
    if os.path.exists(OUTPUT_DB_PATH):
        os.remove(OUTPUT_DB_PATH)

    print(f"Building SQLite database at {OUTPUT_DB_PATH}...")
    conn = sqlite3.connect(OUTPUT_DB_PATH)
    cursor = conn.cursor()

    cursor.execute("PRAGMA page_size = 4096")
    cursor.execute("PRAGMA synchronous = OFF")
    cursor.execute("PRAGMA journal_mode = MEMORY")

    # 1. Create german_nouns table matching GermanNounEntity
    cursor.executescript("""
    CREATE TABLE IF NOT EXISTS german_nouns (
        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        lemma TEXT NOT NULL,
        article TEXT,
        genus TEXT,
        pos TEXT,
        nominativ_singular TEXT,
        nominativ_plural TEXT,
        genitiv_singular TEXT,
        genitiv_plural TEXT,
        dativ_singular TEXT,
        dativ_plural TEXT,
        akkusativ_singular TEXT,
        akkusativ_plural TEXT,
        flexion TEXT
    );

    CREATE INDEX IF NOT EXISTS index_german_nouns_lemma ON german_nouns(lemma);
    CREATE INDEX IF NOT EXISTS index_german_nouns_nominativ_plural ON german_nouns(nominativ_plural);

    CREATE TABLE IF NOT EXISTS words (
        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        german_word TEXT NOT NULL,
        article TEXT,
        plural TEXT,
        pos TEXT NOT NULL,
        arabic_translation TEXT NOT NULL,
        is_separable INTEGER NOT NULL DEFAULT 0,
        separable_prefix TEXT,
        frequency_rank INTEGER,
        source TEXT NOT NULL
    );

    CREATE INDEX IF NOT EXISTS index_words_german_word ON words(german_word);
    CREATE INDEX IF NOT EXISTS index_words_arabic_translation ON words(arabic_translation);
    CREATE INDEX IF NOT EXISTS index_words_pos ON words(pos);
    CREATE INDEX IF NOT EXISTS index_words_plural ON words(plural);

    CREATE TABLE IF NOT EXISTS conjugations (
        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        word_id INTEGER NOT NULL,
        tense TEXT NOT NULL,
        person TEXT NOT NULL,
        form TEXT NOT NULL,
        source TEXT NOT NULL,
        FOREIGN KEY (word_id) REFERENCES words(id) ON UPDATE NO ACTION ON DELETE CASCADE
    );
    CREATE INDEX IF NOT EXISTS index_conjugations_word_id ON conjugations(word_id);
    CREATE INDEX IF NOT EXISTS index_conjugations_form ON conjugations(form);

    CREATE TABLE IF NOT EXISTS examples (
        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        word_id INTEGER NOT NULL,
        german_sentence TEXT NOT NULL,
        arabic_translation TEXT,
        source TEXT NOT NULL,
        FOREIGN KEY (word_id) REFERENCES words(id) ON UPDATE NO ACTION ON DELETE CASCADE
    );
    CREATE INDEX IF NOT EXISTS index_examples_word_id ON examples(word_id);

    CREATE TABLE IF NOT EXISTS history (
        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        word_id INTEGER,
        query TEXT NOT NULL,
        result_type TEXT NOT NULL,
        result_text TEXT NOT NULL,
        is_saved INTEGER NOT NULL,
        searched_at INTEGER NOT NULL,
        FOREIGN KEY (word_id) REFERENCES words(id) ON UPDATE NO ACTION ON DELETE SET NULL
    );
    CREATE INDEX IF NOT EXISTS index_history_searched_at ON history(searched_at);
    CREATE INDEX IF NOT EXISTS index_history_word_id ON history(word_id);

    CREATE TABLE IF NOT EXISTS review_schedules (
        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        word_id INTEGER NOT NULL,
        review_count INTEGER NOT NULL,
        repetition_level INTEGER NOT NULL,
        interval_days INTEGER NOT NULL,
        ease_factor REAL NOT NULL,
        last_reviewed_at INTEGER NOT NULL,
        next_review_at INTEGER NOT NULL,
        FOREIGN KEY (word_id) REFERENCES words(id) ON UPDATE NO ACTION ON DELETE CASCADE
    );
    CREATE UNIQUE INDEX IF NOT EXISTS index_review_schedules_word_id ON review_schedules(word_id);
    CREATE INDEX IF NOT EXISTS index_review_schedules_next_review_at ON review_schedules(next_review_at);

    CREATE TABLE IF NOT EXISTS translation_history (
        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        sourceText TEXT NOT NULL,
        translatedText TEXT NOT NULL,
        sourceLanguage TEXT NOT NULL,
        targetLanguage TEXT NOT NULL,
        timestamp INTEGER NOT NULL,
        isFavorite INTEGER NOT NULL DEFAULT 0
    );

    CREATE TABLE IF NOT EXISTS cheatsheet_items (
        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        phrase TEXT NOT NULL,
        translation TEXT NOT NULL,
        category TEXT NOT NULL,
        languagePair TEXT NOT NULL,
        notes TEXT NOT NULL DEFAULT '',
        createdAt INTEGER NOT NULL
    );
    """)

    # Seed cheat sheet grammar items
    cheatsheet_rules = [
        ("der (Nominativ) -> den (Akkusativ) -> dem (Dativ) -> des (+s/es) (Genitiv)", "أدوات المعرفة للمذكر في الحالات الإعرابية الأربع", "Grammar (Cases)", "de-ar", "حالات إعراب المذكر"),
        ("die (Nominativ) -> die (Akkusativ) -> der (Dativ) -> der (Genitiv)", "أدوات المعرفة للمؤنث في الحالات الإعرابية الأربع", "Grammar (Cases)", "de-ar", "حالات إعراب المؤنث"),
        ("das (Nominativ) -> das (Akkusativ) -> dem (Dativ) -> des (+s/es) (Genitiv)", "أدوات المعرفة للمحايد في الحالات الإعرابية الأربع", "Grammar (Cases)", "de-ar", "حالات إعراب المحايد"),
        ("die (Nominativ) -> die (Akkusativ) -> den (+n) (Dativ) -> der (Genitiv)", "أدوات المعرفة للجمع في الحالات الإعرابية الأربع", "Grammar (Cases)", "de-ar", "حالات إعراب الجمع"),
        ("aus, bei, mit, nach, seit, von, zu, gegenüber", "حروف جر تأخذ الداتيف دائماً (Dativ)", "Prepositions", "de-ar", "حروف جر الـ Dativ الثابتة"),
        ("durch, für, gegen, ohne, um, bis, entlang", "حروف جر تأخذ الأكوزاتيف دائماً (Akkusativ)", "Prepositions", "de-ar", "حروف جر الـ Akkusativ الثابتة"),
        ("an, auf, hinter, in, neben, über, unter, vor, بين", "حروف الجر المشتركة (Wechselpräpositionen): Wohin (Akk) / Wo (Dat)", "Prepositions", "de-ar", "حروف الجر المكانية الثنائية"),
        ("können (الاستطاعة), müssen (الإلزام), dürfen (السماح), wollen (الإرادة), sollen (الواجب), mögen/möchten (الرغبة)", "الأفعال المساعدة (Modalverben) تأتي مع فعل في المصدر بآخر الجملة", "Verbs", "de-ar", "تصريف الأفعال المساعدة"),
        ("weil, dass, wenn, ob, obwohl, da, damit", "روابط الجمل الجانبية (Nebensatz) التي ترسل الفعل المصرف إلى نهاية الجملة تماماً", "Sentence Structure", "de-ar", "تركيب الجمل الجانبية")
    ]
    for phrase, trans, cat, pair, notes in cheatsheet_rules:
        cursor.execute("""
        INSERT INTO cheatsheet_items (phrase, translation, category, languagePair, notes, createdAt)
        VALUES (?, ?, ?, ?, ?, ?)
        """, (phrase, trans, cat, pair, notes, int(time.time() * 1000)))

    # Core German-Arabic Lexicon Dictionary
    CORE_LEXICON = {
        "Haus": ("das", "Häuser", "noun", "بيت / منزل"),
        "Buch": ("das", "Bücher", "noun", "كتاب"),
        "Mann": ("der", "Männer", "noun", "رجل"),
        "Frau": ("die", "Frauen", "noun", "امرأة / سيدة"),
        "Kind": ("das", "Kinder", "noun", "طفل"),
        "Freund": ("der", "Freunde", "noun", "صديق"),
        "Freundin": ("die", "Freundinnen", "noun", "صديقة"),
        "Zeit": ("die", "Zeiten", "noun", "وقت / زمن"),
        "Jahr": ("das", "Jahre", "noun", "سنة / عام"),
        "Tag": ("der", "Tage", "noun", "يوم"),
        "Weg": ("der", "Wege", "noun", "طريق / سبيل"),
        "Welt": ("die", "Welten", "noun", "عالم / دنيا"),
        "Leben": ("das", "Leben", "noun", "حياة"),
        "Hand": ("die", "Hände", "noun", "يد"),
        "Auge": ("das", "Augen", "noun", "عين"),
        "Kopf": ("der", "Köpfe", "noun", "رأس"),
        "Wasser": ("das", "Wässer", "noun", "ماء"),
        "Arbeit": ("die", "Arbeiten", "noun", "عمل / شغل"),
        "Stadt": ("die", "Städte", "noun", "مدينة"),
        "Land": ("das", "Länder", "noun", "بلد / دولة / ريف"),
        "Sprache": ("die", "Sprachen", "noun", "لغة"),
        "Wort": ("das", "Wörter / Worte", "noun", "كلمة"),
        "Schule": ("die", "Schulen", "noun", "مدرسة"),
        "Universität": ("die", "Universitäten", "noun", "جامعة"),
        "Auto": ("das", "Autos", "noun", "سيارة"),
        "Geld": ("das", "Gelder", "noun", "مال / نقود"),
        "Frage": ("die", "Fragen", "noun", "سؤال"),
        "Antwort": ("die", "Antworten", "noun", "جواب / إجابة"),
        "Sonne": ("die", "Sonnen", "noun", "شمس"),
        "Mond": ("der", "Monde", "noun", "قمر"),
        "Stern": ("der", "Sterne", "noun", "نجم"),
        "Tisch": ("der", "Tische", "noun", "طاولة"),
        "Stuhl": ("der", "Stühle", "noun", "كرسي"),
        "Fenster": ("das", "Fenster", "noun", "نافذة / شباك"),
        "Tür": ("die", "Türen", "noun", "باب"),
        "Zimmer": ("das", "Zimmer", "noun", "غرفة"),
        "Wohnung": ("die", "Wohnungen", "noun", "شقة"),
        "Morgen": ("der", "Morgen", "noun", "صباح"),
        "Abend": ("der", "Abende", "noun", "مساء"),
        "Nacht": ("die", "Nächte", "noun", "ليل / ليلة"),
        "Familie": ("die", "Familien", "noun", "عائلة / أسرة"),
        "Vater": ("der", "Väter", "noun", "أب"),
        "Mutter": ("die", "Mütter", "noun", "أم"),
        "Sohn": ("der", "Söhne", "noun", "ابن"),
        "Tochter": ("die", "Töchter", "noun", "ابنة"),
        "Bruder": ("der", "Brüder", "noun", "أخ"),
        "Schwester": ("die", "Schwestern", "noun", "أخت"),
        "Arzt": ("der", "Ärzte", "noun", "طبيب"),
        "Ärztin": ("die", "Ärztinnen", "noun", "طبيبة"),
        "Lehrer": ("der", "Lehrer", "noun", "معلم / مدرس"),
        "Lehrerin": ("die", "Lehrerinnen", "noun", "معلمة / مدرسة"),
        "Schüler": ("der", "Schüler", "noun", "تلميذ"),
        "Student": ("der", "Studenten", "noun", "طالب جامعي"),
        "Problem": ("das", "Probleme", "noun", "مشكلة"),
        "Lösung": ("die", "Lösungen", "noun", "حل"),
        "Grund": ("der", "Gründe", "noun", "سبب / أساس"),
        "Ziel": ("das", "Ziele", "noun", "هدف"),
        "Erfolg": ("der", "Erfolge", "noun", "نجاح"),
        "Gedanke": ("der", "Gedanken", "noun", "فكرة / خاطر"),
        "Gefühl": ("das", "Gefühle", "noun", "شعور / إحساس"),
        "Liebe": ("die", "Lieben", "noun", "حب / محبة"),
        "Hoffnung": ("die", "Hoffnungen", "noun", "أمل / رجاء"),
        "Angst": ("die", "Ängste", "noun", "خوف / قلق"),
        "Frieden": ("der", "Frieden", "noun", "سلام"),
        "Krieg": ("der", "Kriege", "noun", "حرب"),
        "Recht": ("das", "Rechte", "noun", "حق / قانون"),
        "Gesetz": ("das", "Gesetze", "noun", "قانون / تشريع"),
        "Staat": ("der", "Staaten", "noun", "دولة"),
        "Regierung": ("die", "Regierungen", "noun", "حكومة"),
        "Gesellschaft": ("die", "Gesellschaften", "noun", "مجتمع / شركة"),
        "Wirtschaft": ("die", "Wirtschaften", "noun", "اقتصاد"),
        "Markt": ("der", "Märkte", "noun", "سوق"),
        "Preis": ("der", "Preise", "noun", "سعر / جائزة"),
        "Firma": ("die", "Firmen", "noun", "شركة"),
        "Geschichte": ("die", "Geschichten", "noun", "تاريخ / قصة"),
        "Kultur": ("die", "Kulturen", "noun", "ثقافة"),
        "Kunst": ("die", "Künste", "noun", "فن"),
        "Musik": ("die", "Musiken", "noun", "موسيقى"),
        "Bild": ("das", "Bilder", "noun", "صورة / لوحة"),
        "Buchstabe": ("der", "Buchstaben", "noun", "حرف هجائي"),
        "Satz": ("der", "Sätze", "noun", "جملة"),
        "Brief": ("der", "Briefe", "noun", "رسالة / خطاب"),
        "Telefon": ("das", "Telefone", "noun", "هاتف"),
        "Computer": ("der", "Computer", "noun", "حاسوب / كمبيوتر"),
        "Internet": ("das", "Internet", "noun", "إنترنت"),
        "Nachricht": ("die", "Nachrichten", "noun", "خبر / رسالة"),
        "Zeitung": ("die", "Zeitungen", "noun", "جريدة / صحيفة"),
        "Reise": ("die", "Reisen", "noun", "سفر / رحلة"),
        "Zug": ("der", "Züge", "noun", "قطار"),
        "Flugzeug": ("das", "Flugzeuge", "noun", "طائرة"),
        "Schiff": ("das", "Schiffe", "noun", "سفينة"),
        "Bahnhof": ("der", "Bahnhöfe", "noun", "محطة قطار"),
        "Flughafen": ("der", "Flughäfen", "noun", "مطار"),
        "Hotel": ("das", "Hotels", "noun", "فندق"),
        "Restaurant": ("das", "Restaurants", "noun", "مطعم"),
        "Essen": ("das", "Essen", "noun", "طعام / أكل"),
        "Trinken": ("das", "Trinken", "noun", "شراب / مشروب"),
        "Brot": ("das", "Brote", "noun", "خبز"),
        "Milch": ("die", "Milch", "noun", "حليب / لبن"),
        "Kaffee": ("der", "Kaffees", "noun", "قهوة"),
        "Tee": ("der", "Tees", "noun", "شاي"),
        "Fleisch": ("das", "Fleisch", "noun", "لحم"),
        "Fisch": ("der", "Fische", "noun", "سمك"),
        "Obst": ("das", "Obst", "noun", "فاكهة"),
        "Gemüse": ("das", "Gemüse", "noun", "خضروات"),
        "Apfel": ("der", "Äpfel", "noun", "تفاح / تفاحة"),
        "Käse": ("der", "Käse", "noun", "جبن"),
        "Zucker": ("der", "Zucker", "noun", "سكر"),
        "Salz": ("das", "Salze", "noun", "ملح"),
        "Krankheit": ("die", "Krankheiten", "noun", "مرض"),
        "Gesundheit": ("die", "Gesundheiten", "noun", "صحة"),
        "Krankenhaus": ("das", "Krankenhäuser", "noun", "مستشفى"),
        "Medikament": ("das", "Medikamente", "noun", "دواء / علاج"),
        "Körper": ("der", "Körper", "noun", "جسم / جسد"),
        "Herz": ("das", "Herzen", "noun", "قلب"),
        "Mund": ("der", "Münder", "noun", "فم"),
        "Nase": ("die", "Nasen", "noun", "أنف"),
        "Ohr": ("das", "Ohren", "noun", "أذن"),
        "Bein": ("das", "Beine", "noun", "ساق / رجل"),
        "Fuß": ("der", "Füße", "noun", "قدم"),
        "Kleidung": ("die", "Kleidungen", "noun", "ملابس / ثياب"),
        "Hemd": ("das", "Hemden", "noun", "قميص"),
        "Hose": ("die", "Hosen", "noun", "بنطال / سروال"),
        "Schuh": ("der", "Schuhe", "noun", "حذاء"),
        "Wetter": ("das", "Wetter", "noun", "طقس / جو"),
        "Regen": ("der", "Regen", "noun", "مطر"),
        "Schnee": ("der", "Schnee", "noun", "ثلج"),
        "Wind": ("der", "Winde", "noun", "ريح / رياح"),
        "Natur": ("die", "Naturen", "noun", "طبيعة"),
        "Baum": ("der", "Bäume", "noun", "شجرة"),
        "Blume": ("die", "Blumen", "noun", "زهرة / وردة"),
        "Tier": ("das", "Tiere", "noun", "حيوان"),
        "Hund": ("der", "Hunde", "noun", "كلب"),
        "Katze": ("die", "Katzen", "noun", "قطة"),
        "Vogel": ("der", "Vögel", "noun", "طائر / عصفور"),
        "Orange": ("die", "Orangen", "noun", "برتقال / برتقالة"),
        "Physik": ("die", "-", "noun", "علم الفيزياء / فيزياء"),
        # Verbs & Other
        "sein": (None, None, "verb", "يكون (فعل الكينونة)"),
        "haben": (None, None, "verb", "يملك / عنده / لديه"),
        "werden": (None, None, "verb", "يصبح / صيرورة"),
        "können": (None, None, "verb", "يستطيع / يقدر"),
        "müssen": (None, None, "verb", "يجب / يلزم"),
        "sollen": (None, None, "verb", "ينبغي / يُفترض"),
        "wollen": (None, None, "verb", "يريد / يرغب"),
        "dürfen": (None, None, "verb", "يُسمح له / يجوز"),
        "mögen": (None, None, "verb", "يحب / يود"),
        "machen": (None, None, "verb", "يفعل / يصنع / يعمل"),
        "gehen": (None, None, "verb", "يذهب / يمشي"),
        "kommen": (None, None, "verb", "يأتي / يحضر"),
        "sehen": (None, None, "verb", "يرى / يشاهد"),
        "hören": (None, None, "verb", "يسمع / يستمع"),
        "sprechen": (None, None, "verb", "يتكلم / يتحدث"),
        "sagen": (None, None, "verb", "يقول"),
        "geben": (None, None, "verb", "يعطي / يمنح"),
        "nehmen": (None, None, "verb", "يأخذ"),
        "finden": (None, None, "verb", "يجد / يعتبر"),
        "wissen": (None, None, "verb", "يعلم / يعرف"),
        "lernen": (None, None, "verb", "يتعلم"),
        "arbeiten": (None, None, "verb", "يعمل / يشتغل"),
        "wohnen": (None, None, "verb", "يسكن / يقيم"),
        "schreiben": (None, None, "verb", "يكتب"),
        "lesen": (None, None, "verb", "يقرأ"),
        "verstehen": (None, None, "verb", "يفهم / يستوعب"),
        "fahren": (None, None, "verb", "يسافر / يقود مركبة"),
        "essen": (None, None, "verb", "يأكل"),
        "trinken": (None, None, "verb", "يشرب"),
        "kaufen": (None, None, "verb", "يشتري"),
        "helfen": (None, None, "verb", "يساعد / يعين"),
        "brauchen": (None, None, "verb", "يحتاج"),
        "suchen": (None, None, "verb", "يبحث عن / يفتش"),
        "lieben": (None, None, "verb", "يحب / يعشق"),
        "aufstehen": (None, None, "verb", "يستيقظ / ينهض (فعل منفصل)"),
        "anrufen": (None, None, "verb", "يتصل هاتفياً (فعل منفصل)"),
        "einkaufen": (None, None, "verb", "يتسوق (فعل منفصل)")
    }

    # Parse and insert all nouns from gambolputty/german-nouns
    print("Reading and parsing nouns.csv...")
    german_nouns_rows = []
    seen_lemmas = set()

    with open(csv_file, "r", encoding="utf-8") as f:
        reader = csv.DictReader(f)
        for row in reader:
            lemma = (row.get("lemma") or "").strip()
            if not lemma or len(lemma) < 1 or lemma.startswith("-"):
                continue

            genus = row.get("genus") or row.get("genus 1") or ""
            genus = genus.strip().lower()
            if genus == "m":
                article = "der"
            elif genus == "f":
                article = "die"
            elif genus == "n":
                article = "das"
            else:
                article = None
                genus = None

            pos = (row.get("pos") or "Substantiv").strip()
            nom_sg = (row.get("nominativ singular") or row.get("nominativ singular 1") or "").strip() or None
            nom_pl = (row.get("nominativ plural") or row.get("nominativ plural 1") or row.get("nominativ plural*") or "").strip() or None
            gen_sg = (row.get("genitiv singular") or row.get("genitiv singular 1") or "").strip() or None
            gen_pl = (row.get("genitiv plural") or row.get("genitiv plural 1") or "").strip() or None
            dat_sg = (row.get("dativ singular") or row.get("dativ singular 1") or "").strip() or None
            dat_pl = (row.get("dativ plural") or row.get("dativ plural 1") or "").strip() or None
            akk_sg = (row.get("akkusativ singular") or row.get("akkusativ singular 1") or "").strip() or None
            akk_pl = (row.get("akkusativ plural") or row.get("akkusativ plural 1") or "").strip() or None

            # Summary flexion string
            flexion_parts = []
            if nom_sg: flexion_parts.append(f"Nom.Sg: {nom_sg}")
            if nom_pl: flexion_parts.append(f"Nom.Pl: {nom_pl}")
            if gen_sg: flexion_parts.append(f"Gen.Sg: {gen_sg}")
            if dat_sg: flexion_parts.append(f"Dat.Sg: {dat_sg}")
            if akk_sg: flexion_parts.append(f"Akk.Sg: {akk_sg}")
            flexion = ", ".join(flexion_parts) if flexion_parts else None

            german_nouns_rows.append((
                lemma, article, genus, pos,
                nom_sg, nom_pl, gen_sg, gen_pl,
                dat_sg, dat_pl, akk_sg, akk_pl,
                flexion
            ))
            seen_lemmas.add(lemma)

    print(f"Parsed {len(german_nouns_rows)} German nouns. Inserting into `german_nouns`...")
    cursor.executemany("""
    INSERT INTO german_nouns (
        lemma, article, genus, pos,
        nominativ_singular, nominativ_plural, genitiv_singular, genitiv_plural,
        dativ_singular, dativ_plural, akkusativ_singular, akkusativ_plural,
        flexion
    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """, german_nouns_rows)

    # Insert into words table (combining core lexicon + all German nouns)
    print("Populating `words` table with comprehensive vocabulary...")
    rank = 1
    # 1. Insert Core lexicon first
    for g_word, (art, plur, pos, ar_trans) in CORE_LEXICON.items():
        is_sep = 1 if (pos == "verb" and g_word in ("aufstehen", "anrufen", "einkaufen")) else 0
        sep_pref = g_word[:3] if is_sep else None
        cursor.execute("""
        INSERT INTO words (german_word, article, plural, pos, arabic_translation, is_separable, separable_prefix, frequency_rank, source)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """, (g_word, art, plur, pos, ar_trans, is_sep, sep_pref, rank, "Core Lexicon"))
        w_id = cursor.lastrowid
        rank += 1
        cursor.execute("""
        INSERT INTO review_schedules (word_id, review_count, repetition_level, interval_days, ease_factor, last_reviewed_at, next_review_at)
        VALUES (?, 0, 0, 1, 2.5, 0, ?)
        """, (w_id, int(time.time() * 1000)))

    # 2. Insert other nouns into `words` table
    words_to_insert = []
    for row in german_nouns_rows:
        lemma, article, genus, pos, nom_sg, nom_pl, gen_sg, gen_pl, dat_sg, dat_pl, akk_sg, akk_pl, flexion = row
        if lemma in CORE_LEXICON:
            continue
        words_to_insert.append((
            lemma, article, nom_pl, "noun", "", 0, None, rank, "gambolputty/german-nouns"
        ))
        rank += 1

    cursor.executemany("""
    INSERT INTO words (german_word, article, plural, pos, arabic_translation, is_separable, separable_prefix, frequency_rank, source)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
    """, words_to_insert)

    conn.commit()

    print("Optimizing SQLite database...")
    cursor.execute("PRAGMA optimize")
    cursor.execute("VACUUM")
    conn.commit()

    cursor.execute("SELECT COUNT(*) FROM german_nouns")
    total_nouns = cursor.fetchone()[0]

    cursor.execute("SELECT COUNT(*) FROM words")
    total_words = cursor.fetchone()[0]

    cursor.execute("SELECT * FROM german_nouns WHERE lemma = 'Haus'")
    haus_noun = cursor.fetchone()

    cursor.execute("SELECT * FROM german_nouns WHERE lemma = 'Tisch'")
    tisch_noun = cursor.fetchone()

    cursor.execute("SELECT * FROM german_nouns WHERE lemma = 'Sonne'")
    sonne_noun = cursor.fetchone()

    conn.close()

    elapsed = time.time() - t0
    db_size_mb = os.path.getsize(OUTPUT_DB_PATH) / (1024 * 1024)
    print("\n" + "="*60)
    print(f"DATABASE GENERATION COMPLETE in {elapsed:.2f}s")
    print(f"Total German Nouns in `german_nouns`: {total_nouns:,}")
    print(f"Total Entries in `words`:             {total_words:,}")
    print(f"Database File Size:                  {db_size_mb:.2f} MB")
    print(f"Haus row:  {haus_noun}")
    print(f"Tisch row: {tisch_noun}")
    print(f"Sonne row: {sonne_noun}")
    print("="*60 + "\n")

if __name__ == "__main__":
    build_database()
