package com.legado.lite.data.repo

import android.content.Context
import com.legado.lite.data.dao.BookSourceDao
import com.legado.lite.data.entity.BookSourceEntity
import com.legado.lite.domain.repository.BookSourceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class BookSourceJson(
    val bookSourceName: String,
    val bookSourceUrl: String,
    val bookSourceGroup: String? = null,
    val enabled: Boolean? = true,
    val enabledExplore: Boolean? = null,
    val weight: Int = 0,
    val lastUpdateTime: Long = 0L,
    val header: String? = null,
    val searchUrl: String? = null,
    val ruleSearch: String? = null,
    val ruleSearchList: String? = null,
    val ruleSearchName: String? = null,
    val ruleSearchAuthor: String? = null,
    val ruleSearchCover: String? = null,
    val ruleSearchIntro: String? = null,
    val ruleSearchKind: String? = null,
    val ruleSearchLastChapter: String? = null,
    val ruleSearchUrl: String? = null,
    val ruleBookName: String? = null,
    val ruleBookAuthor: String? = null,
    val ruleCoverUrl: String? = null,
    val ruleBookIntro: String? = null,
    val ruleBookKind: String? = null,
    val ruleBookLastChapter: String? = null,
    val ruleTocList: String? = null,
    val ruleTocName: String? = null,
    val ruleTocUrl: String? = null,
    val ruleContent: String? = null
)

class BookSourceRepositoryImpl(
    private val dao: BookSourceDao,
    @Suppress("UNUSED_PARAMETER") private val context: Context
) : BookSourceRepository {

    private val jsonParser = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    override fun observeAll(): Flow<List<BookSourceEntity>> = dao.observeAll()

    override suspend fun listEnabled(): List<BookSourceEntity> = dao.listEnabled()

    override suspend fun findById(id: Long): BookSourceEntity? = dao.findById(id)

    override suspend fun upsert(source: BookSourceEntity): Long = dao.upsert(source)

    override suspend fun upsertAll(sources: List<BookSourceEntity>) = dao.upsertAll(sources)

    override suspend fun update(source: BookSourceEntity) = dao.update(source)

    override suspend fun delete(source: BookSourceEntity) = dao.delete(source)

    override suspend fun deleteById(id: Long) = dao.deleteById(id)

    override suspend fun clear() = dao.clear()

    /**
     * 导入书源 JSON。支持：
     *  1. 单一对象 {...}
     *  2. 数组 [...]
     *  3. 每行一个 JSON 对象的 LEGADO 旧格式（多行 JSON）
     */
    override suspend fun importFromJson(json: String): Int {
        val sources = parseJson(json)
        if (sources.isNotEmpty()) dao.upsertAll(sources)
        return sources.size
    }

    private fun parseJson(raw: String): List<BookSourceEntity> {
        val text = raw.trim()
        if (text.isEmpty()) return emptyList()
        val list = mutableListOf<BookSourceEntity>()
        val direct = tryParseAsList(text)
        if (direct != null) {
            return direct.mapIndexed { idx, it -> it.toEntity(orderNum = idx) }
        }
        // 多行 JSON
        for (line in text.split('\n')) {
            val t = line.trim().removeSuffix(",")
            if (t.isEmpty() || t == "[" || t == "]" || t == "{") continue
            if (!t.startsWith("{")) continue
            try {
                val obj = jsonParser.decodeFromString(BookSourceJson.serializer(), t)
                list.add(obj.toEntity(orderNum = list.size))
            } catch (_: Throwable) {
                // 忽略单行错误
            }
        }
        return list
    }

    private fun tryParseAsList(text: String): List<BookSourceJson>? {
        return try {
            when {
                text.startsWith("[") -> jsonParser.decodeFromString(
                    kotlinx.serialization.builtins.ListSerializer(BookSourceJson.serializer()),
                    text
                )
                text.startsWith("{") -> listOf(jsonParser.decodeFromString(BookSourceJson.serializer(), text))
                else -> null
            }
        } catch (_: Throwable) { null }
    }

    private fun BookSourceJson.toEntity(orderNum: Int): BookSourceEntity {
        return BookSourceEntity(
            name = bookSourceName,
            url = bookSourceUrl,
            group = bookSourceGroup,
            enabled = enabled ?: true,
            orderNum = orderNum,
            weight = weight,
            lastUpdateTime = lastUpdateTime,
            header = header,
            searchUrl = searchUrl ?: ruleSearch,
            ruleSearch = ruleSearch,
            searchListRule = ruleSearchList,
            searchNameRule = ruleSearchName,
            searchAuthorRule = ruleSearchAuthor,
            searchCoverRule = ruleSearchCover,
            searchIntroRule = ruleSearchIntro,
            searchUrlRule = ruleSearchUrl,
            searchKindRule = ruleSearchKind,
            searchLastChapterRule = ruleSearchLastChapter,
            ruleBookName = ruleBookName,
            ruleBookAuthor = ruleBookAuthor,
            ruleCoverUrl = ruleCoverUrl,
            ruleBookIntro = ruleBookIntro,
            ruleBookKind = ruleBookKind,
            ruleBookLastChapter = ruleBookLastChapter,
            ruleChapterList = ruleTocList,
            ruleChapterName = ruleTocName,
            ruleChapterUrl = ruleTocUrl,
            ruleContent = ruleContent
        )
    }

    override suspend fun exportToJson(): String {
        // 直接从 DAO 同步查询 - 通过新增 listAll 方法
        val all = dao.listAll()
        val arr = kotlinx.serialization.builtins.ListSerializer(BookSourceJson.serializer())
        val list = all.map { it.toJson() }
        return jsonParser.encodeToString(arr, list)
    }

    private fun BookSourceEntity.toJson(): BookSourceJson = BookSourceJson(
        bookSourceName = name,
        bookSourceUrl = url,
        bookSourceGroup = group,
        enabled = enabled,
        weight = weight,
        lastUpdateTime = lastUpdateTime,
        header = header,
        searchUrl = searchUrl ?: ruleSearch,
        ruleSearch = ruleSearch,
        ruleSearchList = searchListRule,
        ruleSearchName = searchNameRule,
        ruleSearchAuthor = searchAuthorRule,
        ruleSearchCover = searchCoverRule,
        ruleSearchIntro = searchIntroRule,
        ruleSearchKind = searchKindRule,
        ruleSearchLastChapter = searchLastChapterRule,
        ruleSearchUrl = searchUrlRule,
        ruleBookName = ruleBookName,
        ruleBookAuthor = ruleBookAuthor,
        ruleCoverUrl = ruleCoverUrl,
        ruleBookIntro = ruleBookIntro,
        ruleBookKind = ruleBookKind,
        ruleBookLastChapter = ruleBookLastChapter,
        ruleTocList = ruleChapterList,
        ruleTocName = ruleChapterName,
        ruleTocUrl = ruleChapterUrl,
        ruleContent = ruleContent
    )
}
