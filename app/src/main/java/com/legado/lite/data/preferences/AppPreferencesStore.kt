package com.legado.lite.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences as DpPrefs
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.legado.lite.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("legado_prefs")

class AppPreferencesStore(private val context: Context) : PreferencesRepository {

    private object Keys {
        val FONT_SIZE = intPreferencesKey("reader.fontSize")
        val LINE_SPACING = floatPreferencesKey("reader.lineSpacing")
        val PARA_SPACING = intPreferencesKey("reader.paragraphSpacing")
        val BG_COLOR = stringPreferencesKey("reader.bgColor")
        val PAGE_MODE = stringPreferencesKey("reader.pageMode")
        val BRIGHTNESS = floatPreferencesKey("reader.brightness")
        val KEEP_SCREEN_ON = booleanPreferencesKey("reader.keepScreenOn")
        val VOLUME_KEY_PAGE = booleanPreferencesKey("reader.volumeKeyPage")

        val TTS_MODEL_ID = stringPreferencesKey("tts.modelId")
        val TTS_MODEL_PATH = stringPreferencesKey("tts.modelPath")
        val TTS_SPEED = floatPreferencesKey("tts.speed")
        val TTS_PITCH = floatPreferencesKey("tts.pitch")
        val TTS_VOICE = stringPreferencesKey("tts.voice")

        val APP_FIRST_RUN = booleanPreferencesKey("app.firstRun")
        val APP_IMPORT_ON_START = booleanPreferencesKey("app.importOnStart")
    }

    override val reader: Flow<ReaderPreferences> = context.dataStore.data.map { it.toReader() }
    override val tts: Flow<TtsPreferences> = context.dataStore.data.map { it.toTts() }
    override val app: Flow<AppPreferences> = context.dataStore.data.map { it.toApp() }

    override suspend fun setReader(prefs: ReaderPreferences) {
        context.dataStore.edit {
            it[Keys.FONT_SIZE] = prefs.fontSize
            it[Keys.LINE_SPACING] = prefs.lineSpacing
            it[Keys.PARA_SPACING] = prefs.paragraphSpacing
            it[Keys.BG_COLOR] = prefs.bgColor
            it[Keys.PAGE_MODE] = prefs.pageMode
            it[Keys.BRIGHTNESS] = prefs.brightness
            it[Keys.KEEP_SCREEN_ON] = prefs.keepScreenOn
            it[Keys.VOLUME_KEY_PAGE] = prefs.volumeKeyPage
        }
    }

    override suspend fun setTts(prefs: TtsPreferences) {
        context.dataStore.edit {
            it[Keys.TTS_MODEL_ID] = prefs.modelId
            it[Keys.TTS_MODEL_PATH] = prefs.modelPath
            it[Keys.TTS_SPEED] = prefs.speed
            it[Keys.TTS_PITCH] = prefs.pitch
            it[Keys.TTS_VOICE] = prefs.voice
        }
    }

    override suspend fun setApp(prefs: AppPreferences) {
        context.dataStore.edit {
            it[Keys.APP_FIRST_RUN] = prefs.firstRun
            it[Keys.APP_IMPORT_ON_START] = prefs.importOnStart
        }
    }

    private fun DpPrefs.toReader() = ReaderPreferences(
        fontSize = this[Keys.FONT_SIZE] ?: 18,
        lineSpacing = this[Keys.LINE_SPACING] ?: 1.4f,
        paragraphSpacing = this[Keys.PARA_SPACING] ?: 12,
        bgColor = this[Keys.BG_COLOR] ?: "white",
        pageMode = this[Keys.PAGE_MODE] ?: "slide",
        brightness = this[Keys.BRIGHTNESS] ?: 0.4f,
        keepScreenOn = this[Keys.KEEP_SCREEN_ON] ?: true,
        volumeKeyPage = this[Keys.VOLUME_KEY_PAGE] ?: true
    )

    private fun DpPrefs.toTts() = TtsPreferences(
        modelId = this[Keys.TTS_MODEL_ID] ?: "",
        modelPath = this[Keys.TTS_MODEL_PATH] ?: "",
        speed = this[Keys.TTS_SPEED] ?: 1.0f,
        pitch = this[Keys.TTS_PITCH] ?: 1.0f,
        voice = this[Keys.TTS_VOICE] ?: "default"
    )

    private fun DpPrefs.toApp() = AppPreferences(
        firstRun = this[Keys.APP_FIRST_RUN] ?: true,
        importOnStart = this[Keys.APP_IMPORT_ON_START] ?: false
    )
}
