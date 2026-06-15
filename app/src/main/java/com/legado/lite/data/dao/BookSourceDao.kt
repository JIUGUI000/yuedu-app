package com.legado.lite.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.legado.lite.data.entity.BookSourceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookSourceDao {

    @Query("SELECT * FROM book_sources ORDER BY orderNum ASC, id ASC")
    fun observeAll(): Flow<List<BookSourceEntity>>

    @Query("SELECT * FROM book_sources WHERE enabled = 1 ORDER BY orderNum ASC, id ASC")
    suspend fun listEnabled(): List<BookSourceEntity>

    @Query("SELECT * FROM book_sources ORDER BY orderNum ASC, id ASC")
    suspend fun listAll(): List<BookSourceEntity>

    @Query("SELECT * FROM book_sources WHERE id = :id")
    suspend fun findById(id: Long): BookSourceEntity?

    @Query("SELECT * FROM book_sources WHERE id = :id")
    fun observeById(id: Long): Flow<BookSourceEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(source: BookSourceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(sources: List<BookSourceEntity>)

    @Update
    suspend fun update(source: BookSourceEntity)

    @Delete
    suspend fun delete(source: BookSourceEntity)

    @Query("DELETE FROM book_sources WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM book_sources")
    suspend fun clear()
}
