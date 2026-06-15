package com.legado.lite.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 搜索历史。
 */
@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val keyword: String,
    val createdAt: Long
)
