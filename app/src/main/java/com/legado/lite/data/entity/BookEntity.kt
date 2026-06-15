package com.legado.lite.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 书架中的一本书。
 * 注意：
 *  - bookUrl 是书源侧书籍详情页 URL（也是书籍唯一标识）
 *  - origin 来源书源 id（对应 BookSourceEntity.id）
 *  - cover / name / author / intro / kind 在加入书架时从书源解析
 */
@Entity(
    tableName = "books",
    indices = [Index("origin"), Index("bookUrl"), Index(value = ["origin", "bookUrl"], unique = true)]
)
data class BookEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val origin: Long,
    val originName: String,
    val bookUrl: String,
    val name: String,
    val author: String? = null,
    val cover: String? = null,
    val intro: String? = null,
    val kind: String? = null,
    val lastChapter: String? = null,
    val wordCount: String? = null,
    val tocHtml: String? = null,           // 最近一次解析到的目录 HTML
    val tocUpdatedAt: Long = 0L,
    val latestReadChapterTitle: String? = null,
    val latestReadChapterIndex: Int = 0,
    val latestReadOffset: Int = 0,
    val latestReadProgress: Float = 0f,
    val pinned: Boolean = false,
    val inShelf: Boolean = true,
    val addedAt: Long = 0L,
    val lastReadAt: Long = 0L
)
