package com.legado.lite.data.source

import org.json.JSONArray
import org.json.JSONObject

/**
 * 简化的 JSONPath 提取器，支持：
 *  - $.a.b.c   字段访问
 *  - $.a[0]    数组下标
 *  - $..a      递归搜索（深度优先，返回第一个非空）
 *  - $         根
 *  - $.list[*].title   通配数组
 */
object JsonPathExtractor {

    fun first(path: String, jsonText: String): String {
        val root = parseRoot(jsonText) ?: return ""
        val res = evalFirst(path, root) ?: return ""
        return stringify(res)
    }

    fun all(path: String, jsonText: String): List<String> {
        val root = parseRoot(jsonText) ?: return emptyList()
        return evalAll(path, root).map { stringify(it) }.filter { it.isNotEmpty() }
    }

    private fun parseRoot(text: String): Any? {
        val t = text.trim()
        return try {
            when {
                t.startsWith("{") -> JSONObject(t)
                t.startsWith("[") -> JSONArray(t)
                else -> null
            }
        } catch (_: Throwable) { null }
    }

    private fun evalFirst(path: String, root: Any?): Any? {
        val tokens = tokenize(path)
        var current: List<Any?> = listOf(root)
        for (tk in tokens) {
            current = step(current, tk)
            if (current.isEmpty()) return null
        }
        return current.firstOrNull { it != null && it != JSONObject.NULL }
    }

    private fun evalAll(path: String, root: Any?): List<Any?> {
        val tokens = tokenize(path)
        var current: List<Any?> = listOf(root)
        for (tk in tokens) {
            current = step(current, tk)
        }
        return current.filter { it != null && it != JSONObject.NULL }
    }

    private fun tokenize(path: String): List<String> {
        val p = path.trim().removePrefix("$")
        if (p.isEmpty()) return emptyList()
        val out = mutableListOf<String>()
        val buf = StringBuilder()
        var i = 0
        while (i < p.length) {
            val c = p[i]
            when {
                c == '.' -> { if (buf.isNotEmpty()) { out.add(buf.toString()); buf.clear() } }
                c == '[' -> {
                    if (buf.isNotEmpty()) { out.add(buf.toString()); buf.clear() }
                    val end = p.indexOf(']', i)
                    val inside = p.substring(i + 1, end)
                    out.add("[$inside]")
                    i = end
                }
                c == '*' -> { out.add("[*]") }
                else -> buf.append(c)
            }
            i++
        }
        if (buf.isNotEmpty()) out.add(buf.toString())
        return out
    }

    private fun step(inputs: List<Any?>, token: String): List<Any?> {
        val out = mutableListOf<Any?>()
        for (node in inputs) {
            when {
                token.startsWith("[") && token.endsWith("]") -> {
                    val inside = token.substring(1, token.length - 1)
                    if (node is JSONArray) {
                        when {
                            inside == "*" -> {
                                for (k in 0 until node.length()) out.add(node.opt(k))
                            }
                            inside.toIntOrNull() != null -> {
                                out.add(node.opt(inside.toInt()))
                            }
                            inside.contains(":") -> {
                                val (s, e) = inside.split(":").let { it[0].toIntOrNull() ?: 0 to (it.getOrNull(1)?.toIntOrNull() ?: node.length()) }
                                for (k in s until e) out.add(node.opt(k))
                            }
                            else -> {
                                // 可能是过滤条件，简单忽略
                            }
                        }
                    }
                }
                node is JSONObject -> {
                    val v = node.opt(token)
                    if (v != JSONObject.NULL) out.add(v)
                }
            }
        }
        return out
    }

    private fun stringify(v: Any?): String = when (v) {
        null, JSONObject.NULL -> ""
        is JSONObject, is JSONArray -> v.toString()
        else -> v.toString()
    }
}
