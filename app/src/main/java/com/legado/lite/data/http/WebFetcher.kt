package com.legado.lite.data.http

import com.legado.lite.data.entity.BookSourceEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

/**
 * Web 抓取器：根据书源发出 HTTP 请求并返回响应内容。
 *  - 返回 WebFetch.Response（status / body / finalUrl / charset）
 *  - 解析时 charset 由 Content-Type / BOM 决定，默认 UTF-8
 */
class WebFetcher {

    suspend fun fetchString(
        url: String,
        source: BookSourceEntity? = null,
        method: String = "GET",
        body: String? = null,
        extraHeaders: Map<String, String> = emptyMap()
    ): Response = withContext(Dispatchers.IO) {
        val client = HttpClient.get()
        val builder = Request.Builder().url(url)
        source?.let { builder.tag(BookSourceHeaderInterceptor.SourceTag(it)) }
        extraHeaders.forEach { (k, v) -> builder.header(k, v) }
        val req = when (method.uppercase()) {
            "POST" -> builder.post(okhttp3.RequestBody.create(null, body ?: "")).build()
            else -> builder.get().build()
        }
        client.newCall(req).execute().use { resp ->
            val charset = resp.charsetFromHeader() ?: Charsets.UTF_8
            val text = resp.body?.byteString()?.string(charset) ?: ""
            Response(
                status = resp.code,
                body = text,
                finalUrl = resp.request.url.toString(),
                charset = charset.name()
            )
        }
    }

    private fun okhttp3.Response.charsetFromHeader(): java.nio.charset.Charset? {
        val ct = header("Content-Type") ?: return null
        val m = Regex("charset=([\\w-]+)", RegexOption.IGNORE_CASE).find(ct) ?: return null
        return try { java.nio.charset.Charset.forName(m.groupValues[1]) } catch (_: Throwable) { null }
    }

    data class Response(
        val status: Int,
        val body: String,
        val finalUrl: String,
        val charset: String
    ) {
        val isSuccessful: Boolean get() = status in 200..299
    }

    class FetchException(message: String, cause: Throwable? = null) : IOException(message, cause)
}
