package com.legado.lite.data.repo

import android.content.Context
import com.legado.lite.data.dao.BookDao
import com.legado.lite.data.dao.ChapterDao
import com.legado.lite.data.entity.BookEntity
import com.legado.lite.data.entity.BookSourceEntity
import com.legado.lite.data.entity.ChapterEntity
import com.legado.lite.data.source.BookParser
import com.legado.lite.data.source.BookSearcher
import com.legado.lite.domain.model.Chapter
import com.legado.lite.domain.model.SearchResult
import com.legado.lite.domain.repository.BookRepository
import kotlinx.coroutines.flow.Flow

class BookRepositoryImpl(
    private val bookDao: BookDao,
    private val chapterDao: ChapterDao,
    private val searcher: BookSearcher,
    private val parser: BookParser,
    @Suppress("UNUSED_PARAMETER") context: Context
) : BookRepository {

    override fun observeShelf(): Flow<List<BookEntity>> = bookDao.observeShelf()
    override fun observeRecent(): Flow<List<BookEntity>> = bookDao.observeRecent()
    override suspend fun findById(id: Long): BookEntity? = bookDao.findById(id)
    override suspend fun findByOriginAndUrl(origin: Long, url: String): BookEntity? =
        bookDao.findByOriginAndUrl(origin, url)

    override suspend fun addToShelf(result: SearchResult, source: BookSourceEntity): BookEntity {
        val existing = bookDao.findByOriginAndUrl(source.id, result.bookUrl)
        if (existing != null) {
            val updated = existing.copy(inShelf = true, lastReadAt = System.currentTimeMillis())
            bookDao.update(updated)
            return updated
        }
        val book = BookEntity(
            origin = source.id,
            originName = source.name,
            bookUrl = result.bookUrl,
            name = result.name,
            author = result.author.ifEmpty { null },
            cover = result.cover,
            intro = result.intro,
            kind = result.kind,
            lastChapter = result.lastChapter,
            inShelf = true,
            addedAt = System.currentTimeMillis(),
            lastReadAt = System.currentTimeMillis()
        )
        val id = bookDao.upsert(book)
        return book.copy(id = id)
    }

    override suspend fun setInShelf(id: Long, inShelf: Boolean) = bookDao.setInShelf(id, inShelf)
    override suspend fun setPinned(id: Long, pinned: Boolean) = bookDao.setPinned(id, pinned)
    override suspend fun deleteById(id: Long) {
        bookDao.deleteById(id)
        chapterDao.deleteByBook(id)
    }

    override suspend fun updateReadProgress(
        id: Long,
        chapterIndex: Int,
        title: String,
        offset: Int,
        progress: Float
    ) = bookDao.updateReadProgress(id, chapterIndex, title, offset, progress)

    override suspend fun searchOnline(keyword: String, onResult: suspend (SearchResult) -> Unit) {
        // 拿所有启用的书源
        val container = com.legado.lite.LegadoApp.get().container
        val sourceDao = container.database.bookSourceDao()
        val sources = sourceDao.listEnabled()
        if (sources.isEmpty()) return
        searcher.search(sources, keyword, onResult)
    }

    override suspend fun fetchDetail(source: BookSourceEntity, bookUrl: String): BookEntity? {
        val existing = bookDao.findByOriginAndUrl(source.id, bookUrl)
        val detail = parser.parseDetail(source, bookUrl) ?: return existing
        if (existing == null) {
            val book = BookEntity(
                origin = source.id,
                originName = source.name,
                bookUrl = bookUrl,
                name = detail.name,
                author = detail.author,
                cover = detail.cover,
                intro = detail.intro,
                kind = detail.kind,
                lastChapter = detail.lastChapter,
                inShelf = false,
                addedAt = System.currentTimeMillis()
            )
            val id = bookDao.upsert(book)
            return book.copy(id = id)
        }
        val updated = existing.copy(
            name = detail.name,
            author = detail.author,
            cover = detail.cover,
            intro = detail.intro,
            kind = detail.kind,
            lastChapter = detail.lastChapter
        )
        bookDao.update(updated)
        return updated
    }

    override suspend fun loadChapters(book: BookEntity, forceRefresh: Boolean): List<ChapterEntity> {
        if (!forceRefresh) {
            val cached = chapterDao.listByBook(book.id)
            if (cached.isNotEmpty()) return cached
        } else {
            chapterDao.deleteByBook(book.id)
        }
        val source = currentSource(book) ?: return emptyList()
        val toc = parser.parseToc(source, book.bookUrl)
        if (toc.isEmpty()) return emptyList()
        val entities = toc.map { c ->
            ChapterEntity(bookId = book.id, title = c.title, url = c.url, index = c.index)
        }
        chapterDao.upsertAll(entities)
        return entities
    }

    override suspend fun loadChapterContent(book: BookEntity, chapter: ChapterEntity): String {
        if (chapter.isCached && !chapter.contentText.isNullOrEmpty()) return chapter.contentText
        val source = currentSource(book) ?: return ""
        val text = parser.parseContent(source, Chapter(chapter.title, chapter.url, chapter.index))
        if (text.isNotEmpty()) {
            chapterDao.setContent(chapter.id, text)
        }
        return text
    }

    private fun currentSource(book: BookEntity): BookSourceEntity? {
        // 通过 AppContainer 拿到 sourceDao（懒加载）。
        val container = com.legado.lite.LegadoApp.get().container
        val dao = container.database.bookSourceDao()
        return kotlinx.coroutines.runBlocking { dao.findById(book.origin) }
    }
}
