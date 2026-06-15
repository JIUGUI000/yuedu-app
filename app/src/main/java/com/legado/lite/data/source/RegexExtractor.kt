package com.legado.lite.data.source

/**
 * 正则提取器。
 *
 * 用法：
 *  - "RegExp(模式)"  → 写完整正则（捕获组 1 为目标）
 *  - "##模式##"     → 隐式形式（捕获组 1 为目标）
 *  - 模式里可以写 "##" 表示 "##" 文本（通过占位符规避）。
 *
 * 若要返回整个匹配，模式用 `(?<full>...)` 命名组；否则取 group(1)。
 */
object RegexExtractor {

    fun first(rule: String, html: String, baseUrl: String): String {
        return match(rule, html, all = false).firstOrNull() ?: ""
    }

    fun all(rule: String, html: String, baseUrl: String): List<String> {
        return match(rule, html, all = true)
    }

    private fun match(rule: String, html: String, all: Boolean): List<String> {
        val (pattern, _) = parseRule(rule) ?: return emptyList()
        val rx = try { Regex(pattern, RegexOption.DOT_MATCHES_ALL) } catch (_: Throwable) { return emptyList() }
        val out = mutableListOf<String>()
        if (all) {
            for (m in rx.findAll(html)) out.add(extract(m))
        } else {
            val m = rx.find(html) ?: return emptyList()
            out.add(extract(m))
        }
        return out
    }

    private fun extract(m: MatchResult): String {
        return when {
            m.groups["full"] != null -> m.groups["full"]!!.value ?: ""
            m.groupValues.size > 1 && m.groupValues[1].isNotEmpty() -> m.groupValues[1]
            else -> m.value
        }
    }

    private fun parseRule(rule: String): Pair<String, String>? {
        if (rule.startsWith("RegExp(") && rule.endsWith(")")) {
            val body = rule.removePrefix("RegExp(").removeSuffix(")")
            return body to ""
        }
        if (rule.startsWith("regex(") && rule.endsWith(")")) {
            val body = rule.removePrefix("regex(").removeSuffix(")")
            return body to ""
        }
        if (rule.startsWith("##") && rule.length > 2) {
            // 单个 ## 表示模式，重组规则 ##模式##替换##模式##替换
            // 我们的简化方案里只取第一个模式（applyRule 里再做替换）
            val body = rule.removePrefix("##")
            // 如果还有 ##，只取第一段
            val idx = body.indexOf("##")
            val pattern = if (idx > 0) body.substring(0, idx) else body
            return pattern to ""
        }
        return null
    }
}
