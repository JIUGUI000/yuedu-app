package com.legado.lite.ui.bookshelf

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.legado.lite.data.entity.BookEntity
import com.legado.lite.ui.LegadoViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BookshelfViewModel(app: android.app.Application) : LegadoViewModel(app) {

    val books: StateFlow<List<BookEntity>> = container.bookRepository.observeShelf()
        .stateIn(scope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun togglePin(id: Long, pinned: Boolean) {
        scope.launch { container.bookRepository.setPinned(id, pinned) }
    }

    fun removeFromShelf(id: Long) {
        scope.launch { container.bookRepository.setInShelf(id, false) }
    }

    fun delete(id: Long) {
        scope.launch { container.bookRepository.deleteById(id) }
    }
}
