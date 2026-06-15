package com.legado.lite.domain.repository

import com.legado.lite.data.entity.ReadHistoryEntity
import kotlinx.coroutines.flow.Flow

interface ReadHistoryRepository {
    fun observeAll(): Flow<List<ReadHistoryEntity>>
    suspend fun record(item: ReadHistoryEntity)
    suspend fun clear()
}
