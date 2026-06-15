package com.legado.lite.ui.detail

import androidx.lifecycle.viewModelScope
import com.legado.lite.data.entity.BookEntity
import com.legado.lite.data.entity.ChapterEntity
import com.legado.lite.ui.LegadoViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BookDetailViewModel(app: android.app.Application) : LegadoViewModel(app) {

    private val _book = MutableStateFlow<BookEntity?>(null)
    val book: StateFlow<BookEntity?> = _book.asStateFlow()

    private val _chapters = MutableStateFlow<List<ChapterEntity>>(emptyList())
    val chapters: StateFlow<List<ChapterEntity>> = _chapters.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _inShelf = MutableStateFlow(false)
    val inShelf: StateFlow<Boolean> = _inShelf.asStateFlow()

    fun load(bookId: Long) {
        scope.launch {
            val b = container.bookRepository.findById(bookId) ?: return@launch
            _book.value = b
            _inShelf.value = b.inShelf
            _loading.value = true
            try {
                val source = container.database.bookSourceDao().findById(b.origin)
                if (source != null) {
                    // 拉详情 & 目录
                    val detail = container.bookRepository.fetchDetail(source, b.bookUrl)
                    if (detail != null) {
                        _book.value = detail
                        val list = container.bookRepository.loadChapters(detail)
                        _chapters.value = list
                    }
                } else {
                    _error.value = "书源已被删除"
                }
            } catch (t: Throwable) {
                _error.value = t.message ?: "加载失败"
            } finally {
                _loading.value = false
            }
        }
    }

    fun addToShelf() {
        val b = _book.value ?: return
        scope.launch {
            container.bookRepository.setInShelf(b.id, true)
            _inShelf.value = true
        }
    }

    fun removeFromShelf() {
        val b = _book.value ?: return
        scope.launch {
            container.bookRepository.setInShelf(b.id, false)
            _inShelf.value = false
        }
    }
}
