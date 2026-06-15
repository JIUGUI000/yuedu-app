package com.legado.lite.data.repo

import com.legado.lite.data.dao.BookDao
import com.legado.lite.data.dao.ReadHistoryDao
import com.legado.lite.data.entity.ReadHistoryEntity
import com.legado.lite.domain.repository.ReadHistoryRepository
import kotlinx.coroutines.flow.Flow

class ReadHistoryRepositoryImpl(
    private val dao: ReadHistoryDao,
    @Suppress("UNUSED_PARAMETER") private val bookDao: BookDao
) : ReadHistoryRepository {
    override fun observeAll(): Flow<List<ReadHistoryEntity>> = dao.observeAll()
    override suspend fun record(item: ReadHistoryEntity) = dao.insert(item)
    override suspend fun clear() = dao.clear()
}
