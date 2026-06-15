package com.legado.lite.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 书源。
 * 字段命名参考 Legado，便于熟悉开源阅读的用户迁移。
 *
 *  - [ruleSearch]：搜索 URL 模板，支持 {{key}} 占位
 *  - [searchListRule] / [searchNameRule] / [searchAuthorRule] / [searchCoverRule] / [searchIntroRule] / [searchUrlRule]：
 *      搜索结果列表项的解析规则（JSONPath / CSS / Regex / 默认替换）
 *  - [ruleBookName] 等：详情页的字段解析规则
 *  - [ruleChapterList]：目录页规则
 *  - [ruleContent]：正文规则
 */
@Entity(
    tableName = "book_sources",
    indices = [Index("enabled"), Index("orderNum")]
)
data class BookSourceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val url: String,
    val group: String? = null,
    val enabled: Boolean = true,
    val orderNum: Int = 0,
    val weight: Int = 0,
    val lastUpdateTime: Long = 0L,
    val header: String? = null,
    val searchUrl: String? = null,
    val ruleSearch: String? = null,
    val searchListRule: String? = null,
    val searchNameRule: String? = null,
    val searchAuthorRule: String? = null,
    val searchCoverRule: String? = null,
    val searchIntroRule: String? = null,
    val searchUrlRule: String? = null,
    val searchKindRule: String? = null,
    val searchLastChapterRule: String? = null,
    val ruleBookName: String? = null,
    val ruleBookAuthor: String? = null,
    val ruleCoverUrl: String? = null,
    val ruleBookIntro: String? = null,
    val ruleBookKind: String? = null,
    val ruleBookLastChapter: String? = null,
    val ruleChapterList: String? = null,
    val ruleChapterName: String? = null,
    val ruleChapterUrl: String? = null,
    val ruleContent: String? = null,
    val ruleContentUrl: String? = null,
    val ruleNextPage: String? = null,
    val payAction: String? = null
)
