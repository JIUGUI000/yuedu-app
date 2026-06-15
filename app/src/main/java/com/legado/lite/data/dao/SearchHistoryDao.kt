package com.legado.lite.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.legado.lite.data.entity.SearchHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchHistoryDao {

    @Query("SELECT * FROM search_history ORDER BY createdAt DESC LIMIT 30")
    fun observeAll(): Flow<List<SearchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: SearchHistoryEntity)

    @Query("DELETE FROM search_history WHERE keyword = :keyword")
    suspend fun deleteByKeyword(keyword: String)

    @Query("DELETE FROM search_history")
    suspend fun clear()
}
