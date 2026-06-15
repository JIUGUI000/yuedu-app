package com.legado.lite.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 章节缓存。
 *  - bookId 关联 BookEntity.id
 *  - title / url / index 是从目录页解析后存入
 *  - contentText 是缓存的正文（按需懒加载）
 *  - contentUpdatedAt 上次拉取正文的时刻
 */
@Entity(
    tableName = "chapters",
    indices = [Index("bookId"), Index(value = ["bookId", "url"], unique = true)]
)
data class ChapterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: Long,
    val title: String,
    val url: String,
    val index: Int,
    val contentText: String? = null,
    val contentUpdatedAt: Long = 0L,
    val isCached: Boolean = false
)
