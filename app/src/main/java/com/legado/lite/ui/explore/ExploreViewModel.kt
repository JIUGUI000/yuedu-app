package com.legado.lite.ui.explore

import com.legado.lite.data.entity.BookEntity
import com.legado.lite.data.entity.ReadHistoryEntity
import com.legado.lite.ui.LegadoViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ExploreViewModel(app: android.app.Application) : LegadoViewModel(app) {

    val recent: StateFlow<List<BookEntity>> = container.bookRepository.observeRecent()
        .stateIn(scope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val history: StateFlow<List<ReadHistoryEntity>> = container.readHistoryRepository.observeAll()
        .stateIn(scope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
