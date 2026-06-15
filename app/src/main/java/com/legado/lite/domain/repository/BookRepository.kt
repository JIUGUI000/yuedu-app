package com.legado.lite.domain.repository

import com.legado.lite.data.entity.BookEntity
import com.legado.lite.data.entity.BookSourceEntity
import com.legado.lite.data.entity.ChapterEntity
import com.legado.lite.domain.model.Chapter
import com.legado.lite.domain.model.SearchResult
import kotlinx.coroutines.flow.Flow

interface BookRepository {

    // 书架
    fun observeShelf(): Flow<List<BookEntity>>
    fun observeRecent(): Flow<List<BookEntity>>
    suspend fun findById(id: Long): BookEntity?
    suspend fun findByOriginAndUrl(origin: Long, url: String): BookEntity?
    suspend fun addToShelf(result: SearchResult, source: BookSourceEntity): BookEntity
    suspend fun setInShelf(id: Long, inShelf: Boolean)
    suspend fun setPinned(id: Long, pinned: Boolean)
    suspend fun deleteById(id: Long)
    suspend fun updateReadProgress(id: Long, chapterIndex: Int, title: String, offset: Int, progress: Float)

    // 搜索
    suspend fun searchOnline(keyword: String, onResult: suspend (SearchResult) -> Unit)

    // 详情 & 目录 & 正文
    suspend fun fetchDetail(source: BookSourceEntity, bookUrl: String): BookEntity?
    suspend fun loadChapters(book: BookEntity, forceRefresh: Boolean = false): List<ChapterEntity>
    suspend fun loadChapterContent(book: BookEntity, chapter: ChapterEntity): String
}
