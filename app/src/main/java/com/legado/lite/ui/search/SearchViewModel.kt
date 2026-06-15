package com.legado.lite.ui.search

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.legado.lite.LegadoApp
import com.legado.lite.data.entity.SearchHistoryEntity
import com.legado.lite.domain.model.SearchResult
import com.legado.lite.ui.LegadoViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SearchViewModel(app: android.app.Application) : LegadoViewModel(app) {

    private val _keyword = MutableStateFlow("")
    val keyword: StateFlow<String> = _keyword.asStateFlow()

    private val _results = MutableStateFlow<List<SearchResult>>(emptyList())
    val results: StateFlow<List<SearchResult>> = _results.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errors = MutableStateFlow<List<String>>(emptyList())
    val errors: StateFlow<List<String>> = _errors.asStateFlow()

    val history: StateFlow<List<SearchHistoryEntity>> = container.database.searchHistoryDao()
        .observeAll()
        .stateIn(scope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private var searchJob: Job? = null

    fun setKeyword(value: String) {
        _keyword.value = value
    }

    fun clear() {
        _results.value = emptyList()
        _errors.value = emptyList()
    }

    fun search() {
        val kw = _keyword.value.trim()
        if (kw.isEmpty()) return
        searchJob?.cancel()
        _results.value = emptyList()
        _errors.value = emptyList()
        _isLoading.value = true
        scope.launch {
            container.database.searchHistoryDao().insert(
                SearchHistoryEntity(keyword = kw, createdAt = System.currentTimeMillis())
            )
        }
        searchJob = scope.launch {
            try {
                container.bookRepository.searchOnline(kw) { result ->
                    _results.value = _results.value + result
                }
            } catch (t: Throwable) {
                _errors.value = _errors.value + (t.message ?: "未知错误")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearHistory() {
        scope.launch { container.database.searchHistoryDao().clear() }
    }

    fun addToShelfAndOpen(r: SearchResult, onResult: (Long) -> Unit) {
        scope.launch {
            val source = container.database.bookSourceDao().findById(r.sourceId) ?: return@launch
            val book = container.bookRepository.addToShelf(r, source)
            onResult(book.id)
        }
    }
}
