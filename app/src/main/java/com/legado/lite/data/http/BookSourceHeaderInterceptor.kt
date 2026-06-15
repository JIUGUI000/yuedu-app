package com.legado.lite.data.http

import com.legado.lite.data.entity.BookSourceEntity
import okhttp3.Interceptor
import okhttp3.Response

/**
 * 为请求注入 User-Agent 等基础请求头。
 * 也支持书源自定义 header（JSON 字符串），例如：
 *  {"User-Agent": "Mozilla/5.0", "Referer": "https://www.xx.com"}
 */
class BookSourceHeaderInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val req = chain.request()
        val tag = req.tag(SourceTag::class.java)
        val source = tag?.source
        val builder = req.newBuilder()
            .header("User-Agent", defaultUA)
            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
        if (source != null) {
            applySourceHeaders(builder, source)
        }
        return chain.proceed(builder.build())
    }

    private fun applySourceHeaders(
        builder: okhttp3.Request.Builder,
        source: BookSourceEntity
    ) {
        val raw = source.header
        if (raw.isNullOrBlank()) return
        // 简易 JSON 解析：{"Key":"Value","Key2":"Value2"}
        val map = parseFlatJsonObject(raw) ?: return
        for ((k, v) in map) {
            if (k.isNotBlank()) builder.header(k, v)
        }
    }

    private fun parseFlatJsonObject(s: String): Map<String, String>? = try {
        val trimmed = s.trim().removePrefix("{").removeSuffix("}")
        if (trimmed.isEmpty()) return emptyMap()
        val map = LinkedHashMap<String, String>()
        val regex = Regex("\"([^\"\\\\]*(?:\\\\.[^\"\\\\]*)*)\"\\s*:\\s*\"([^\"\\\\]*(?:\\\\.[^\"\\\\]*)*)\"")
        for (m in regex.findAll(trimmed)) {
            val (k, v) = m.destructured
            map[k] = v.replace("\\\"", "\"").replace("\\\\", "\\")
        }
        map
    } catch (_: Throwable) { null }

    /** 通过 OkHttp tag 关联书源，用于自动注入自定义 header。 */
    class SourceTag(val source: BookSourceEntity)

    companion object {
        const val defaultUA = "Mozilla/5.0 (Linux; Android 12; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Mobile Safari/537.36 LegadoLite/1.0"
    }
}
