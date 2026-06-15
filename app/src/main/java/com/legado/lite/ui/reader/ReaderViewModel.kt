package com.legado.lite.ui.reader

import androidx.lifecycle.viewModelScope
import com.legado.lite.data.entity.BookEntity
import com.legado.lite.data.entity.ChapterEntity
import com.legado.lite.data.preferences.ReaderPreferences
import com.legado.lite.ui.LegadoViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ReaderViewModel(app: android.app.Application) : LegadoViewModel(app) {

    private val _book = MutableStateFlow<BookEntity?>(null)
    val book: StateFlow<BookEntity?> = _book.asStateFlow()

    private val _chapters = MutableStateFlow<List<ChapterEntity>>(emptyList())
    val chapters: StateFlow<List<ChapterEntity>> = _chapters.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _content = MutableStateFlow("")
    val content: StateFlow<String> = _content.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _prefs = MutableStateFlow(ReaderPreferences())
    val prefs: StateFlow<ReaderPreferences> = _prefs.asStateFlow()

    private val _chromeVisible = MutableStateFlow(true)
    val chromeVisible: StateFlow<Boolean> = _chromeVisible.asStateFlow()

    fun toggleChrome() {
        _chromeVisible.value = !_chromeVisible.value
    }

    fun load(bookId: Long, startIndex: Int) {
        scope.launch {
            val b = container.bookRepository.findById(bookId) ?: return@launch
            _book.value = b
            _prefs.value = container.preferencesRepository.reader.first()
            val list = container.bookRepository.loadChapters(b)
            _chapters.value = list
            val start = when {
                startIndex >= 0 -> startIndex
                b.latestReadChapterIndex in list.indices -> b.latestReadChapterIndex
                else -> 0
            }
            _currentIndex.value = start
            loadContent(start)
        }
    }

    private fun loadContent(index: Int) {
        val list = _chapters.value
        val b = _book.value ?: return
        if (index !in list.indices) return
        scope.launch {
            _loading.value = true
            try {
                val ch = list[index]
                val text = container.bookRepository.loadChapterContent(b, ch)
                _content.value = text.ifEmpty { "（章节无内容或解析失败）" }
                // 保存进度
                val progress = if (list.isEmpty()) 0f else index.toFloat() / list.size
                container.bookRepository.updateReadProgress(b.id, index, ch.title, 0, progress)
                // 写阅读历史
                container.readHistoryRepository.record(
                    com.legado.lite.data.entity.ReadHistoryEntity(
                        bookId = b.id,
                        bookName = b.name,
                        author = b.author,
                        cover = b.cover,
                        origin = b.origin,
                        originName = b.originName,
                        bookUrl = b.bookUrl,
                        chapterIndex = index,
                        chapterTitle = ch.title,
                        readAt = System.currentTimeMillis()
                    )
                )
            } finally {
                _loading.value = false
            }
        }
    }

    fun next() {
        val list = _chapters.value
        val cur = _currentIndex.value
        if (cur + 1 < list.size) {
            _currentIndex.value = cur + 1
            loadContent(cur + 1)
        }
    }

    fun prev() {
        val cur = _currentIndex.value
        if (cur > 0) {
            _currentIndex.value = cur - 1
            loadContent(cur - 1)
        }
    }

    fun jumpTo(index: Int) {
        val list = _chapters.value
        if (index in list.indices) {
            _currentIndex.value = index
            loadContent(index)
        }
    }

    fun setPrefs(p: ReaderPreferences) {
        _prefs.value = p
        scope.launch { container.preferencesRepository.setReader(p) }
    }
}
