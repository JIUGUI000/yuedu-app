package com.legado.lite.data.source

import com.legado.lite.data.entity.BookSourceEntity
import com.legado.lite.data.http.WebFetcher
import com.legado.lite.domain.model.BookDetail
import com.legado.lite.domain.model.Chapter
import kotlinx.coroutines.runBlocking

/**
 * 书籍详情 & 目录 & 正文解析。
 */
class BookParser(
    private val fetcher: WebFetcher = WebFetcher()
) {

    /** 抓取并解析书籍详情。 */
    fun parseDetail(source: BookSourceEntity, bookUrl: String): BookDetail? {
        val resp = runBlocking { fetcher.fetchString(bookUrl, source) }
        if (!resp.isSuccessful) return null
        val baseUrl = resp.finalUrl
        val html = resp.body
        val name = RuleEngine.extractFirst(source.ruleBookName ?: "", html, baseUrl).trim()
        if (name.isEmpty()) return null
        return BookDetail(
            name = name,
            author = RuleEngine.extractFirst(source.ruleBookAuthor ?: "", html, baseUrl).trim(),
            cover = RuleEngine.extractFirst(source.ruleCoverUrl ?: "", html, baseUrl).trim()
                .ifEmpty { null }?.let { UrlUtils.absolute(baseUrl, it) },
            intro = RuleEngine.extractFirst(source.ruleBookIntro ?: "", html, baseUrl).trim().ifEmpty { null },
            kind = RuleEngine.extractFirst(source.ruleBookKind ?: "", html, baseUrl).trim().ifEmpty { null },
            lastChapter = RuleEngine.extractFirst(source.ruleBookLastChapter ?: "", html, baseUrl).trim().ifEmpty { null },
            tocUrl = bookUrl
        )
    }

    /** 抓取并解析目录（章节列表）。 */
    fun parseToc(source: BookSourceEntity, tocUrl: String): List<Chapter> {
        val resp = runBlocking { fetcher.fetchString(tocUrl, source) }
        if (!resp.isSuccessful) return emptyList()
        val baseUrl = resp.finalUrl
        val html = resp.body
        val listRule = source.ruleChapterList?.takeIf { it.isNotBlank() } ?: return emptyList()
        val nameRule = source.ruleChapterName?.takeIf { it.isNotBlank() } ?: "text"
        val urlRule = source.ruleChapterUrl?.takeIf { it.isNotBlank() } ?: "href"

        val items = RuleEngine.extractAll(listRule, html, baseUrl)
        val result = mutableListOf<Chapter>()
        for ((idx, item) in items.withIndex()) {
            val title = RuleEngine.applyRule(nameRule, item, baseUrl).trim()
            val rawUrl = RuleEngine.applyRule(urlRule, item, baseUrl).trim()
            if (title.isEmpty() || rawUrl.isEmpty()) continue
            result.add(Chapter(title = title, url = UrlUtils.absolute(baseUrl, rawUrl), index = idx))
        }
        return result
    }

    /** 抓取并解析章节正文。 */
    fun parseContent(source: BookSourceEntity, chapter: Chapter): String {
        val resp = runBlocking { fetcher.fetchString(chapter.url, source) }
        if (!resp.isSuccessful) return ""
        val baseUrl = resp.finalUrl
        val html = resp.body
        val rule = source.ruleContent?.takeIf { it.isNotBlank() } ?: return ""
        val text = RuleEngine.applyRule(rule, html, baseUrl)
        // 兼容：如果规则最后没有 @text，再做一次文本化
        return if (rule.endsWith("@text")) text else extractPlainText(text)
    }

    private fun extractPlainText(html: String): String {
        var s = html
        s = s.replace(Regex("(?is)<script.*?</script>"), "")
        s = s.replace(Regex("(?is)<style.*?</style>"), "")
        s = s.replace(Regex("(?i)</?(p|br|div|h[1-6])[^>]*>"), "\n")
        s = s.replace(Regex("<[^>]+>"), "")
        s = s.replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
        s = s.replace(Regex("[ \\t]+"), " ")
        s = s.replace(Regex("\n{2,}"), "\n\n")
        return s.trim()
    }
}
