package com.legado.lite.domain.repository

import com.legado.lite.data.preferences.AppPreferences
import com.legado.lite.data.preferences.ReaderPreferences
import com.legado.lite.data.preferences.TtsPreferences
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    val reader: Flow<ReaderPreferences>
    val tts: Flow<TtsPreferences>
    val app: Flow<AppPreferences>

    suspend fun setReader(prefs: ReaderPreferences)
    suspend fun setTts(prefs: TtsPreferences)
    suspend fun setApp(prefs: AppPreferences)
}
