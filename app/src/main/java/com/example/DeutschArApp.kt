package com.example

import android.app.Application
import com.example.core.database.AppDatabase
import com.example.core.database.DatabaseSeeder
import com.example.core.database.WordRepository
import com.example.core.database.WordRepositoryImpl
import com.example.core.translation.MlKitTranslatorManager
import com.example.core.translation.TriLanguageRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class DeutschArApp : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    lateinit var database: AppDatabase
        private set

    lateinit var wordRepository: WordRepository
        private set

    lateinit var triLanguageRepository: TriLanguageRepository
        private set

    lateinit var mlKitTranslator: MlKitTranslatorManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = AppDatabase.getInstance(this)
        mlKitTranslator = MlKitTranslatorManager()

        val wordRepo = WordRepositoryImpl(
            wordDao = database.wordDao(),
            conjugationDao = database.conjugationDao(),
            exampleDao = database.exampleDao(),
            historyDao = database.historyDao(),
            nounDao = database.germanNounDao(),
            verbDao = database.germanVerbDao()
        )
        wordRepository = wordRepo

        triLanguageRepository = TriLanguageRepository(
            germanNounDao = database.germanNounDao(),
            germanVerbDao = database.germanVerbDao(),
            wordDao = database.wordDao(),
            mlKitTranslator = mlKitTranslator
        )

        applicationScope.launch {
            DatabaseSeeder.seedDatabaseIfEmpty(database)
        }
    }

    companion object {
        lateinit var instance: DeutschArApp
            private set
    }
}
