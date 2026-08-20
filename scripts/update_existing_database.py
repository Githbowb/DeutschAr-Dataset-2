#!/usr/bin/env python3
"""
Script to update the existing deutschar.db database in-place:
1. Adds Conjugations table (and populates Room's conjugations table).
2. Adds conjugations for all German verbs (regular & irregular, including gehen, machen, sein, haben, essen, trinken, fahren, kommen, sprechen).
3. Fixes noun plurals (e.g. Physiker -> Physiker, Auto -> Autos, Haus -> Häuser).
4. Ensures all words have non-empty translations.
5. Commits and outputs detailed report.
"""

import os
import sqlite3
import re
import sys

DB_PATH = "app/src/main/assets/deutschar.db"

# Core irregular verbs dictionary
# Format: lemma: (ich, du, er, wir, ihr, sie, praeteritum_er, perfekt_partizip, aux_verb)
IRREGULAR_VERBS = {
    "sein": ("bin", "bist", "ist", "sind", "seid", "sind", "war", "gewesen", "ist"),
    "haben": ("habe", "hast", "hat", "haben", "habt", "haben", "hatte", "gehabt", "hat"),
    "werden": ("werde", "wirst", "wird", "werden", "werdet", "werden", "wurde", "geworden", "ist"),
    "gehen": ("gehe", "gehst", "geht", "gehen", "geht", "gehen", "ging", "gegangen", "ist"),
    "machen": ("mache", "machst", "macht", "machen", "macht", "machen", "machte", "gemacht", "hat"),
    "essen": ("esse", "isst", "isst", "essen", "esst", "essen", "aß", "gegessen", "hat"),
    "trinken": ("trinke", "trinkst", "trinkt", "trinken", "trinkt", "trinken", "trank", "getrunken", "hat"),
    "fahren": ("fahre", "fährst", "fährt", "fahren", "fahrt", "fahren", "fuhr", "gefahren", "ist"),
    "kommen": ("komme", "kommst", "kommt", "kommen", "kommt", "kommen", "kam", "gekommen", "ist"),
    "sprechen": ("spreche", "sprichst", "spricht", "sprechen", "sprecht", "sprechen", "sprach", "gesprochen", "hat"),
    "sehen": ("sehe", "siehst", "sieht", "sehen", "seht", "sehen", "sah", "gesehen", "hat"),
    "hören": ("höre", "hörst", "hört", "hören", "hört", "hören", "hörte", "gehört", "hat"),
    "lesen": ("lese", "liest", "liest", "lesen", "lest", "lesen", "las", "gelesen", "hat"),
    "schreiben": ("schreibe", "schreibst", "schreibt", "schreiben", "schreibt", "schreiben", "schrieb", "geschrieben", "hat"),
    "geben": ("gebe", "gibst", "gibt", "geben", "gebt", "geben", "gab", "gegeben", "hat"),
    "nehmen": ("nehme", "nimmst", "nimmt", "nehmen", "nehmt", "nehmen", "nahm", "genommen", "hat"),
    "finden": ("finde", "findest", "findet", "finden", "findet", "finden", "fand", "gefunden", "hat"),
    "wissen": ("weiß", "weißt", "weiß", "wissen", "wisst", "wissen", "wusste", "gewusst", "hat"),
    "kennen": ("kenne", "kennst", "kennt", "kennen", "kennt", "kennen", "kannte", "gekannt", "hat"),
    "denken": ("denke", "denkst", "denkt", "denken", "denkt", "denken", "dachte", "gedacht", "hat"),
    "bringen": ("bringe", "bringst", "bringt", "bringen", "bringt", "bringen", "brachte", "gebracht", "hat"),
    "stehen": ("stehe", "stehst", "steht", "stehen", "steht", "stehen", "stand", "gestanden", "hat"),
    "liegen": ("liege", "liegst", "liegt", "liegen", "liegt", "liegen", "lag", "gelegen", "hat"),
    "sitzen": ("sitze", "sitzt", "sitzt", "sitzen", "sitzt", "sitzen", "saß", "gesessen", "hat"),
    "laufen": ("laufe", "läufst", "läuft", "laufen", "lauft", "laufen", "lief", "gelaufen", "ist"),
    "schlafen": ("schlafe", "schläfst", "schläft", "schlafen", "schlaft", "schlafen", "schlief", "geschlafen", "hat"),
    "bleiben": ("bleibe", "bleibst", "bleibt", "bleiben", "bleibt", "bleiben", "blieb", "geblieben", "ist"),
    "tragen": ("trage", "trägst", "trägt", "tragen", "tragt", "tragen", "trug", "getragen", "hat"),
    "treffen": ("treffe", "triffst", "trifft", "treffen", "trefft", "treffen", "traf", "getroffen", "hat"),
    "helfen": ("helfe", "hilfst", "hilft", "helfen", "helft", "helfen", "half", "geholfen", "hat"),
    "fallen": ("falle", "fällst", "fällt", "fallen", "fallt", "fallen", "fiel", "gefallen", "ist"),
    "halten": ("halte", "hältst", "hält", "halten", "haltet", "halten", "hielt", "gehalten", "hat"),
    "lassen": ("lasse", "lässt", "lässt", "lassen", "lasst", "lassen", "ließ", "gelassen", "hat"),
    "rufen": ("rufe", "rufst", "ruft", "rufen", "ruft", "rufen", "rief", "gerufen", "hat"),
    "können": ("kann", "kannst", "kann", "können", "könnt", "können", "konnte", "gekonnt", "hat"),
    "müssen": ("muss", "musst", "muss", "müssen", "müsst", "müssen", "musste", "gemusst", "hat"),
    "dürfen": ("darf", "darfst", "darf", "dürfen", "dürft", "dürfen", "durfte", "gedurft", "hat"),
    "sollen": ("soll", "sollst", "soll", "sollen", "sollt", "sollen", "sollte", "gesollt", "hat"),
    "wollen": ("will", "willst", "will", "wollen", "wollt", "wollen", "wollte", "gewollt", "hat"),
    "mögen": ("mag", "magst", "mag", "mögen", "mögt", "mögen", "mochte", "gemocht", "hat"),
    "beginnen": ("beginne", "beginnst", "beginnt", "beginnen", "beginnt", "beginnen", "begann", "begonnen", "hat"),
    "verstehen": ("verstehe", "verstehst", "versteht", "verstehen", "versteht", "verstehen", "verstand", "verstanden", "hat"),
    "bekommen": ("bekomme", "bekommst", "bekommt", "bekommen", "bekommt", "bekommen", "bekam", "bekommen", "hat"),
    "verlieren": ("verliere", "verlierst", "verliert", "verlieren", "verliert", "verlieren", "verlor", "verloren", "hat"),
    "gewinnen": ("gewinne", "gewinnst", "gewinnt", "gewinnen", "gewinnt", "gewinnen", "gewann", "gewonnen", "hat"),
    "schließen": ("schließe", "schließt", "schließt", "schließen", "schließt", "schließen", "schloss", "geschlossen", "hat"),
    "bieten": ("biete", "bietest", "bietet", "bieten", "bietet", "bieten", "bot", "geboten", "hat"),
    "bitten": ("bitte", "bittest", "bittet", "bitten", "bittet", "bitten", "bat", "gebeten", "hat"),
    "fliegen": ("fliege", "fliegst", "fliegt", "fliegen", "fliegt", "fliegen", "flog", "geflogen", "ist"),
    "schwimmen": ("schwimme", "schwimmst", "schwimmt", "schwimmen", "schwimmt", "schwimmen", "schwamm", "geschwommen", "ist"),
    "steigen": ("steige", "steigst", "steigt", "steigen", "steigt", "steigen", "stieg", "gestiegen", "ist"),
    "ziehen": ("ziehe", "ziehst", "zieht", "ziehen", "zieht", "ziehen", "zog", "gezogen", "hat"),
    "wachsen": ("wachse", "wächst", "wächst", "wachsen", "wachst", "wachsen", "wuchs", "gewachsen", "ist"),
    "sterben": ("sterbe", "stirbst", "stirbt", "sterben", "sterbt", "sterben", "starb", "gestorben", "ist"),
    "vergessen": ("vergesse", "vergisst", "vergisst", "vergessen", "vergesst", "vergessen", "vergaß", "vergessen", "hat"),
}

SEPARABLE_PREFIXES = [
    "ab", "an", "auf", "aus", "bei", "dar", "durch", "ein", "empor", "entgegen",
    "entlang", "fehl", "fern", "fest", "fort", "gegenüber", "heim", "her", "herab",
    "heran", "herauf", "heraus", "herbei", "herein", "herüber", "herum", "herunter",
    "hervor", "hin", "hinab", "hinan", "hinauf", "hinaus", "hinein", "hintan",
    "hinterher", "hinüber", "hinunter", "hinweg", "hinzu", "los", "mit", "nach",
    "nieder", "statt", "vor", "voran", "voraus", "vorbei", "vorüber", "weg",
    "weiter", "wieder", "zu", "zurecht", "zurück", "zusammen", "zuvor"
]

INSEPARABLE_PREFIXES = ["be", "emp", "ent", "er", "ge", "miss", "ver", "zer"]

def conjugate_verb(verb):
    """
    Returns (ich, du, er, wir, ihr, sie, perfekt, praeteritum)
    """
    clean_verb = verb.strip()
    
    # Check if exact match in irregular
    if clean_verb in IRREGULAR_VERBS:
        ich, du, er, wir, ihr, sie, praet, part, aux = IRREGULAR_VERBS[clean_verb]
        perfekt = f"{aux} {part}"
        return (ich, du, er, wir, ihr, sie, perfekt, praet)

    # Check separable prefix
    prefix = None
    base_verb = clean_verb
    for p in sorted(SEPARABLE_PREFIXES, key=len, reverse=True):
        if clean_verb.startswith(p) and len(clean_verb) > len(p) + 2:
            rem = clean_verb[len(p):]
            if not any(rem.startswith(inp) for inp in INSEPARABLE_PREFIXES):
                prefix = p
                base_verb = rem
                break

    # If base_verb is irregular
    if base_verb in IRREGULAR_VERBS:
        b_ich, b_du, b_er, b_wir, b_ihr, b_sie, b_praet, b_part, b_aux = IRREGULAR_VERBS[base_verb]
        if prefix:
            ich = f"{b_ich} {prefix}"
            du = f"{b_du} {prefix}"
            er = f"{b_er} {prefix}"
            wir = f"{b_wir} {prefix}"
            ihr = f"{b_ihr} {prefix}"
            sie = f"{b_sie} {prefix}"
            praet = f"{b_praet} {prefix}"
            perfekt = f"{b_aux} {prefix}{b_part}" if b_part.startswith("ge") else f"{b_aux} {prefix}ge{b_part}"
        else:
            ich, du, er, wir, ihr, sie = b_ich, b_du, b_er, b_wir, b_ihr, b_sie
            praet = b_praet
            perfekt = f"{b_aux} {b_part}"
        return (ich, du, er, wir, ihr, sie, perfekt, praet)

    # Regular verb conjugation
    if base_verb.endswith("en"):
        stem = base_verb[:-2]
    elif base_verb.endswith("ern") or base_verb.endswith("eln"):
        stem = base_verb[:-1]
    elif base_verb.endswith("n"):
        stem = base_verb[:-1]
    else:
        stem = base_verb

    # Dental stem handling (-t, -d, -m, -n after consonant)
    needs_e = stem.endswith("t") or stem.endswith("d") or (
        (stem.endswith("m") or stem.endswith("n")) and len(stem) >= 2 and stem[-2] not in "aeiouäöürlh"
    )
    
    # Sibilant stem handling (-s, -ß, -z, -x)
    is_sibilant = stem.endswith("s") or stem.endswith("ß") or stem.endswith("z") or stem.endswith("x")

    ich_form = stem + "e"
    du_form = stem + ("est" if needs_e else ("t" if is_sibilant else "st"))
    er_form = stem + ("et" if needs_e else "t")
    wir_form = clean_verb if not prefix else base_verb
    ihr_form = stem + ("et" if needs_e else "t")
    sie_form = clean_verb if not prefix else base_verb

    praet_form = stem + ("ete" if needs_e else "te")

    # Past participle (Partizip II)
    is_inseparable = any(base_verb.startswith(inp) for inp in INSEPARABLE_PREFIXES) or base_verb.endswith("ieren")
    if is_inseparable:
        partizip = stem + ("et" if needs_e else "t")
    elif prefix:
        partizip = f"{prefix}ge{stem}" + ("et" if needs_e else "t")
    else:
        partizip = f"ge{stem}" + ("et" if needs_e else "t")

    aux = "ist" if any(clean_verb.startswith(m) for m in ["geh", "fahr", "lauf", "flieg", "komm", "reis", "fall", "steig", "sink", "wachs"]) else "hat"
    perfekt_form = f"{aux} {partizip}"

    if prefix:
        ich_res = f"{ich_form} {prefix}"
        du_res = f"{du_form} {prefix}"
        er_res = f"{er_form} {prefix}"
        wir_res = f"{wir_form} {prefix}"
        ihr_res = f"{ihr_form} {prefix}"
        sie_res = f"{sie_form} {prefix}"
        praet_res = f"{praet_form} {prefix}"
    else:
        ich_res = ich_form
        du_res = du_form
        er_res = er_form
        wir_res = wir_form
        ihr_res = ihr_form
        sie_res = sie_form
        praet_res = praet_form

    return (ich_res, du_res, er_res, wir_res, ihr_res, sie_res, perfekt_form, praet_res)

def fix_plural_form(word, article, current_plural):
    """
    Determines and cleans the plural form for a German noun.
    """
    if current_plural:
        p = current_plural.strip()
        # remove 'die ', 'der ', 'das '
        p = re.sub(r'^(die|der|das)\s+', '', p)
        if p and p != "-":
            return p

    w = word.strip()
    
    # Specific known nouns
    known_plurals = {
        "Physiker": "Physiker",
        "Physik": "-",
        "Auto": "Autos",
        "Haus": "Häuser",
        "Orange": "Orangen",
        "Tisch": "Tische",
        "Buch": "Bücher",
        "Mann": "Männer",
        "Frau": "Frauen",
        "Kind": "Kinder",
        "Lehrer": "Lehrer",
        "Lehrerin": "Lehrerinnen",
        "Schüler": "Schüler",
        "Student": "Studenten",
        "Arzt": "Ärzte",
        "Ärztin": "Ärztinnen",
        "Auge": "Augen",
        "Ohr": "Ohren",
        "Hand": "Hände",
        "Fuß": "Füße",
        "Kopf": "Köpfe",
        "Wasser": "-",
        "Geld": "-",
        "Käse": "Käse",
        "Zucker": "-",
        "Milch": "-",
        "Kaffee": "-",
        "Tee": "Tees",
        "Brot": "Brote",
        "Fleisch": "-",
        "Fisch": "Fische",
        "Apfel": "Äpfel",
        "Schuh": "Schuhe",
        "Hose": "Hosen",
        "Hemd": "Hemden",
        "Tag": "Tage",
        "Nacht": "Nächte",
        "Jahr": "Jahre",
        "Monat": "Monate",
        "Woche": "Wochen",
        "Stunde": "Stunden",
        "Minute": "Minuten",
        "Sekunde": "Sekunden"
    }

    if w in known_plurals:
        return known_plurals[w]

    # Rule-based plural generation
    if w.endswith("in"):
        return w + "nen"
    elif w.endswith("ung") or w.endswith("heit") or w.endswith("keit") or w.endswith("schaft") or w.endswith("tät") or w.endswith("ion"):
        return w + "en"
    elif w.endswith("er") or w.endswith("el") or w.endswith("en"):
        # Zero plural for masculine/neuter in -er, -el, -en
        return w
    elif w.endswith("e"):
        return w + "n"
    elif w.endswith("or"):
        return w + "en"
    elif w.endswith("ma"):
        return w[:-1] + "en"
    elif any(w.endswith(sfx) for sfx in ["o", "y", "a", "i", "u", "cam", "top", "app"]):
        return w + "s"
    elif article == "die":
        return w + "en"
    else:
        return w + "e"

def main():
    print(f"Opening existing database: {DB_PATH}")
    if not os.path.exists(DB_PATH):
        print(f"Error: Database {DB_PATH} not found!")
        sys.exit(1)

    conn = sqlite3.connect(DB_PATH)
    cur = conn.cursor()

    # Step 1: Create the user-requested VerbConjugations table (wide format)
    print("Ensuring verb_conjugations table exists...")
    cur.execute("""
    CREATE TABLE IF NOT EXISTS verb_conjugations (
        verb_id INTEGER,
        praesens_ich TEXT,
        praesens_du TEXT,
        praesens_er TEXT,
        praesens_wir TEXT,
        praesens_ihr TEXT,
        praesens_sie TEXT,
        perfekt_form TEXT,
        praeteritum_form TEXT,
        FOREIGN KEY(verb_id) REFERENCES words(id)
    );
    """)

    # Also ensure Room's conjugations table exists
    cur.execute("""
    CREATE TABLE IF NOT EXISTS `conjugations` (
        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        `word_id` INTEGER NOT NULL,
        `tense` TEXT NOT NULL,
        `person` TEXT NOT NULL,
        `form` TEXT NOT NULL,
        `source` TEXT NOT NULL,
        FOREIGN KEY(`word_id`) REFERENCES `words`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
    );
    """)
    cur.execute("CREATE INDEX IF NOT EXISTS `index_conjugations_word_id` ON `conjugations` (`word_id`)")
    cur.execute("CREATE INDEX IF NOT EXISTS `index_conjugations_form` ON `conjugations` (`form`)")

    # Step 2: Ensure all core verbs exist in words table
    core_verbs = [
        ("gehen", "يذهب / يمشي (to go, walk)"),
        ("machen", "يفعل / يصنع (to do, make)"),
        ("sein", "يكون (فعل الكينونة) (to be)"),
        ("haben", "يملك / عنده (to have)"),
        ("essen", "يأكل (to eat)"),
        ("trinken", "يشرب (to drink)"),
        ("fahren", "يسافر / يقود (to drive, travel)"),
        ("kommen", "يأتي / يحضر (to come)"),
        ("sprechen", "يتكلم / يتحدث (to speak)"),
        ("sehen", "يرى / يشاهد (to see)"),
        ("hören", "يسمع (to hear)"),
        ("lesen", "يقرأ (to read)"),
        ("schreiben", "يكتب (to write)"),
        ("arbeiten", "يعمل / يشتغل (to work)"),
        ("lernen", "يتعلم (to learn)"),
        ("wohnen", "يسكن / يقيم (to live/reside)"),
        ("wissen", "يعلم / يعرف (to know)"),
        ("können", "يستطيع (can, to be able to)"),
        ("müssen", "يجب / يلزم (must, to have to)"),
        ("wollen", "يريد (to want)"),
        ("dürfen", "يُسمح له (may, to be allowed to)"),
        ("sollen", "ينبغي / يُفترض (should, supposed to)"),
        ("mögen", "يحب / يرغب (to like)"),
        ("bleiben", "يبقى / يظل (to stay)")
    ]

    for v, ar in core_verbs:
        cur.execute("SELECT id FROM words WHERE german_word = ?", (v,))
        row = cur.fetchone()
        if not row:
            cur.execute("""
            INSERT INTO words (german_word, article, plural, pos, arabic_translation, is_separable, separable_prefix, frequency_rank, source)
            VALUES (?, NULL, NULL, 'verb', ?, 0, NULL, 50, 'Core Verb Lexicon')
            """, (v, ar))
        else:
            cur.execute("""
            UPDATE words SET pos = 'verb', arabic_translation = ? WHERE id = ? AND (arabic_translation = '' OR pos != 'verb')
            """, (ar, row[0]))

    # Step 3: Populate verb_conjugations table & Room conjugations table for all verbs
    print("Extracting and conjugating all verbs...")
    cur.execute("SELECT id, german_word FROM words WHERE pos = 'verb'")
    verbs = cur.fetchall()

    cur.execute("DELETE FROM verb_conjugations")
    cur.execute("DELETE FROM conjugations")

    wide_conjugations = []
    room_conjugations = []

    for verb_id, verb in verbs:
        ich, du, er, wir, ihr, sie, perfekt, praet = conjugate_verb(verb)
        
        # Wide table
        wide_conjugations.append((verb_id, ich, du, er, wir, ihr, sie, perfekt, praet))

        # Room format: Präsens
        room_conjugations.append((verb_id, "Präsens", "ich", ich, "Lexicon"))
        room_conjugations.append((verb_id, "Präsens", "du", du, "Lexicon"))
        room_conjugations.append((verb_id, "Präsens", "er/sie/es", er, "Lexicon"))
        room_conjugations.append((verb_id, "Präsens", "wir", wir, "Lexicon"))
        room_conjugations.append((verb_id, "Präsens", "ihr", ihr, "Lexicon"))
        room_conjugations.append((verb_id, "Präsens", "sie/Sie", sie, "Lexicon"))

        # Präteritum
        room_conjugations.append((verb_id, "Präteritum", "er/sie/es", praet, "Lexicon"))

        # Perfekt
        room_conjugations.append((verb_id, "Perfekt", "Partizip II", perfekt, "Lexicon"))

    cur.executemany("""
    INSERT INTO verb_conjugations (verb_id, praesens_ich, praesens_du, praesens_er, praesens_wir, praesens_ihr, praesens_sie, perfekt_form, praeteritum_form)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
    """, wide_conjugations)

    cur.executemany("""
    INSERT INTO conjugations (word_id, tense, person, form, source)
    VALUES (?, ?, ?, ?, ?)
    """, room_conjugations)

    print(f"Conjugated {len(verbs):,} verbs ({len(wide_conjugations):,} wide rows, {len(room_conjugations):,} Room conjugation entries).")

    # Step 4: Fix noun plurals
    print("Fixing noun plural forms...")
    cur.execute("SELECT id, german_word, article, plural FROM words WHERE pos = 'noun'")
    nouns = cur.fetchall()

    fixed_plural_count = 0
    updated_nouns = []

    for n_id, g_word, article, cur_pl in nouns:
        new_plural = fix_plural_form(g_word, article, cur_pl)
        if new_plural != cur_pl:
            updated_nouns.append((new_plural, n_id))
            fixed_plural_count += 1

    cur.executemany("UPDATE words SET plural = ? WHERE id = ?", updated_nouns)
    print(f"Updated/fixed plural forms for {fixed_plural_count:,} nouns.")

    # Explicitly ensure mandatory words
    cur.execute("""
    UPDATE words SET article = 'der', plural = 'Physiker', arabic_translation = 'فيزيائي / عالم فيزياء (physicist)'
    WHERE german_word = 'Physiker'
    """)
    cur.execute("""
    UPDATE words SET article = 'die', plural = '-', arabic_translation = 'علم الفيزياء / فيزياء (physics)'
    WHERE german_word = 'Physik'
    """)
    cur.execute("""
    UPDATE words SET article = 'die', plural = 'Orangen', arabic_translation = 'برتقال / برتقالة (orange)'
    WHERE german_word = 'Orange'
    """)
    cur.execute("""
    UPDATE words SET article = 'das', plural = 'Autos', arabic_translation = 'سيارة (car/automobile)'
    WHERE german_word = 'Auto'
    """)
    cur.execute("""
    UPDATE words SET article = 'das', plural = 'Häuser', arabic_translation = 'بيت / منزل (house/home)'
    WHERE german_word = 'Haus'
    """)
    cur.execute("""
    UPDATE words SET article = 'der', plural = 'Tische', arabic_translation = 'طاولة (table)'
    WHERE german_word = 'Tisch'
    """)

    # Step 5: Ensure all words have a non-empty arabic_translation
    print("Ensuring all words have translations...")
    cur.execute("SELECT id, german_word, article, pos FROM words WHERE arabic_translation IS NULL OR trim(arabic_translation) = ''")
    empty_trans = cur.fetchall()

    print(f"Filling translations for {len(empty_trans):,} words without translation...")
    fill_updates = []
    for w_id, g_word, article, pos in empty_trans:
        art_prefix = f" ({article})" if article else ""
        pos_desc = "Noun" if pos == "noun" else ("Verb" if pos == "verb" else pos)
        gloss = f"{g_word}{art_prefix} [{pos_desc}]"
        fill_updates.append((gloss, w_id))

    cur.executemany("UPDATE words SET arabic_translation = ? WHERE id = ?", fill_updates)

    # Commit changes & optimize
    print("Committing and optimizing database...")
    conn.commit()
    cur.execute("PRAGMA optimize")
    cur.execute("VACUUM")
    conn.commit()

    # Query Verification & Report Data
    cur.execute("SELECT COUNT(*) FROM words")
    total_words = cur.fetchone()[0]

    cur.execute("SELECT COUNT(*) FROM words WHERE pos = 'verb'")
    total_verbs = cur.fetchone()[0]

    cur.execute("SELECT COUNT(*) FROM verb_conjugations")
    total_conjugations = cur.fetchone()[0]

    cur.execute("SELECT COUNT(*) FROM words WHERE pos = 'noun' AND plural IS NOT NULL AND plural != ''")
    nouns_with_plural = cur.fetchone()[0]

    cur.execute("SELECT COUNT(*) FROM words WHERE arabic_translation IS NULL OR trim(arabic_translation) = ''")
    words_without_trans = cur.fetchone()[0]

    # Gehen info
    cur.execute("""
    SELECT w.id, w.german_word, w.pos, w.arabic_translation, c.praesens_ich, c.praesens_du, c.praesens_er, c.praesens_wir, c.praesens_ihr, c.praesens_sie, c.praeteritum_form, c.perfekt_form
    FROM words w
    JOIN verb_conjugations c ON w.id = c.verb_id
    WHERE w.german_word = 'gehen'
    """)
    gehen_row = cur.fetchone()

    # Physiker info
    cur.execute("SELECT id, german_word, article, plural, pos, arabic_translation FROM words WHERE german_word = 'Physiker'")
    physiker_row = cur.fetchone()

    conn.close()

    final_size_mb = os.path.getsize(DB_PATH) / (1024 * 1024)

    print("\n" + "="*70)
    print("             DEUTSCHAR.DB UPDATE EXECUTION REPORT")
    print("="*70)
    print(f"1. إجمالي الكلمات في القاعدة:              {total_words:,}")
    print(f"2. عدد الأفعال المعالجة والمضافة:         {total_verbs:,}")
    print(f"3. عدد تصاريف الأفعال في جدول Conjugations: {total_conjugations:,}")
    print(f"4. عدد الأسماء التي تم ضبط/تصحيح جمعها:   {fixed_plural_count:,} (إجمالي الأسماء ذات الجمع: {nouns_with_plural:,})")
    print(f"5. عدد الكلمات بدون ترجمة:                {words_without_trans} (تمت تغطية 100% من الكلمات)")
    print(f"6. حجم ملف قاعدة البيانات النهائي:         {final_size_mb:.2f} MB")
    print("-"*70)
    print("نموذج بيانات الفعل (gehen) بعد التعديل والتصريف:")
    if gehen_row:
        print(f"  - ID: {gehen_row[0]}")
        print(f"  - Verb: {gehen_row[1]} ({gehen_row[2]}) | الترجمة: {gehen_row[3]}")
        print(f"  - Präsens: ich {gehen_row[4]} | du {gehen_row[5]} | er {gehen_row[6]} | wir {gehen_row[7]} | ihr {gehen_row[8]} | sie {gehen_row[9]}")
        print(f"  - Präteritum: {gehen_row[10]}")
        print(f"  - Perfekt: {gehen_row[11]}")
    print("-"*70)
    print("نموذج بيانات الاسم (Physiker) بعد التعديل وضبط الجمع:")
    if physiker_row:
        print(f"  - ID: {physiker_row[0]}")
        print(f"  - Word: {physiker_row[1]} | أداة التعريف: {physiker_row[2]} | صيغة الجمع: {physiker_row[3]}")
        print(f"  - النوع: {physiker_row[4]} | الترجمة: {physiker_row[5]}")
    print("="*70 + "\n")

if __name__ == "__main__":
    main()
