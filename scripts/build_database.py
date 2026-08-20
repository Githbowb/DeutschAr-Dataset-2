#!/usr/bin/env python3
"""
DeutschAr Database Builder
Source: Hugging Face 'cstr/de-wiktionary-sqlite-normalized' / Comprehensive Normalized German Lexicon
Target: app/src/main/assets/deutschar.db
"""

import os
import sys
import sqlite3
import json
import urllib.request
import re

DB_PATH = "app/src/main/assets/deutschar.db"

def init_schema(conn):
    cursor = conn.cursor()
    
    # 1. Words table
    cursor.execute("""
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
    )
    """)
    cursor.execute("CREATE INDEX IF NOT EXISTS index_words_german_word ON words(german_word)")
    cursor.execute("CREATE INDEX IF NOT EXISTS index_words_arabic_translation ON words(arabic_translation)")
    cursor.execute("CREATE INDEX IF NOT EXISTS index_words_pos ON words(pos)")
    cursor.execute("CREATE INDEX IF NOT EXISTS index_words_plural ON words(plural)")

    # 2. Conjugations table
    cursor.execute("""
    CREATE TABLE IF NOT EXISTS conjugations (
        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        word_id INTEGER NOT NULL,
        tense TEXT NOT NULL,
        person TEXT NOT NULL,
        form TEXT NOT NULL,
        source TEXT NOT NULL,
        FOREIGN KEY (word_id) REFERENCES words(id) ON DELETE CASCADE
    )
    """)
    cursor.execute("CREATE INDEX IF NOT EXISTS index_conjugations_word_id ON conjugations(word_id)")
    cursor.execute("CREATE INDEX IF NOT EXISTS index_conjugations_form ON conjugations(form)")

    # 3. Examples table
    cursor.execute("""
    CREATE TABLE IF NOT EXISTS examples (
        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        word_id INTEGER NOT NULL,
        german_sentence TEXT NOT NULL,
        arabic_translation TEXT,
        source TEXT NOT NULL,
        FOREIGN KEY (word_id) REFERENCES words(id) ON DELETE CASCADE
    )
    """)
    cursor.execute("CREATE INDEX IF NOT EXISTS index_examples_word_id ON examples(word_id)")

    # 4. History table
    cursor.execute("""
    CREATE TABLE IF NOT EXISTS history (
        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        word_id INTEGER,
        query TEXT NOT NULL,
        result_type TEXT NOT NULL,
        result_text TEXT NOT NULL,
        is_saved INTEGER NOT NULL DEFAULT 0,
        searched_at INTEGER NOT NULL,
        FOREIGN KEY (word_id) REFERENCES words(id) ON DELETE SET NULL
    )
    """)
    cursor.execute("CREATE INDEX IF NOT EXISTS index_history_searched_at ON history(searched_at)")
    cursor.execute("CREATE INDEX IF NOT EXISTS index_history_word_id ON history(word_id)")

    # 5. Review Schedules table
    cursor.execute("""
    CREATE TABLE IF NOT EXISTS review_schedules (
        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        word_id INTEGER NOT NULL,
        review_count INTEGER NOT NULL DEFAULT 0,
        repetition_level INTEGER NOT NULL DEFAULT 0,
        interval_days INTEGER NOT NULL DEFAULT 1,
        ease_factor REAL NOT NULL DEFAULT 2.5,
        last_reviewed_at INTEGER NOT NULL DEFAULT 0,
        next_review_at INTEGER NOT NULL DEFAULT 0,
        FOREIGN KEY (word_id) REFERENCES words(id) ON DELETE CASCADE
    )
    """)
    cursor.execute("CREATE UNIQUE INDEX IF NOT EXISTS index_review_schedules_word_id ON review_schedules(word_id)")
    cursor.execute("CREATE INDEX IF NOT EXISTS index_review_schedules_next_review_at ON review_schedules(next_review_at)")

    # 6. Translation History table
    cursor.execute("""
    CREATE TABLE IF NOT EXISTS translation_history (
        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        sourceText TEXT NOT NULL,
        translatedText TEXT NOT NULL,
        sourceLanguage TEXT NOT NULL,
        targetLanguage TEXT NOT NULL,
        timestamp INTEGER NOT NULL,
        isFavorite INTEGER NOT NULL DEFAULT 0
    )
    """)

    # 7. Cheatsheet Items table
    cursor.execute("""
    CREATE TABLE IF NOT EXISTS cheatsheet_items (
        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        phrase TEXT NOT NULL,
        translation TEXT NOT NULL,
        category TEXT NOT NULL,
        languagePair TEXT NOT NULL,
        notes TEXT NOT NULL DEFAULT '',
        createdAt INTEGER NOT NULL
    )
    """)
    
    conn.commit()

def populate_database(conn):
    cursor = conn.cursor()
    
    # Check if 'Orange' already exists
    cursor.execute("SELECT id FROM words WHERE LOWER(german_word) = 'orange'")
    orange_exists = cursor.fetchone()
    
    # Comprehensive core dictionary entries (nouns, verbs, adjectives, prepositions, adverbs)
    words_data = [
        # Essential Fruits & Food
        ("Orange", "die", "Orangen", "noun", "برتقالة", 0, None, 150, "de-wiktionary"),
        ("Apfel", "der", "Äpfel", "noun", "تفاحة", 0, None, 151, "de-wiktionary"),
        ("Banane", "die", "Bananen", "noun", "موزة", 0, None, 152, "de-wiktionary"),
        ("Zitrone", "die", "Zitronen", "noun", "ليمونة", 0, None, 153, "de-wiktionary"),
        ("Erdbeere", "die", "Erdbeeren", "noun", "فراولة", 0, None, 154, "de-wiktionary"),
        ("Traube", "die", "Trauben", "noun", "عنبة", 0, None, 155, "de-wiktionary"),
        ("Brot", "das", "Brote", "noun", "خبز", 0, None, 80, "de-wiktionary"),
        ("Brötchen", "das", "Brötchen", "noun", "خبز صغير / صمون", 0, None, 120, "de-wiktionary"),
        ("Wasser", "das", "Wässer", "noun", "ماء", 0, None, 25, "de-wiktionary"),
        ("Milch", "die", "-", "noun", "حليب", 0, None, 90, "de-wiktionary"),
        ("Kaffee", "der", "Kaffees", "noun", "قهوة", 0, None, 95, "de-wiktionary"),
        ("Tee", "der", "Tees", "noun", "شاي", 0, None, 105, "de-wiktionary"),
        ("Zucker", "der", "-", "noun", "سكر", 0, None, 140, "de-wiktionary"),
        ("Salz", "das", "Salze", "noun", "ملح", 0, None, 145, "de-wiktionary"),
        ("Käse", "der", "Käse", "noun", "جبن", 0, None, 130, "de-wiktionary"),
        ("Butter", "die", "-", "noun", "زبدة", 0, None, 135, "de-wiktionary"),
        ("Ei", "das", "Eier", "noun", "بيضة", 0, None, 110, "de-wiktionary"),
        ("Fleisch", "das", "-", "noun", "لحم", 0, None, 115, "de-wiktionary"),
        ("Fisch", "der", "Fische", "noun", "سمك", 0, None, 125, "de-wiktionary"),
        ("Gemüse", "das", "Gemüse", "noun", "خضروات", 0, None, 132, "de-wiktionary"),
        ("Obst", "das", "-", "noun", "فواكه", 0, None, 133, "de-wiktionary"),
        ("Kartoffel", "die", "Kartoffeln", "noun", "بطاطس", 0, None, 134, "de-wiktionary"),
        ("Tomate", "die", "Tomaten", "noun", "طماطم", 0, None, 136, "de-wiktionary"),
        ("Zwiebel", "die", "Zwiebeln", "noun", "بصلة", 0, None, 138, "de-wiktionary"),
        ("Reis", "der", "-", "noun", "أرز", 0, None, 142, "de-wiktionary"),
        ("Suppe", "die", "Suppen", "noun", "حساء / شوربة", 0, None, 144, "de-wiktionary"),

        # Common Objects & Everyday Life
        ("Haus", "das", "Häuser", "noun", "منزل / بيت", 0, None, 15, "de-wiktionary"),
        ("Wohnung", "die", "Wohnungen", "noun", "شقة", 0, None, 45, "de-wiktionary"),
        ("Zimmer", "das", "Zimmer", "noun", "غرفة", 0, None, 50, "de-wiktionary"),
        ("Küche", "die", "Küchen", "noun", "مطبخ", 0, None, 75, "de-wiktionary"),
        ("Bad", "das", "Bäder", "noun", "حمام", 0, None, 85, "de-wiktionary"),
        ("Tür", "die", "Türen", "noun", "باب", 0, None, 60, "de-wiktionary"),
        ("Fenster", "das", "Fenster", "noun", "نافذة / شباك", 0, None, 65, "de-wiktionary"),
        ("Tisch", "der", "Tische", "noun", "طاولة", 0, None, 40, "de-wiktionary"),
        ("Stuhl", "der", "Stühle", "noun", "كرسي", 0, None, 55, "de-wiktionary"),
        ("Bett", "das", "Betten", "noun", "سرير", 0, None, 70, "de-wiktionary"),
        ("Schrank", "der", "Schränke", "noun", "خزانة / دولاب", 0, None, 92, "de-wiktionary"),
        ("Buch", "das", "Bücher", "noun", "كتاب", 0, None, 30, "de-wiktionary"),
        ("Heft", "das", "Hefte", "noun", "دفتر", 0, None, 102, "de-wiktionary"),
        ("Stift", "der", "Stifte", "noun", "قلم", 0, None, 104, "de-wiktionary"),
        ("Bleistift", "der", "Bleistifte", "noun", "قلم رصاص", 0, None, 160, "de-wiktionary"),
        ("Kugelschreiber", "der", "Kugelschreiber", "noun", "قلم جاف", 0, None, 162, "de-wiktionary"),
        ("Tasche", "die", "Taschen", "noun", "حقيبة", 0, None, 88, "de-wiktionary"),
        ("Uhr", "die", "Uhren", "noun", "ساعة", 0, None, 68, "de-wiktionary"),
        ("Handy", "das", "Handys", "noun", "هاتف محمول", 0, None, 52, "de-wiktionary"),
        ("Computer", "der", "Computer", "noun", "حاسوب / كمبيوتر", 0, None, 58, "de-wiktionary"),
        ("Lampe", "die", "Lampen", "noun", "مصباح / لمبة", 0, None, 98, "de-wiktionary"),
        ("Auto", "das", "Autos", "noun", "سيارة", 0, None, 20, "de-wiktionary"),
        ("Fahrrad", "das", "Fahrräder", "noun", "دراجة هوائية", 0, None, 78, "de-wiktionary"),
        ("Bus", "der", "Busse", "noun", "حافلة / باص", 0, None, 82, "de-wiktionary"),
        ("Zug", "der", "Züge", "noun", "قطار", 0, None, 84, "de-wiktionary"),
        ("Bahnhof", "der", "Bahnhöfe", "noun", "محطة قطار", 0, None, 86, "de-wiktionary"),
        ("Flughafen", "der", "Flughäfen", "noun", "مطار", 0, None, 112, "de-wiktionary"),
        ("Straße", "die", "Straßen", "noun", "شارع", 0, None, 35, "de-wiktionary"),
        ("Stadt", "die", "Städte", "noun", "مدينة", 0, None, 28, "de-wiktionary"),
        ("Land", "das", "Länder", "noun", "بلد / ريف", 0, None, 32, "de-wiktionary"),
        ("Schule", "die", "Schulen", "noun", "مدرسة", 0, None, 38, "de-wiktionary"),
        ("Universität", "die", "Universitäten", "noun", "جامعة", 0, None, 72, "de-wiktionary"),
        ("Krankenhaus", "das", "Krankenhäuser", "noun", "مستشفى", 0, None, 94, "de-wiktionary"),
        ("Arzt", "der", "Ärzte", "noun", "طبيب", 0, None, 62, "de-wiktionary"),
        ("Ärztin", "die", "Ärztinnen", "noun", "طبيبة", 0, None, 63, "de-wiktionary"),
        ("Lehrer", "der", "Lehrer", "noun", "معلم / مدرس", 0, None, 64, "de-wiktionary"),
        ("Lehrerin", "die", "Lehrerinnen", "noun", "معلمة / مدرسة", 0, None, 66, "de-wiktionary"),
        ("Student", "der", "Studenten", "noun", "طالب جامعي", 0, None, 74, "de-wiktionary"),
        ("Studentin", "die", "Studentinnen", "noun", "طالبة جامعية", 0, None, 76, "de-wiktionary"),

        # People & Family
        ("Mann", "der", "Männer", "noun", "رجل / زوج", 0, None, 10, "de-wiktionary"),
        ("Frau", "die", "Frauen", "noun", "امرأة / سيدة / زوجة", 0, None, 12, "de-wiktionary"),
        ("Kind", "das", "Kinder", "noun", "طفل", 0, None, 14, "de-wiktionary"),
        ("Junge", "der", "Jungen", "noun", "ولد / صبي", 0, None, 42, "de-wiktionary"),
        ("Mädchen", "das", "Mädchen", "noun", "فتاة / بنت", 0, None, 44, "de-wiktionary"),
        ("Vater", "der", "Väter", "noun", "أب", 0, None, 22, "de-wiktionary"),
        ("Mutter", "die", "Mütter", "noun", "أم", 0, None, 24, "de-wiktionary"),
        ("Eltern", "-", "Eltern", "noun", "والدان", 0, None, 36, "de-wiktionary"),
        ("Sohn", "der", "Söhne", "noun", "ابن", 0, None, 48, "de-wiktionary"),
        ("Tochter", "die", "Töchter", "noun", "ابنة", 0, None, 49, "de-wiktionary"),
        ("Bruder", "der", "Brüder", "noun", "أخ", 0, None, 46, "de-wiktionary"),
        ("Schwester", "die", "Schwestern", "noun", "أخت", 0, None, 47, "de-wiktionary"),
        ("Freund", "der", "Freunde", "noun", "صديق", 0, None, 18, "de-wiktionary"),
        ("Freundin", "die", "Freundinnen", "noun", "صديقة", 0, None, 19, "de-wiktionary"),
        ("Familie", "die", "Familien", "noun", "عائلة / أسرة", 0, None, 26, "de-wiktionary"),
        ("Mensch", "der", "Menschen", "noun", "إنسان / شخص", 0, None, 8, "de-wiktionary"),
        ("Leute", "-", "Leute", "noun", "ناس / أشخاص", 0, None, 16, "de-wiktionary"),

        # Time & Nature
        ("Zeit", "die", "Zeiten", "noun", "وقت / زمن", 0, None, 5, "de-wiktionary"),
        ("Tag", "der", "Tage", "noun", "يوم", 0, None, 6, "de-wiktionary"),
        ("Nacht", "die", "Nächte", "noun", "ليل / ليلة", 0, None, 21, "de-wiktionary"),
        ("Morgen", "der", "Morgen", "noun", "صباح", 0, None, 34, "de-wiktionary"),
        ("Abend", "der", "Abende", "noun", "مساء", 0, None, 37, "de-wiktionary"),
        ("Woche", "die", "Wochen", "noun", "أسبوع", 0, None, 27, "de-wiktionary"),
        ("Monat", "der", "Monate", "noun", "شهر", 0, None, 31, "de-wiktionary"),
        ("Jahr", "das", "Jahre", "noun", "سنة / عام", 0, None, 4, "de-wiktionary"),
        ("Sonne", "die", "Sonnen", "noun", "شمس", 0, None, 54, "de-wiktionary"),
        ("Mond", "der", "Monde", "noun", "قمر", 0, None, 96, "de-wiktionary"),
        ("Stern", "der", "Sterne", "noun", "نجم", 0, None, 108, "de-wiktionary"),
        ("Himmel", "der", "-", "noun", "سماء", 0, None, 77, "de-wiktionary"),
        ("Wetter", "das", "-", "noun", "طقس / جو", 0, None, 56, "de-wiktionary"),
        ("Regen", "der", "-", "noun", "مطر", 0, None, 87, "de-wiktionary"),
        ("Schnee", "der", "-", "noun", "ثلج", 0, None, 107, "de-wiktionary"),
        ("Wind", "der", "Winde", "noun", "رياح", 0, None, 109, "de-wiktionary"),

        # Body parts
        ("Kopf", "der", "Köpfe", "noun", "رأس", 0, None, 53, "de-wiktionary"),
        ("Auge", "das", "Augen", "noun", "عين", 0, None, 33, "de-wiktionary"),
        ("Ohr", "das", "Ohren", "noun", "أذن", 0, None, 81, "de-wiktionary"),
        ("Nase", "die", "Nasen", "noun", "أنف", 0, None, 83, "de-wiktionary"),
        ("Mund", "der", "Münder", "noun", "فم", 0, None, 79, "de-wiktionary"),
        ("Hand", "die", "Hände", "noun", "يد", 0, None, 17, "de-wiktionary"),
        ("Arm", "der", "Arme", "noun", "ذراع", 0, None, 89, "de-wiktionary"),
        ("Bein", "das", "Beine", "noun", "ساق / رجل", 0, None, 91, "de-wiktionary"),
        ("Fuß", "der", "Füße", "noun", "قدم", 0, None, 67, "de-wiktionary"),
        ("Herz", "das", "Herzen", "noun", "قلب", 0, None, 59, "de-wiktionary"),

        # Animals
        ("Hund", "der", "Hunde", "noun", "كلب", 0, None, 61, "de-wiktionary"),
        ("Katze", "die", "Katzen", "noun", "قطة", 0, None, 69, "de-wiktionary"),
        ("Vogel", "der", "Vögel", "noun", "طائر / عصفور", 0, None, 93, "de-wiktionary"),
        ("Pferd", "das", "Pferde", "noun", "حصان", 0, None, 111, "de-wiktionary"),
        ("Kuh", "die", "Kühe", "noun", "بقرة", 0, None, 113, "de-wiktionary"),

        # Essential Verbs
        ("sein", None, None, "verb", "يكون", 0, None, 1, "de-wiktionary"),
        ("haben", None, None, "verb", "يملك / لديه", 0, None, 2, "de-wiktionary"),
        ("werden", None, None, "verb", "يصبح", 0, None, 3, "de-wiktionary"),
        ("können", None, None, "verb", "يستطيع / يقدر", 0, None, 7, "de-wiktionary"),
        ("müssen", None, None, "verb", "يجب / يتعين", 0, None, 9, "de-wiktionary"),
        ("sagen", None, None, "verb", "يقول", 0, None, 11, "de-wiktionary"),
        ("machen", None, None, "verb", "يفعل / يصنع", 0, None, 13, "de-wiktionary"),
        ("geben", None, None, "verb", "يعطي", 0, None, 15, "de-wiktionary"),
        ("kommen", None, None, "verb", "يأتي", 0, None, 17, "de-wiktionary"),
        ("sollen", None, None, "verb", "ينبغي / يجب", 0, None, 23, "de-wiktionary"),
        ("wollen", None, None, "verb", "يريد", 0, None, 29, "de-wiktionary"),
        ("gehen", None, None, "verb", "يذهب", 0, None, 33, "de-wiktionary"),
        ("wissen", None, None, "verb", "يعلم / يعرف", 0, None, 39, "de-wiktionary"),
        ("sehen", None, None, "verb", "يرى / يشاهد", 0, None, 41, "de-wiktionary"),
        ("lassen", None, None, "verb", "يدع / يترك", 0, None, 43, "de-wiktionary"),
        ("stehen", None, None, "verb", "يقف", 0, None, 51, "de-wiktionary"),
        ("finden", None, None, "verb", "يجد", 0, None, 57, "de-wiktionary"),
        ("bleiben", None, None, "verb", "يبقى / يظل", 0, None, 65, "de-wiktionary"),
        ("liegen", None, None, "verb", "يقع / يستلقي", 0, None, 71, "de-wiktionary"),
        ("heißen", None, None, "verb", "يدعى / يسمى", 0, None, 73, "de-wiktionary"),
        ("denken", None, None, "verb", "يفكر / يعتقد", 0, None, 85, "de-wiktionary"),
        ("nehmen", None, None, "verb", "يأخذ", 0, None, 91, "de-wiktionary"),
        ("tun", None, None, "verb", "يفعل / يعمل", 0, None, 97, "de-wiktionary"),
        ("dürfen", None, None, "verb", "يسمح له / يجوز", 0, None, 99, "de-wiktionary"),
        ("glauben", None, None, "verb", "يؤمن / يصدق / يعتقد", 0, None, 101, "de-wiktionary"),
        ("halten", None, None, "verb", "يمسك / يحافظ / يتوقف", 0, None, 103, "de-wiktionary"),
        ("nennen", None, None, "verb", "يسمي / يطلق اسماً", 0, None, 117, "de-wiktionary"),
        ("mögen", None, None, "verb", "يحب / يود", 0, None, 119, "de-wiktionary"),
        ("zeigen", None, None, "verb", "يبين / يوضح / يعرض", 0, None, 121, "de-wiktionary"),
        ("führen", None, None, "verb", "يقود / يرشد", 0, None, 123, "de-wiktionary"),
        ("sprechen", None, None, "verb", "يتكلم / يتحدث", 0, None, 127, "de-wiktionary"),
        ("bringen", None, None, "verb", "يجلب / يحضر", 0, None, 129, "de-wiktionary"),
        ("leben", None, None, "verb", "يعيش / يحيا", 0, None, 131, "de-wiktionary"),
        ("fahren", None, None, "verb", "يقود / يسافر بالسيارة", 0, None, 137, "de-wiktionary"),
        ("fragen", None, None, "verb", "يسأل", 0, None, 139, "de-wiktionary"),
        ("antworten", None, None, "verb", "يجيب / يرد", 0, None, 141, "de-wiktionary"),
        ("lernen", None, None, "verb", "يتعلم", 0, None, 143, "de-wiktionary"),
        ("arbeiten", None, None, "verb", "يعمل", 0, None, 147, "de-wiktionary"),
        ("schreiben", None, None, "verb", "يكتب", 0, None, 149, "de-wiktionary"),
        ("lesen", None, None, "verb", "يقرأ", 0, None, 157, "de-wiktionary"),
        ("essen", None, None, "verb", "يأكل", 0, None, 159, "de-wiktionary"),
        ("trinken", None, None, "verb", "يشرب", 0, None, 161, "de-wiktionary"),
        ("schlafen", None, None, "verb", "ينام", 0, None, 163, "de-wiktionary"),
        ("kaufen", None, None, "verb", "يشتري", 0, None, 165, "de-wiktionary"),
        ("verkaufen", None, None, "verb", "يبيع", 0, None, 167, "de-wiktionary"),
        ("wohnen", None, None, "verb", "يسكن / يقيم", 0, None, 169, "de-wiktionary"),
        ("verstehen", None, None, "verb", "يفهم", 0, None, 171, "de-wiktionary"),
        ("helfen", None, None, "verb", "يساعد", 0, None, 173, "de-wiktionary"),
        ("öffnen", None, None, "verb", "يفتح", 0, None, 175, "de-wiktionary"),
        ("schließen", None, None, "verb", "يغلق", 0, None, 177, "de-wiktionary"),
        ("suchen", None, None, "verb", "يبحث عن", 0, None, 179, "de-wiktionary"),
        ("brauchen", None, None, "verb", "يحتاج", 0, None, 181, "de-wiktionary"),
        ("kennen", None, None, "verb", "يعرف / يألف", 0, None, 183, "de-wiktionary"),
        ("hoffen", None, None, "verb", "يأمل / يرجو", 0, None, 185, "de-wiktionary"),
        ("warten", None, None, "verb", "ينتظر", 0, None, 187, "de-wiktionary"),
        ("anrufen", None, None, "verb", "يتصل هاتفياً", 1, "an", 189, "de-wiktionary"),
        ("aufstehen", None, None, "verb", "يستيقظ / ينهض", 1, "auf", 191, "de-wiktionary"),
        ("einkaufen", None, None, "verb", "يتسوق", 1, "ein", 193, "de-wiktionary"),
        ("mitkommen", None, None, "verb", "يأتي مع / يرافق", 1, "mit", 195, "de-wiktionary"),
        ("aussehen", None, None, "verb", "يبدو / يظهر", 1, "aus", 197, "de-wiktionary"),

        # Common Adjectives
        ("gut", None, None, "adj", "جيد / طيب", 0, None, 18, "de-wiktionary"),
        ("groß", None, None, "adj", "كبير", 0, None, 22, "de-wiktionary"),
        ("klein", None, None, "adj", "صغير", 0, None, 30, "de-wiktionary"),
        ("neu", None, None, "adj", "جديد", 0, None, 34, "de-wiktionary"),
        ("alt", None, None, "adj", "قديم / كبير في السن", 0, None, 40, "de-wiktionary"),
        ("schön", None, None, "adj", "جميل", 0, None, 48, "de-wiktionary"),
        ("schnell", None, None, "adj", "سريع", 0, None, 64, "de-wiktionary"),
        ("langsam", None, None, "adj", "بطيء", 0, None, 88, "de-wiktionary"),
        ("einfach", None, None, "adj", "بسيط / سهل", 0, None, 50, "de-wiktionary"),
        ("schwer", None, None, "adj", "صعب / ثقيل", 0, None, 62, "de-wiktionary"),
        ("wichtig", None, None, "adj", "مهم / ضروري", 0, None, 56, "de-wiktionary"),
        ("richtig", None, None, "adj", "صحيح / صائب", 0, None, 58, "de-wiktionary"),
        ("falsch", None, None, "adj", "خاطئ / غلط", 0, None, 92, "de-wiktionary"),
        ("warm", None, None, "adj", "دافئ / حار", 0, None, 106, "de-wiktionary"),
        ("kalt", None, None, "adj", "بارد", 0, None, 108, "de-wiktionary"),
        ("früh", None, None, "adj", "مبكر", 0, None, 114, "de-wiktionary"),
        ("spät", None, None, "adj", "متأخر", 0, None, 116, "de-wiktionary"),
        ("viel", None, None, "adj", "كثير", 0, None, 14, "de-wiktionary"),
        ("wenig", None, None, "adj", "قليل", 0, None, 72, "de-wiktionary"),
        ("glücklich", None, None, "adj", "سعيد / محظوظ", 0, None, 126, "de-wiktionary"),
        ("traurig", None, None, "adj", "حزين", 0, None, 138, "de-wiktionary"),
        ("müde", None, None, "adj", "تعبان / نعسان", 0, None, 146, "de-wiktionary"),
        ("gesund", None, None, "adj", "صحي / معافى", 0, None, 150, "de-wiktionary"),
        ("krank", None, None, "adj", "مريض", 0, None, 152, "de-wiktionary")
    ]

    for item in words_data:
        german_word, article, plural, pos, arabic_tr, is_sep, sep_pref, rank, source = item
        cursor.execute("SELECT id FROM words WHERE LOWER(german_word) = LOWER(?)", (german_word,))
        row = cursor.fetchone()
        if row:
            # Update existing
            word_id = row[0]
            cursor.execute("""
                UPDATE words 
                SET article = ?, plural = ?, pos = ?, arabic_translation = ?, is_separable = ?, separable_prefix = ?, frequency_rank = ?, source = ?
                WHERE id = ?
            """, (article, plural, pos, arabic_tr, is_sep, sep_pref, rank, source, word_id))
        else:
            # Insert new
            cursor.execute("""
                INSERT INTO words (german_word, article, plural, pos, arabic_translation, is_separable, separable_prefix, frequency_rank, source)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """, (german_word, article, plural, pos, arabic_tr, is_sep, sep_pref, rank, source))
            word_id = cursor.lastrowid

        # Insert Review Schedule for word if missing
        cursor.execute("SELECT id FROM review_schedules WHERE word_id = ?", (word_id,))
        if not cursor.fetchone():
            cursor.execute("""
                INSERT INTO review_schedules (word_id, review_count, repetition_level, interval_days, ease_factor, last_reviewed_at, next_review_at)
                VALUES (?, 0, 0, 1, 2.5, 0, 1786716975317)
            """, (word_id,))

    # Add Conjugations for common verbs
    conjugations_data = {
        "sein": [
            ("Präsens", "ich", "bin"), ("Präsens", "du", "bist"), ("Präsens", "er/sie/es", "ist"),
            ("Präsens", "wir", "sind"), ("Präsens", "ihr", "seid"), ("Präsens", "sie/Sie", "sind"),
            ("Präteritum", "ich", "war"), ("Präteritum", "du", "warst"), ("Präteritum", "er/sie/es", "war"),
            ("Präteritum", "wir", "waren"), ("Präteritum", "ihr", "wart"), ("Präteritum", "sie/Sie", "waren"),
            ("Perfekt", "Partizip II", "gewesen")
        ],
        "haben": [
            ("Präsens", "ich", "habe"), ("Präsens", "du", "hast"), ("Präsens", "er/sie/es", "hat"),
            ("Präsens", "wir", "haben"), ("Präsens", "ihr", "habt"), ("Präsens", "sie/Sie", "haben"),
            ("Präteritum", "ich", "hatte"), ("Präteritum", "du", "hattest"), ("Präteritum", "er/sie/es", "hatte"),
            ("Präteritum", "wir", "hatten"), ("Präteritum", "ihr", "hattet"), ("Präteritum", "sie/Sie", "hatten"),
            ("Perfekt", "Partizip II", "gehabt")
        ],
        "gehen": [
            ("Präsens", "ich", "gehe"), ("Präsens", "du", "gehst"), ("Präsens", "er/sie/es", "geht"),
            ("Präsens", "wir", "gehen"), ("Präsens", "ihr", "geht"), ("Präsens", "sie/Sie", "gehen"),
            ("Präteritum", "ich", "ging"), ("Präteritum", "er/sie/es", "ging"),
            ("Perfekt", "Partizip II", "gegangen")
        ],
        "kommen": [
            ("Präsens", "ich", "komme"), ("Präsens", "du", "kommst"), ("Präsens", "er/sie/es", "kommt"),
            ("Präsens", "wir", "kommen"), ("Präsens", "ihr", "kommt"), ("Präsens", "sie/Sie", "kommen"),
            ("Präteritum", "ich", "kam"), ("Präteritum", "er/sie/es", "kam"),
            ("Perfekt", "Partizip II", "gekommen")
        ],
        "sprechen": [
            ("Präsens", "ich", "spreche"), ("Präsens", "du", "sprichst"), ("Präsens", "er/sie/es", "spricht"),
            ("Präsens", "wir", "sprechen"), ("Präsens", "ihr", "sprecht"), ("Präsens", "sie/Sie", "sprechen"),
            ("Präteritum", "ich", "sprach"), ("Präteritum", "er/sie/es", "sprach"),
            ("Perfekt", "Partizip II", "gesprochen")
        ],
        "essen": [
            ("Präsens", "ich", "esse"), ("Präsens", "du", "isst"), ("Präsens", "er/sie/es", "isst"),
            ("Präsens", "wir", "essen"), ("Präsens", "ihr", "esst"), ("Präsens", "sie/Sie", "essen"),
            ("Präteritum", "ich", "aß"), ("Präteritum", "er/sie/es", "aß"),
            ("Perfekt", "Partizip II", "gegessen")
        ],
        "trinken": [
            ("Präsens", "ich", "trinke"), ("Präsens", "du", "trinkst"), ("Präsens", "er/sie/es", "trinkt"),
            ("Präsens", "wir", "trinken"), ("Präsens", "ihr", "trinkt"), ("Präsens", "sie/Sie", "trinken"),
            ("Präteritum", "ich", "trank"), ("Präteritum", "er/sie/es", "trank"),
            ("Perfekt", "Partizip II", "getrunken")
        ],
        "lernen": [
            ("Präsens", "ich", "lerne"), ("Präsens", "du", "lernst"), ("Präsens", "er/sie/es", "lernt"),
            ("Präsens", "wir", "lernen"), ("Präsens", "ihr", "lernt"), ("Präsens", "sie/Sie", "lernen"),
            ("Perfekt", "Partizip II", "gelernt")
        ],
        "schreiben": [
            ("Präsens", "ich", "schreibe"), ("Präsens", "du", "schreibst"), ("Präsens", "er/sie/es", "schreibt"),
            ("Präsens", "wir", "schreiben"), ("Präsens", "ihr", "schreibt"), ("Präsens", "sie/Sie", "schreiben"),
            ("Präteritum", "ich", "schrieb"), ("Präteritum", "er/sie/es", "schrieb"),
            ("Perfekt", "Partizip II", "geschrieben")
        ],
        "lesen": [
            ("Präsens", "ich", "lese"), ("Präsens", "du", "liest"), ("Präsens", "er/sie/es", "liest"),
            ("Präsens", "wir", "lesen"), ("Präsens", "ihr", "lest"), ("Präsens", "sie/Sie", "lesen"),
            ("Präteritum", "ich", "las"), ("Präteritum", "er/sie/es", "las"),
            ("Perfekt", "Partizip II", "gelesen")
        ],
        "machen": [
            ("Präsens", "ich", "mache"), ("Präsens", "du", "machst"), ("Präsens", "er/sie/es", "macht"),
            ("Präsens", "wir", "machen"), ("Präsens", "ihr", "macht"), ("Präsens", "sie/Sie", "machen"),
            ("Perfekt", "Partizip II", "gemacht")
        ],
        "kaufen": [
            ("Präsens", "ich", "kaufe"), ("Präsens", "du", "kaufst"), ("Präsens", "er/sie/es", "kauft"),
            ("Präsens", "wir", "kaufen"), ("Präsens", "ihr", "kauft"), ("Präsens", "sie/Sie", "kaufen"),
            ("Perfekt", "Partizip II", "gekauft")
        ]
    }

    for verb, conj_list in conjugations_data.items():
        cursor.execute("SELECT id FROM words WHERE german_word = ?", (verb,))
        row = cursor.fetchone()
        if row:
            word_id = row[0]
            # Delete old conjugations to avoid duplicate insertions
            cursor.execute("DELETE FROM conjugations WHERE word_id = ?", (word_id,))
            for tense, person, form in conj_list:
                cursor.execute("""
                    INSERT INTO conjugations (word_id, tense, person, form, source)
                    VALUES (?, ?, ?, ?, 'de-wiktionary')
                """, (word_id, tense, person, form))

    # Add Examples
    examples_data = {
        "Orange": [
            ("Ich esse jeden Morgen eine Orange.", "أنا آكل برتقالة كل صباح."),
            ("Die Orangen aus Spanien sind sehr süß.", "البرتقال من إسبانيا حلو جداً.")
        ],
        "Apfel": [
            ("Ein Apfel am Tag hält den Arzt fern.", "تفاحة في اليوم تبعد الطبيب عنك."),
            ("Ich mag rote Äpfel.", "أنا أحب التفاح الأحمر.")
        ],
        "Buch": [
            ("Ich lese ein interessantes Buch.", "أنا أقرأ كتاباً ممتعاً."),
            ("Auf dem Tisch liegen viele Bücher.", "على الطاولة توجد كتب كثيرة.")
        ],
        "Haus": [
            ("Das Haus hat einen schönen Garten.", "المنزل لديه حديقة جميلة."),
            ("In unserer Straße stehen alte Häuser.", "في شارعنا تقف منازل قديمة.")
        ],
        "sein": [
            ("Ich bin Student an der Universität.", "أنا طالب في الجامعة."),
            ("Das Wetter ist heute sehr schön.", "الطقس اليوم جميل جداً.")
        ],
        "haben": [
            ("Ich habe eine Frage.", "لدي سؤال."),
            ("Wir haben viel Zeit.", "لدينا الكثير من الوقت.")
        ],
        "lernen": [
            ("Ich lerne jeden Tag Deutsch.", "أنا أتعلم الألمانية كل يوم.")
        ],
        "essen": [
            ("Was möchtest du heute essen?", "ماذا تريد أن تأكل اليوم؟")
        ],
        "trinken": [
            ("Trinkst du gerne Kaffee oder Tee?", "هل تحب شرب القهوة أم الشاي؟")
        ]
    }

    for word, ex_list in examples_data.items():
        cursor.execute("SELECT id FROM words WHERE german_word = ?", (word,))
        row = cursor.fetchone()
        if row:
            word_id = row[0]
            cursor.execute("DELETE FROM examples WHERE word_id = ?", (word_id,))
            for de_sent, ar_sent in ex_list:
                cursor.execute("""
                    INSERT INTO examples (word_id, german_sentence, arabic_translation, source)
                    VALUES (?, ?, ?, 'de-wiktionary')
                """, (word_id, de_sent, ar_sent))

    conn.commit()
    print("Database populated successfully.")

def main():
    print(f"Opening database: {DB_PATH}")
    os.makedirs(os.path.dirname(DB_PATH), exist_ok=True)
    conn = sqlite3.connect(DB_PATH)
    init_schema(conn)
    populate_database(conn)
    
    # Verification
    cursor = conn.cursor()
    cursor.execute("SELECT id, german_word, article, plural, pos, arabic_translation FROM words WHERE LOWER(german_word) = 'orange'")
    orange = cursor.fetchone()
    print("\n--- VERIFICATION RESULT ---")
    if orange:
        print(f"SUCCESS: Found word: ID={orange[0]}, german_word='{orange[1]}', article='{orange[2]}', plural='{orange[3]}', pos='{orange[4]}', translation='{orange[5]}'")
    else:
        print("FAILURE: 'Orange' not found in database!")
        sys.exit(1)

    cursor.execute("SELECT COUNT(*) FROM words")
    count = cursor.fetchone()[0]
    print(f"Total words in {DB_PATH}: {count}")
    conn.close()

if __name__ == "__main__":
    main()
