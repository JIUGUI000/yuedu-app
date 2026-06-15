package com.legado.lite.domain.model

/**
 * 跨书源聚合后的搜索结果。
 */
data class SearchResult(
    val sourceId: Long,
    val sourceName: String,
    val name: String,
    val author: String,
    val cover: String?,
    val intro: String?,
    val kind: String?,
    val lastChapter: String?,
    val bookUrl: String
) {
    /** 唯一键：来源书源 + 书籍 URL。 */
    val key: String get() = "$sourceId::$bookUrl"
}
