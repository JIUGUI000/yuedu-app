package com.legado.lite.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 阅读历史。 每次打开阅读器 / 翻到新章节都写一条。
 */
@Entity(
    tableName = "read_history",
    indices = [Index("bookId"), Index("readAt")]
)
data class ReadHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: Long,
    val bookName: String,
    val author: String?,
    val cover: String?,
    val origin: Long,
    val originName: String,
    val bookUrl: String,
    val chapterIndex: Int,
    val chapterTitle: String,
    val readAt: Long
)
