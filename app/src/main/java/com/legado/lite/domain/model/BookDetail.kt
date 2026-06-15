package com.legado.lite.domain.model

/** 书籍详情。 */
data class BookDetail(
    val name: String,
    val author: String,
    val cover: String?,
    val intro: String?,
    val kind: String?,
    val lastChapter: String?,
    val tocUrl: String
)
