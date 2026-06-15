package com.legado.lite.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.legado.lite.data.entity.BookEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {

    @Query("SELECT * FROM books WHERE inShelf = 1 ORDER BY pinned DESC, lastReadAt DESC, addedAt DESC")
    fun observeShelf(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE inShelf = 1 ORDER BY lastReadAt DESC, addedAt DESC")
    fun observeRecent(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE id = :id")
    fun observeById(id: Long): Flow<BookEntity?>

    @Query("SELECT * FROM books WHERE id = :id")
    suspend fun findById(id: Long): BookEntity?

    @Query("SELECT * FROM books WHERE origin = :origin AND bookUrl = :bookUrl LIMIT 1")
    suspend fun findByOriginAndUrl(origin: Long, bookUrl: String): BookEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(book: BookEntity): Long

    @Update
    suspend fun update(book: BookEntity)

    @Delete
    suspend fun delete(book: BookEntity)

    @Query("DELETE FROM books WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE books SET inShelf = :inShelf, lastReadAt = :lastReadAt WHERE id = :id")
    suspend fun setInShelf(id: Long, inShelf: Boolean, lastReadAt: Long = System.currentTimeMillis())

    @Query("UPDATE books SET pinned = :pinned WHERE id = :id")
    suspend fun setPinned(id: Long, pinned: Boolean)

    @Query("UPDATE books SET latestReadChapterIndex = :chapterIndex, latestReadChapterTitle = :title, latestReadOffset = :offset, latestReadProgress = :progress, lastReadAt = :lastReadAt WHERE id = :id")
    suspend fun updateReadProgress(
        id: Long,
        chapterIndex: Int,
        title: String,
        offset: Int,
        progress: Float,
        lastReadAt: Long = System.currentTimeMillis()
    )
}
