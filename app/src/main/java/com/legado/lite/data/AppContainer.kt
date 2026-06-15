package com.legado.lite.data

import android.content.Context
import com.legado.lite.data.dao.BookDao
import com.legado.lite.data.dao.BookSourceDao
import com.legado.lite.data.dao.ChapterDao
import com.legado.lite.data.dao.ReadHistoryDao
import com.legado.lite.data.dao.SearchHistoryDao
import com.legado.lite.data.http.WebFetcher
import com.legado.lite.data.source.BookParser
import com.legado.lite.data.source.BookSearcher
import com.legado.lite.data.preferences.AppPreferencesStore
import com.legado.lite.domain.repository.BookRepository
import com.legado.lite.domain.repository.BookSourceRepository
import com.legado.lite.domain.repository.PreferencesRepository
import com.legado.lite.domain.repository.ReadHistoryRepository

interface AppContainer {
    val database: LegadoDatabase
    val bookSourceRepository: BookSourceRepository
    val bookRepository: BookRepository
    val readHistoryRepository: ReadHistoryRepository
    val preferencesRepository: PreferencesRepository
    val bookSearcher: BookSearcher
    val bookParser: BookParser
    val webFetcher: WebFetcher
}

class DefaultAppContainer(context: Context) : AppContainer {

    override val database: LegadoDatabase = LegadoDatabase.get(context)

    private val sourceDao: BookSourceDao = database.bookSourceDao()
    private val bookDao: BookDao = database.bookDao()
    private val chapterDao: ChapterDao = database.chapterDao()
    private val historyDao: ReadHistoryDao = database.readHistoryDao()
    private val searchHistoryDao: SearchHistoryDao = database.searchHistoryDao()

    override val webFetcher: WebFetcher = WebFetcher()
    override val bookSearcher: BookSearcher = BookSearcher(webFetcher)
    override val bookParser: BookParser = BookParser(webFetcher)

    override val preferencesRepository: PreferencesRepository = AppPreferencesStore(context)

    override val bookSourceRepository: BookSourceRepository = BookSourceRepositoryImpl(sourceDao, context)
    override val bookRepository: BookRepository = BookRepositoryImpl(bookDao, chapterDao, bookSearcher, bookParser, context)
    override val readHistoryRepository: ReadHistoryRepository = ReadHistoryRepositoryImpl(historyDao, bookDao)
}
