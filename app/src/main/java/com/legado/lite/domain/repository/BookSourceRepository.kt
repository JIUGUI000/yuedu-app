package com.legado.lite.domain.repository

import com.legado.lite.data.entity.BookSourceEntity
import kotlinx.coroutines.flow.Flow

interface BookSourceRepository {
    fun observeAll(): Flow<List<BookSourceEntity>>
    suspend fun listEnabled(): List<BookSourceEntity>
    suspend fun findById(id: Long): BookSourceEntity?
    suspend fun upsert(source: BookSourceEntity): Long
    suspend fun upsertAll(sources: List<BookSourceEntity>)
    suspend fun update(source: BookSourceEntity)
    suspend fun delete(source: BookSourceEntity)
    suspend fun deleteById(id: Long)
    suspend fun clear()
    suspend fun importFromJson(json: String): Int
    suspend fun exportToJson(): String
}
