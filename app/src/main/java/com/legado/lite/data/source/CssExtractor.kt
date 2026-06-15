package com.legado.lite.data.source

/**
 * 极简 CSS 选择器（自实现，不依赖 jsoup）。
 *
 * 支持的选择器语法（自上而下，从父级到子级）：
 *  - tag            标签名（如 `div`、`a`、`li`）
 *  - .class         class 选择（可重复使用 `.a.b`）
 *  - #id            id 选择器
 *  - [attr=val]     属性精确匹配
 *  - [attr]         属性存在
 *  - 多个规则用空格分隔表示层级（简化版，按出现顺序在父元素范围内搜索）
 *  - @text          取元素纯文本（去 HTML 标签）
 *  - @html          取元素内部 HTML
 *  - @attrName      取元素的 attrName 属性值
 *  - @tag           取元素外层 HTML（含自身）
 *
 * 设计上仅实现"够用"的子集，不追求完整 CSS3 规范。
 */
object CssExtractor {

    private val tagRe = Regex("<([a-zA-Z][a-zA-Z0-9]*)([^>]*)>")
    private val anyTagRe = Regex("<[!/]?([a-zA-Z][a-zA-Z0-9]*)([^>]*)>")

    fun first(selector: String, html: String, baseUrl: String): String {
        val nodes = match(selector, html)
        val first = nodes.firstOrNull() ?: return ""
        return renderNode(first, "")
    }

    fun all(selector: String, html: String, baseUrl: String): List<String> {
        val nodes = match(selector, html)
        return nodes.map { renderNode(it, baseUrl) }
    }

    /** 匹配所有满足选择器的元素节点（外层 HTML 形式）。 */
    private fun match(selector: String, html: String): List<String> {
        val parts = selector.split(' ').filter { it.isNotEmpty() }
        if (parts.isEmpty()) return emptyList()
        // 自顶向下递归：每个 part 在前一个 part 的结果中继续搜索
        var current = listOf(html)
        for (p in parts) {
            val next = mutableListOf<String>()
            for (chunk in current) {
                next.addAll(scanChunk(p, chunk))
            }
            current = next
            if (current.isEmpty()) return emptyList()
        }
        return current
    }

    private fun scanChunk(part: String, html: String): List<String> {
        val parsed = parseSelectorPart(part) ?: return emptyList()
        val results = mutableListOf<String>()
        // 顺序遍历所有标签开闭对，匹配开标签
        tagRe.findAll(html).forEach { m ->
            val start = m.range.first
            val tagName = m.groupValues[1].lowercase()
            val attrs = parseAttrs(m.groupValues[2])
            if (tagName != parsed.tag) return@forEach
            if (parsed.id != null && attrs["id"] != parsed.id) return@forEach
            if (parsed.classes.isNotEmpty()) {
                val cls = (attrs["class"] ?: "").split(' ').filter { it.isNotEmpty() }
                if (!parsed.classes.all { it in cls }) return@forEach
            }
            for ((k, v) in parsed.attrEquals) {
                if (attrs[k] != v) return@forEach
            }
            for (k in parsed.attrExists) {
                if (k !in attrs) return@forEach
            }
            // 找匹配的闭合标签
            val end = findMatchingEnd(html, start, tagName)
            if (end > start) {
                results.add(html.substring(start, end)
                    .replace(Regex("(?i)<script.*?</script>"), "")
                    .replace(Regex("(?i)<style.*?</style>"), "")
                )
            }
        }
        return results
    }

    private fun findMatchingEnd(html: String, start: Int, tag: String): Int {
        // 在 start 之后查找对应的 </tag>，处理简单嵌套
        val openRe = Regex("<${tag}(\\s[^>]*)?>", RegexOption.IGNORE_CASE)
        val closeRe = Regex("</${tag}\\s*>", RegexOption.IGNORE_CASE)
        var depth = 1
        var pos = start + 1
        while (depth > 0) {
            val nextOpen = openRe.find(html, pos)?.range?.first ?: -1
            val nextClose = closeRe.find(html, pos)?.range?.first ?: -1
            when {
                nextClose < 0 -> return html.length
                nextOpen in 0 until nextClose -> { depth++; pos = nextOpen + 1 }
                else -> {
                    depth--
                    if (depth == 0) {
                        val endRange = closeRe.find(html, pos)!!.range
                        return endRange.last + 1
                    }
                    pos = nextClose + 1
                }
            }
        }
        return html.length
    }

    private data class ParsedSel(
        val tag: String,
        val id: String? = null,
        val classes: List<String> = emptyList(),
        val attrEquals: Map<String, String> = emptyMap(),
        val attrExists: List<String> = emptyList()
    )

    private fun parseSelectorPart(part: String): ParsedSel? {
        // 形如 a.tag#id.class[attr=v][rel]
        val sb = StringBuilder()
        var i = 0
        var tag = "*"
        var id: String? = null
        val classes = mutableListOf<String>()
        val attrEquals = LinkedHashMap<String, String>()
        val attrExists = mutableListOf<String>()
        var inBracket = false
        val buffer = StringBuilder()
        while (i < part.length) {
            val c = part[i]
            when {
                c == '[' -> { inBracket = true; buffer.clear() }
                c == ']' -> {
                    inBracket = false
                    val content = buffer.toString()
                    val eq = content.indexOf('=')
                    if (eq > 0) {
                        val k = content.substring(0, eq).trim()
                        val v = content.substring(eq + 1).trim().trim('\'').trim('"')
                        attrEquals[k] = v
                    } else {
                        attrExists.add(content.trim())
                    }
                }
                inBracket -> buffer.append(c)
                c == '#' -> {
                    val (name, rest) = readIdent(part, i + 1)
                    id = name
                    i += 1 + (part.length - i - 1 - rest.length)
                }
                c == '.' -> {
                    val (name, _) = readIdent(part, i + 1)
                    if (name.isNotEmpty()) classes.add(name)
                    i += 1 + name.length
                }
                else -> sb.append(c)
            }
            i++
        }
        if (sb.isNotEmpty()) tag = sb.toString().lowercase()
        return ParsedSel(tag, id, classes, attrEquals, attrExists)
    }

    private fun readIdent(s: String, from: Int): Pair<String, String> {
        val end = s.indexOfFirst { !it.isLetterOrDigit() && it != '-' && it != '_' && it != ':' }
        val stop = if (end < 0) s.length else end
        val name = if (from < s.length) s.substring(from, stop) else ""
        val rest = if (stop < s.length) s.substring(stop) else ""
        return name to rest
    }

    private fun parseAttrs(raw: String): Map<String, String> {
        val out = LinkedHashMap<String, String>()
        val rx = Regex("([a-zA-Z_:][a-zA-Z0-9_.:-]*)\\s*=\\s*(\"[^\"]*\"|'[^']*')")
        for (m in rx.findAll(raw)) {
            val k = m.groupValues[1].lowercase()
            var v = m.groupValues[2]
            if (v.startsWith('"') || v.startsWith('\'')) v = v.substring(1, v.length - 1)
            out[k] = v
        }
        return out
    }

    private fun renderNode(node: String, baseUrl: String): String {
        // 第一段提取完后，applyRule 会继续 @ 处理
        return node
    }
}
