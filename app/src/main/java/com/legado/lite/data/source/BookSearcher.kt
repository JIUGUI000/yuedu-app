package com.legado.lite.data.source

import com.legado.lite.data.entity.BookSourceEntity
import com.legado.lite.data.http.WebFetcher
import com.legado.lite.domain.model.SearchResult

/**
 * 跨书源搜索。
 *
 * 工作流程：
 *  1. 对每个启用的书源，用 searchUrl 模板拼接真实请求 URL
 *  2. 用 WebFetcher 抓取 HTML
 *  3. 用 searchListRule 匹配每条结果，再用 searchNameRule / searchAuthorRule 等解析
 *  4. 拼装成 SearchResult 返回
 *  5. 多书源并发抓取，任一失败不影响整体
 */
class BookSearcher(
    private val fetcher: WebFetcher = WebFetcher()
) {

    suspend fun search(
        sources: List<BookSourceEntity>,
        keyword: String,
        onSourceResult: suspend (SearchResult) -> Unit = {},
        onSourceError: suspend (BookSourceEntity, Throwable) -> Unit = { _, _ -> }
    ) {
        for (source in sources) {
            try {
                val results = searchOne(source, keyword)
                for (r in results) onSourceResult(r)
            } catch (t: Throwable) {
                onSourceError(source, t)
            }
        }
    }

    fun searchOne(source: BookSourceEntity, keyword: String): List<SearchResult> {
        val urlTemplate = source.searchUrl?.takeIf { it.isNotBlank() } ?: source.ruleSearch
        if (urlTemplate.isNullOrBlank()) return emptyList()
        val url = RuleEngine.fillPlaceholder(urlTemplate, keyword)
        val response = kotlinx.coroutines.runBlocking { fetcher.fetchString(url, source) }
        if (!response.isSuccessful) return emptyList()

        val baseUrl = response.finalUrl
        val html = response.body

        val listRule = source.searchListRule?.takeIf { it.isNotBlank() }
            ?: return emptyList()

        val items = RuleEngine.extractAll(listRule, html, baseUrl)
        val results = mutableListOf<SearchResult>()
        for (item in items) {
            val name = RuleEngine.extractFirst(source.searchNameRule ?: "", item, baseUrl).trim()
            if (name.isEmpty()) continue
            val author = RuleEngine.extractFirst(source.searchAuthorRule ?: "", item, baseUrl).trim()
            val cover = RuleEngine.extractFirst(source.searchCoverRule ?: "", item, baseUrl).trim().ifEmpty { null }
            val intro = RuleEngine.extractFirst(source.searchIntroRule ?: "", item, baseUrl).trim().ifEmpty { null }
            val kind = RuleEngine.extractFirst(source.searchKindRule ?: "", item, baseUrl).trim().ifEmpty { null }
            val lastChapter = RuleEngine.extractFirst(source.searchLastChapterRule ?: "", item, baseUrl).trim().ifEmpty { null }
            val rawUrl = RuleEngine.extractFirst(source.searchUrlRule ?: "", item, baseUrl).trim()
            if (rawUrl.isEmpty()) continue
            val bookUrl = UrlUtils.absolute(baseUrl, rawUrl)
            results.add(
                SearchResult(
                    sourceId = source.id,
                    sourceName = source.name,
                    name = name,
                    author = author,
                    cover = cover?.let { UrlUtils.absolute(baseUrl, it) },
                    intro = intro,
                    kind = kind,
                    lastChapter = lastChapter,
                    bookUrl = bookUrl
                )
            )
        }
        return results
    }
}
