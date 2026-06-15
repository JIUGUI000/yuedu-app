package com.legado.lite.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.legado.lite.data.entity.ReadHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadHistoryDao {

    @Query("SELECT * FROM read_history ORDER BY readAt DESC LIMIT 200")
    fun observeAll(): Flow<List<ReadHistoryEntity>>

    @Query("SELECT * FROM read_history WHERE bookId = :bookId ORDER BY readAt DESC LIMIT 1")
    suspend fun latestForBook(bookId: Long): ReadHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ReadHistoryEntity)

    @Query("DELETE FROM read_history WHERE bookId = :bookId")
    suspend fun deleteByBook(bookId: Long)

    @Query("DELETE FROM read_history")
    suspend fun clear()
}
