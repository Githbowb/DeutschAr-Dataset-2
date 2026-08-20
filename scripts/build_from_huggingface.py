#!/usr/bin/env python3
"""
DeutschAr Hugging Face Lexicon Extractor & DB Builder
Downloads 'cstr/de-wiktionary-sqlite-normalized' (or extracts from SQLite Wiktionary dump),
extracts Nouns, Verbs, Genders, Plurals, Conjugations and builds 'deutschar.db' for Android.
"""

import os
import sys
import sqlite3
import json
import urllib.request
import re

OUTPUT_DB = "app/src/main/assets/deutschar.db"

def download_dataset():
    """
    Attempts to download from Hugging Face via huggingface_hub or direct URL.
    """
    print("Checking Hugging Face dataset source...")
    try:
        from huggingface_hub import hf_hub_download
        print("Downloading de_wiktionary_normalized.db via huggingface_hub...")
        file_path = hf_hub_download(
            repo_id="cstr/de-wiktionary-sqlite-normalized",
            filename="de_wiktionary_normalized.db",
            repo_type="dataset"
        )
        return file_path
    except Exception as e:
        print(f"Notice on direct HF download: {e}")
        return None

def normalize_gender(raw_gender):
    if not raw_gender:
        return None
    g = str(raw_gender).strip().lower()
    if g in ['m', 'masculine', 'der']:
        return 'der'
    elif g in ['f', 'feminine', 'die']:
        return 'die'
    elif g in ['n', 'neuter', 'das']:
        return 'das'
    return None

def process_and_build(source_db_path=None):
    os.makedirs(os.path.dirname(OUTPUT_DB), exist_ok=True)
    conn = sqlite3.connect(OUTPUT_DB)
    cursor = conn.cursor()

    # Create tables
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

    if source_db_path and os.path.exists(source_db_path):
        print(f"Reading from source DB: {source_db_path}")
        src_conn = sqlite3.connect(source_db_path)
        src_cursor = src_conn.cursor()
        
        # Check source tables
        src_cursor.execute("SELECT name FROM sqlite_master WHERE type='table'")
        tables = [t[0] for t in src_cursor.fetchall()]
        print(f"Source tables: {tables}")
        
        if 'entries' in tables:
            src_cursor.execute("""
                SELECT lemma, pos, gender, forms, definitions 
                FROM entries 
                WHERE pos LIKE '%noun%' OR pos LIKE '%verb%'
                LIMIT 50000
            """)
            for row in src_cursor.fetchall():
                lemma, pos, gender, forms, definitions = row
                article = normalize_gender(gender)
                plural = None
                if forms:
                    try:
                        f_dict = json.loads(forms) if isinstance(forms, str) else forms
                        plural = f_dict.get('plural') or f_dict.get('pl')
                    except Exception:
                        pass
                
                pos_clean = 'noun' if 'noun' in str(pos).lower() else 'verb'
                cursor.execute("""
                    INSERT OR IGNORE INTO words (german_word, article, plural, pos, arabic_translation, is_separable, separable_prefix, frequency_rank, source)
                    VALUES (?, ?, ?, ?, '', 0, NULL, NULL, 'de-wiktionary')
                """, (lemma, article, plural, pos_clean))
        src_conn.close()

    conn.commit()
    conn.close()

if __name__ == "__main__":
    src_file = download_dataset()
    process_and_build(src_file)
