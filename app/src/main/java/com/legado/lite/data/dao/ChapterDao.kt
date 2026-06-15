package com.legado.lite.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.legado.lite.data.entity.ChapterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChapterDao {

    @Query("SELECT * FROM chapters WHERE bookId = :bookId ORDER BY `index` ASC")
    fun observeByBook(bookId: Long): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE bookId = :bookId ORDER BY `index` ASC")
    suspend fun listByBook(bookId: Long): List<ChapterEntity>

    @Query("SELECT * FROM chapters WHERE bookId = :bookId AND `index` = :index LIMIT 1")
    suspend fun findByIndex(bookId: Long, index: Int): ChapterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(chapter: ChapterEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(chapters: List<ChapterEntity>)

    @Query("UPDATE chapters SET contentText = :content, contentUpdatedAt = :ts, isCached = 1 WHERE id = :id")
    suspend fun setContent(id: Long, content: String, ts: Long = System.currentTimeMillis())

    @Query("DELETE FROM chapters WHERE bookId = :bookId")
    suspend fun deleteByBook(bookId: Long)
}
