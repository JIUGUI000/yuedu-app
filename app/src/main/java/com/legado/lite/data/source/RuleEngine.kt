package com.legado.lite.data.source

/**
 * 书源规则工具类。
 *
 * 借鉴 Legado 规则语法（简化版），支持：
 *  1. **占位符**：`{{key}}` 在搜索 URL 中可被替换为查询字符串（已做 URL 编码）。
 *  2. **候选规则**：`||` 分隔的多条规则，依次尝试，取首个非空结果。
 *  3. **多段管道**：`@` 分隔多段，对每段做「提取 / 替换」。
 *      - 段 0 是提取规则（CSS / XPath / JSONPath / RegExp）
 *      - 段 1+ 是后处理：`text` / `html` / `tag` / `attr(属性名)` / `##正则##替换`
 *  4. **替换**：`##regex##replacement` 形式（replacement 中 `$1` 为分组）。
 *  5. **正则模式**：以 `RegExp(` 开头或 `##` 开头。
 *  6. **XPath**：以 `/` 开头，简化支持。
 *  7. **CSS**：常规 `tag.class#id` 形式。
 *  8. **JSONPath**：以 `$.` 开头。
 */
object RuleEngine {

    fun fillPlaceholder(template: String, query: String): String {
        if (template.isEmpty()) return template
        val encoded = java.net.URLEncoder.encode(query, "UTF-8")
        return template
            .replace("{{key}}", encoded)
            .replace("{{query}}", query)
    }

    fun extractFirst(rule: String, html: String, baseUrl: String = ""): String {
        for (candidate in rule.splitCandidates()) {
            val r = applyRule(candidate.trim(), html, baseUrl)
            if (r.isNotEmpty()) return r
        }
        return ""
    }

    fun extractAll(rule: String, html: String, baseUrl: String = ""): List<String> {
        for (candidate in rule.splitCandidates()) {
            val r = applyRuleAll(candidate.trim(), html, baseUrl)
            if (r.isNotEmpty()) return r
        }
        return emptyList()
    }

    fun applyRule(rule: String, html: String, baseUrl: String = ""): String {
        if (rule.isEmpty()) return ""
        val parts = rule.split('@')
        var current: String
        // 段 0：提取
        current = extractSingle(parts[0], html, baseUrl)
        if (current.isEmpty()) return ""
        // 段 1+：后处理
        for (i in 1 until parts.size) {
            val seg = parts[i]
            if (seg.isEmpty()) continue
            current = applyPostProcess(seg, current)
            if (current.isEmpty()) return ""
        }
        return current
    }

    fun applyRuleAll(rule: String, html: String, baseUrl: String = ""): List<String> {
        if (rule.isEmpty()) return emptyList()
        val parts = rule.split('@')
        if (parts.isEmpty()) return emptyList()
        var list: List<String> = extractList(parts[0], html, baseUrl)
        if (list.isEmpty()) return emptyList()
        for (i in 1 until parts.size) {
            val seg = parts[i]
            if (seg.isEmpty()) continue
            list = list.map { applyPostProcess(seg, it) }.filter { it.isNotEmpty() }
            if (list.isEmpty()) return emptyList()
        }
        return list
    }

    private fun extractSingle(rule: String, html: String, baseUrl: String): String {
        return when {
            rule.isEmpty() -> html
            rule.startsWith("RegExp(") && rule.endsWith(")") -> {
                val pat = rule.removePrefix("RegExp(").removeSuffix(")")
                RegexExtractor.first(pat, html, baseUrl)
            }
            rule.startsWith("regex(") && rule.endsWith(")") -> {
                val pat = rule.removePrefix("regex(").removeSuffix(")")
                RegexExtractor.first(pat, html, baseUrl)
            }
            rule.startsWith("##") -> RegexExtractor.first(rule, html, baseUrl)
            rule.startsWith("$") -> JsonPathExtractor.first(rule, html)
            rule.startsWith("/") -> XPathExtractor.first(rule, html)
            rule.startsWith("css:") -> CssExtractor.first(rule.removePrefix("css:"), html, baseUrl)
            else -> CssExtractor.first(rule, html, baseUrl)
        }
    }

    private fun extractList(rule: String, html: String, baseUrl: String): List<String> {
        return when {
            rule.isEmpty() -> listOf(html)
            rule.startsWith("RegExp(") && rule.endsWith(")") -> {
                val pat = rule.removePrefix("RegExp(").removeSuffix(")")
                RegexExtractor.all(pat, html, baseUrl)
            }
            rule.startsWith("regex(") && rule.endsWith(")") -> {
                val pat = rule.removePrefix("regex(").removeSuffix(")")
                RegexExtractor.all(pat, html, baseUrl)
            }
            rule.startsWith("##") -> RegexExtractor.all(rule, html, baseUrl)
            rule.startsWith("$") -> JsonPathExtractor.all(rule, html)
            rule.startsWith("/") -> XPathExtractor.all(rule, html)
            rule.startsWith("css:") -> CssExtractor.all(rule.removePrefix("css:"), html, baseUrl)
            else -> CssExtractor.all(rule, html, baseUrl)
        }
    }

    /**
     * 处理 @ 之后的后处理段。
     *  - `text` → 提取纯文本（去 HTML 标签）
     *  - `html` → 保留 HTML
     *  - `tag`  → 保留 outerHTML
     *  - `attr(name)` → 取属性 name
     *  - 含 `##` → 正则替换
     *  - 否则视为属性名
     */
    private fun applyPostProcess(seg: String, input: String): String {
        if (seg.isEmpty()) return input
        return when {
            seg == "text" -> extractText(input)
            seg == "html" -> input
            seg == "tag" -> input
            seg.startsWith("attr(") && seg.endsWith(")") -> {
                val name = seg.substring(5, seg.length - 1).trim()
                extractAttr(input, name)
            }
            seg.startsWith("##") -> applyRegexReplace(seg, input)
            else -> extractAttr(input, seg)
        }
    }

    private fun extractText(html: String): String {
        // 去掉 script/style
        var s = html
        s = s.replace(Regex("(?is)<script.*?</script>"), "")
        s = s.replace(Regex("(?is)<style.*?</style>"), "")
        // 替换常见块级标签为换行
        s = s.replace(Regex("(?i)</?(p|br|div|h[1-6]|li|tr|td|th|article|section)[^>]*>"), "\n")
        // 去掉其余标签
        s = s.replace(Regex("<[^>]+>"), "")
        // HTML 实体反转
        s = htmlUnescape(s)
        // 合并连续空白
        s = s.replace(Regex("[ \\t]+"), " ")
        s = s.replace(Regex("\n{2,}"), "\n\n")
        return s.trim()
    }

    private fun extractAttr(html: String, attrName: String): String {
        // html 是元素外层，尝试提取属性
        val m = Regex("(?i)\\b${Regex.escape(attrName)}\\s*=\\s*\"([^\"]*)\"").find(html)
            ?: Regex("(?i)\\b${Regex.escape(attrName)}\\s*=\\s*'([^']*)'").find(html)
        return m?.groupValues?.get(1) ?: ""
    }

    private fun applyRegexReplace(seg: String, input: String): String {
        // 形如 ##pattern##replacement##pattern2##replacement2##
        val body = seg.removePrefix("##").removeSuffix("##")
        val chunks = body.split("##")
        var s = input
        var i = 0
        while (i + 1 < chunks.size) {
            val pattern = chunks[i]
            val replacement = chunks[i + 1]
            try {
                s = Regex(pattern).replace(s, replacement)
            } catch (_: Throwable) {
                // 忽略
            }
            i += 2
        }
        return s
    }

    private fun htmlUnescape(s: String): String = s
        .replace("&nbsp;", " ")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .replace("&apos;", "'")

    private fun String.splitCandidates(): List<String> =
        if (this.contains("||")) this.split("||") else listOf(this)
}
