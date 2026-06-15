package com.legado.lite.ui.settings

import com.legado.lite.ui.LegadoViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsViewModel(app: android.app.Application) : LegadoViewModel(app) {

    val reader = container.preferencesRepository.reader

    fun exportSources(): String {
        // 同步获取当前所有书源 JSON
        var result = "[]"
        kotlinx.coroutines.runBlocking {
            val all = container.database.bookSourceDao().listAll()
            val list = all.map { e ->
                mapOf(
                    "bookSourceName" to e.name,
                    "bookSourceUrl" to e.url,
                    "bookSourceGroup" to e.group,
                    "enabled" to e.enabled,
                    "weight" to e.weight,
                    "searchUrl" to (e.searchUrl ?: e.ruleSearch),
                    "ruleSearch" to e.ruleSearch,
                    "ruleSearchList" to e.searchListRule,
                    "ruleSearchName" to e.searchNameRule,
                    "ruleSearchAuthor" to e.searchAuthorRule,
                    "ruleSearchCover" to e.searchCoverRule,
                    "ruleSearchIntro" to e.searchIntroRule,
                    "ruleSearchKind" to e.searchKindRule,
                    "ruleSearchLastChapter" to e.searchLastChapterRule,
                    "ruleSearchUrl" to e.searchUrlRule,
                    "ruleBookName" to e.ruleBookName,
                    "ruleBookAuthor" to e.ruleBookAuthor,
                    "ruleCoverUrl" to e.ruleCoverUrl,
                    "ruleBookIntro" to e.ruleBookIntro,
                    "ruleBookKind" to e.ruleBookKind,
                    "ruleBookLastChapter" to e.ruleBookLastChapter,
                    "ruleTocList" to e.ruleChapterList,
                    "ruleTocName" to e.ruleChapterName,
                    "ruleTocUrl" to e.ruleChapterUrl,
                    "ruleContent" to e.ruleContent
                )
            }
            result = kotlinx.serialization.json.Json {
                prettyPrint = true
                encodeDefaults = true
            }.encodeToString(
                kotlinx.serialization.builtins.ListSerializer(
                    kotlinx.serialization.json.JsonObject.serializer()
                ),
                list.map { kotlinx.serialization.json.JsonObject(it.mapValues { (_, v) ->
                    kotlinx.serialization.json.JsonPrimitive(v?.toString() ?: "")
                }) }
            )
        }
        return result
    }
}
