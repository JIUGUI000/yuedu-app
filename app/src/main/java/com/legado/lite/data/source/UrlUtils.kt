package com.legado.lite.data.source

import java.net.URI
import java.net.URLDecoder
import java.net.URLEncoder

/**
 * URL 处理：把相对 URL 拼接成绝对 URL。
 * 当书源规则给出的链接是相对路径时用得上。
 */
object UrlUtils {

    fun absolute(base: String, maybeRelative: String): String {
        if (maybeRelative.isEmpty()) return base
        val lower = maybeRelative.lowercase()
        if (lower.startsWith("http://") || lower.startsWith("https://") ||
            lower.startsWith("javascript:") || lower.startsWith("mailto:")) {
            return maybeRelative
        }
        return try {
            val uri = URI(base)
            val resolved = uri.resolve(maybeRelative).toString()
            resolved
        } catch (_: Throwable) {
            try {
                URI(base).resolve(maybeRelative).toString()
            } catch (_: Throwable) {
                maybeRelative
            }
        }
    }

    fun encode(s: String): String = URLEncoder.encode(s, "UTF-8")
    fun decode(s: String): String = URLDecoder.decode(s, "UTF-8")
}
