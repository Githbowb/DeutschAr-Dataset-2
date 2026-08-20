package com.example.core.translation

import android.content.Context
import com.example.core.database.AppDatabase
import com.example.core.database.GermanNounDao
import com.example.core.database.GermanNounEntity
import com.example.core.database.GermanVerbDao
import com.example.core.database.GermanVerbEntity
import com.example.core.database.WordDao
import com.example.core.database.WordEntity
import com.example.core.database.WordRepository
import com.example.core.database.WordRepositoryImpl

class TriLanguageRepository(
    private val germanNounDao: GermanNounDao,
    private val germanVerbDao: GermanVerbDao,
    private val wordDao: WordDao,
    private val mlKitTranslator: MlKitTranslatorManager
) {

    constructor(
        wordRepository: WordRepository,
        mlKitManager: MlKitTranslatorManager
    ) : this(
        germanNounDao = (wordRepository as? WordRepositoryImpl)?.nounDao ?: object : GermanNounDao {
            override suspend fun findByLemma(lemma: String) = null
            override suspend fun findByPlural(plural: String) = null
            override suspend fun getById(id: Long) = null
            override suspend fun insert(noun: GermanNounEntity) = 0L
            override suspend fun insertNouns(nouns: List<GermanNounEntity>) {}
        },
        germanVerbDao = (wordRepository as? WordRepositoryImpl)?.verbDao ?: object : GermanVerbDao {
            override suspend fun findByInfinitive(infinitive: String) = null
            override suspend fun findByForm(verb: String) = null
            override suspend fun insert(verb: GermanVerbEntity) = 0L
            override suspend fun insertVerbs(verbs: List<GermanVerbEntity>) {}
        },
        wordDao = (wordRepository as? WordRepositoryImpl)?.wordDao ?: object : WordDao {
            override suspend fun getById(id: Long) = null
            override suspend fun findByGermanWordExact(word: String) = null
            override suspend fun findExactIgnoreCase(word: String) = null
            override suspend fun findByPluralIgnoreCase(plural: String) = null
            override suspend fun searchWords(query: String, limit: Int) = emptyList<WordEntity>()
            override suspend fun getSuggestions(prefix: String, limit: Int) = emptyList<String>()
            override suspend fun findByArabic(arabic: String, limit: Int) = emptyList<WordEntity>()
            override suspend fun getVocabularyList(limit: Int) = emptyList<WordEntity>()
            override suspend fun getAllWordsList() = emptyList<WordEntity>()
            override suspend fun countWords() = 0
            override suspend fun insertWord(word: WordEntity) = 0L
            override suspend fun insert(word: WordEntity) = 0L
            override suspend fun insertAll(words: List<WordEntity>) {}
        },
        mlKitTranslator = mlKitManager
    )

    suspend fun lookupLocalNoun(query: String): GermanNounEntity? {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return null
        if (GermanFunctionWords.isFunctionWord(trimmed)) return null

        return germanNounDao.findByLemma(trimmed)
            ?: germanNounDao.findByPlural(trimmed)
    }

    suspend fun lookupLocalVerb(query: String): GermanVerbEntity? {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return null
        return germanVerbDao.findByInfinitive(trimmed)
            ?: germanVerbDao.findByForm(trimmed)
    }

    suspend fun translateTriLanguage(germanText: String): Result<TrilingualResult> {
        val trimmed = germanText.trim()
        if (trimmed.isEmpty()) {
            return Result.failure(IllegalArgumentException("German text is empty"))
        }

        return try {
            val functionWordType = GermanFunctionWords.classifyFunctionWord(trimmed)
            if (functionWordType != null) {
                // Function word - bypass nouns DB, never assign article
                val en = mlKitTranslator.translateDirect(trimmed, "de", "en")
                val ar = mlKitTranslator.translateDirect(en, "en", "ar")
                return Result.success(
                    TrilingualResult(
                        german = trimmed,
                        germanArticle = null,
                        english = en,
                        arabic = ar,
                        source = TranslationSource.ML_KIT_FALLBACK,
                        posType = functionWordType,
                        sourceLanguage = "de",
                        targetLanguage = "ar"
                    )
                )
            }

            // 1. Check local noun DB
            val noun = lookupLocalNoun(trimmed)
            if (noun != null) {
                val matchedWord = wordDao.findExactIgnoreCase(noun.lemma) ?: wordDao.findByPluralIgnoreCase(noun.lemma)
                val en = try { mlKitTranslator.translateDirect(noun.lemma, "de", "en") } catch (_: Exception) { "" }
                val ar = matchedWord?.arabicTranslation?.ifBlank { null }
                    ?: try { mlKitTranslator.translateDirect(en.ifBlank { noun.lemma }, "en", "ar") } catch (_: Exception) { "" }
                return Result.success(
                    TrilingualResult(
                        german = noun.lemma,
                        germanArticle = noun.article,
                        english = en.ifBlank { matchedWord?.arabicTranslation ?: "" },
                        arabic = ar.ifBlank { matchedWord?.arabicTranslation ?: "" },
                        source = TranslationSource.LOCAL_DB,
                        plural = noun.nominativPlural,
                        cases = noun.rawGrammar ?: noun.flexion,
                        sourceLanguage = "de",
                        targetLanguage = "ar"
                    )
                )
            }

            // 2. Check local verb DB
            val verb = lookupLocalVerb(trimmed)
            if (verb != null) {
                val matchedWord = wordDao.findExactIgnoreCase(verb.infinitive)
                val en = try { mlKitTranslator.translateDirect(verb.infinitive, "de", "en") } catch (_: Exception) { "" }
                val ar = matchedWord?.arabicTranslation?.ifBlank { null }
                    ?: try { mlKitTranslator.translateDirect(en.ifBlank { verb.infinitive }, "en", "ar") } catch (_: Exception) { "" }
                val verbResult = VerbConjugator.createConjugation(
                    infinitive = verb.infinitive,
                    verbEntity = verb,
                    english = en,
                    arabic = ar
                )
                return Result.success(
                    TrilingualResult(
                        german = verb.infinitive,
                        germanArticle = null,
                        english = en.ifBlank { matchedWord?.arabicTranslation ?: "" },
                        arabic = ar.ifBlank { matchedWord?.arabicTranslation ?: "" },
                        source = TranslationSource.LOCAL_DB,
                        verbData = verbResult,
                        sourceLanguage = "de",
                        targetLanguage = "ar"
                    )
                )
            }

            // 3. Check general words table
            val exactWord = wordDao.findExactIgnoreCase(trimmed) ?: wordDao.findByPluralIgnoreCase(trimmed)
            if (exactWord != null) {
                val isVerb = exactWord.wordType.equals("verb", ignoreCase = true) || VerbConjugator.isGermanVerb(exactWord.germanWord)
                val en = try { mlKitTranslator.translateDirect(exactWord.germanWord, "de", "en") } catch (_: Exception) { "" }
                val verbResult = if (isVerb) {
                    VerbConjugator.createConjugation(
                        infinitive = VerbConjugator.resolveInfinitive(exactWord.germanWord),
                        english = en.ifBlank { exactWord.arabicTranslation },
                        arabic = exactWord.arabicTranslation
                    )
                } else null

                return Result.success(
                    TrilingualResult(
                        german = exactWord.germanWord,
                        germanArticle = if (!isVerb) exactWord.article else null,
                        english = en.ifBlank { exactWord.arabicTranslation },
                        arabic = exactWord.arabicTranslation,
                        source = TranslationSource.LOCAL_DB,
                        plural = if (!isVerb) exactWord.plural else null,
                        verbData = verbResult,
                        sourceLanguage = "de",
                        targetLanguage = "ar"
                    )
                )
            }

            // 4. ML Kit fallback (Pivot DE -> EN -> AR)
            val isVerb = VerbConjugator.isGermanVerb(trimmed)
            val en = try { mlKitTranslator.translateDirect(trimmed, "de", "en") } catch (_: Exception) { trimmed }
            val ar = try { mlKitTranslator.translateDirect(en, "en", "ar") } catch (_: Exception) { "" }
            val verbResult = if (isVerb) {
                VerbConjugator.createConjugation(
                    infinitive = VerbConjugator.resolveInfinitive(trimmed),
                    english = en,
                    arabic = ar
                )
            } else null

            Result.success(
                TrilingualResult(
                    german = trimmed,
                    germanArticle = null,
                    english = en,
                    arabic = ar.ifBlank { "ترجمة قيد التنزيل / جاري التهيئة" },
                    source = TranslationSource.ML_KIT_FALLBACK,
                    verbData = verbResult,
                    sourceLanguage = "de",
                    targetLanguage = "ar"
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun translateGermanToTriLanguage(germanText: String): Result<TrilingualResult> {
        return translateTriLanguage(germanText)
    }

    suspend fun translateArabicToTriLanguage(arabicText: String): Result<TrilingualResult> {
        val trimmed = arabicText.trim()
        if (trimmed.isEmpty()) {
            return Result.failure(IllegalArgumentException("Arabic text is empty"))
        }

        return try {
            // Check local words table first
            val localWords = wordDao.findByArabic(trimmed)
            val exactLocal = localWords.firstOrNull { it.arabicTranslation.equals(trimmed, ignoreCase = true) }
                ?: localWords.firstOrNull()

            if (exactLocal != null) {
                val en = try { mlKitTranslator.translateDirect(exactLocal.germanWord, "de", "en") } catch (_: Exception) { "" }
                val noun = lookupLocalNoun(exactLocal.germanWord)
                val verb = lookupLocalVerb(exactLocal.germanWord)
                return Result.success(
                    TrilingualResult(
                        german = exactLocal.germanWord,
                        germanArticle = exactLocal.article ?: noun?.article,
                        english = en,
                        arabic = exactLocal.arabicTranslation.ifBlank { trimmed },
                        source = TranslationSource.LOCAL_DB,
                        plural = exactLocal.plural ?: noun?.nominativPlural,
                        cases = noun?.rawGrammar ?: noun?.flexion,
                        verbData = verb?.let {
                            VerbConjugator.createConjugation(
                                infinitive = it.infinitive,
                                verbEntity = it,
                                english = en,
                                arabic = exactLocal.arabicTranslation
                            )
                        },
                        sourceLanguage = "ar",
                        targetLanguage = "de"
                    )
                )
            }

            // Neural Pivot AR -> EN -> DE
            val en = try { mlKitTranslator.translateDirect(trimmed, "ar", "en") } catch (_: Exception) { trimmed }
            val de = try { mlKitTranslator.translateDirect(en, "en", "de") } catch (_: Exception) { "" }

            val noun = if (de.isNotBlank()) lookupLocalNoun(de) else null
            val verb = if (de.isNotBlank()) lookupLocalVerb(de) else null
            val word = if (de.isNotBlank()) wordDao.findExactIgnoreCase(de) else null

            Result.success(
                TrilingualResult(
                    german = noun?.lemma ?: verb?.infinitive ?: word?.germanWord ?: de.ifBlank { "..." },
                    germanArticle = noun?.article ?: word?.article,
                    english = en,
                    arabic = trimmed,
                    source = if (noun != null || verb != null || word != null) TranslationSource.LOCAL_DB else TranslationSource.ML_KIT_FALLBACK,
                    plural = noun?.nominativPlural ?: word?.plural,
                    cases = noun?.rawGrammar ?: noun?.flexion,
                    verbData = verb?.let {
                        VerbConjugator.createConjugation(
                            infinitive = it.infinitive,
                            verbEntity = it,
                            english = en,
                            arabic = trimmed
                        )
                    },
                    sourceLanguage = "ar",
                    targetLanguage = "de"
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun close() {
        mlKitTranslator.close()
    }

    companion object {
        @Volatile
        private var INSTANCE: TriLanguageRepository? = null

        fun getInstance(context: Context): TriLanguageRepository {
            return INSTANCE ?: synchronized(this) {
                val db = AppDatabase.getInstance(context)
                val instance = TriLanguageRepository(
                    germanNounDao = db.germanNounDao(),
                    germanVerbDao = db.germanVerbDao(),
                    wordDao = db.wordDao(),
                    mlKitTranslator = MlKitTranslatorManager()
                )
                INSTANCE = instance
                instance
            }
        }
    }
}

