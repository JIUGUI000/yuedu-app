package com.legado.lite.data

import androidx.room.TypeConverter

/**
 * Room 类型转换器。
 * 主要用于把 List<String> 之类的字段编码到单列里。
 */
class Converters {

    @TypeConverter
    fun fromList(list: List<String>?): String =
        list.orEmpty().joinToString(SEP_LIST)

    @TypeConverter
    fun toList(value: String?): List<String> =
        if (value.isNullOrEmpty()) emptyList() else value.split(SEP_LIST).filter { it.isNotEmpty() }

    @TypeConverter
    fun fromIntMap(map: Map<String, Int>?): String =
        map.orEmpty().entries.joinToString(SEP_ENTRY) { "${it.key}$SEP_KV${it.value}" }

    @TypeConverter
    fun toIntMap(value: String?): Map<String, Int> {
        if (value.isNullOrEmpty()) return emptyMap()
        return value.split(SEP_ENTRY)
            .filter { it.contains(SEP_KV) }
            .associate { entry ->
                val parts: List<String> = entry.split(SEP_KV, limit = 2)
                val k = parts.getOrNull(0).orEmpty()
                val v = parts.getOrNull(1)?.toIntOrNull() ?: 0
                k to v
            }
    }

    private companion object {
        const val SEP_LIST = "‖"
        const val SEP_ENTRY = "⁅⁆"
        const val SEP_KV = "≡"
    }
}
