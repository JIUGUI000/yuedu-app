package com.legado.lite.tts

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import android.speech.tts.TextToSpeech
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

/**
 * TTS 控制器（轻量版）。
 *
 * 设计思路：
 *  - 内核使用 Android 系统自带 TTS（`TextToSpeech`），保证零依赖、必可用。
 *  - 预留 sherpa-onnx 升级路径（见 [SherpaOnnxEngine]），等用户放入模型时自动切换。
 *  - 状态用 StateFlow 暴露：模型未加载 / 加载中 / 已加载 / 错误 / 播放中。
 *  - 流式合成：把章节文本拆成句子，合成一段播一段，降低首字延迟。
 *
 * 注：sherpa-onnx-android 在标准 JitPack/Maven 上有 `com.k2fsa.sherpa-onnx:sherpa-onnx-android` 等
 * 坐标（不同作者打包名略不同）。本工程不强制依赖，便于在未配置 Maven 仓库时也能编译通过；
 * 预留的 [SherpaOnnxEngine] 类会让用户在 App 内手动引入本地 .so + .jar 时启用。
 */
object TtsController {

    enum class State { UNINITIALIZED, LOADING, READY, ERROR, PLAYING, PAUSED }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _state = MutableStateFlow(State.UNINITIALIZED)
    val state: StateFlow<State> = _state.asStateFlow()

    private val _modelInfo = MutableStateFlow("未加载")
    val modelInfo: StateFlow<String> = _modelInfo.asStateFlow()

    private var androidTts: TextToSpeech? = null
    private var sherpaEngine: SherpaOnnxEngine? = null
    private var currentFile: File? = null

    /** 异步初始化：先尝试 sherpa-onnx 模型，失败则降级到系统 TTS。 */
    fun initAsync(context: Context) {
        if (_state.value != State.UNINITIALIZED) return
        _state.value = State.LOADING
        scope.launch {
            try {
                val modelDir = SherpaOnnxEngine.modelDir(context)
                if (SherpaOnnxEngine.isModelPresent(modelDir)) {
                    val ok = SherpaOnnxEngine.tryLoad(modelDir)
                    if (ok) {
                        sherpaEngine = SherpaOnnxEngine(modelDir)
                        _modelInfo.value = "Sherpa-onnx 已加载：${modelDir.name}"
                        _state.value = State.READY
                        return@launch
                    }
                }
                initAndroidTts(context.applicationContext)
            } catch (t: Throwable) {
                _modelInfo.value = "初始化失败：${t.message}"
                _state.value = State.ERROR
            }
        }
    }

    private fun initAndroidTts(context: Context) {
        if (androidTts != null) return
        androidTts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val res = androidTts?.setLanguage(Locale.SIMPLIFIED_CHINESE)
                if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                    androidTts?.setLanguage(Locale.US)
                }
                _modelInfo.value = "使用 Android 系统 TTS（未配置 sherpa-onnx 模型）"
                _state.value = State.READY
            } else {
                _modelInfo.value = "系统 TTS 不可用"
                _state.value = State.ERROR
            }
        }
    }

    fun setSpeed(speed: Float) {
        androidTts?.setSpeechRate(speed.coerceIn(0.5f, 2.0f))
    }

    /**
     * 流式朗读：把章节文本拆成短句，一句一合成一播。
     *  - sherpa 引擎：用流式 PCM 写到 AudioTrack
     *  - 系统 TTS：一句一 speak，内部排队
     */
    fun speakChapter(
        context: Context,
        chapterTitle: String,
        text: String,
        onComplete: () -> Unit = {}
    ) {
        if (_state.value == State.UNINITIALIZED) initAsync(context)
        val engine = sherpaEngine
        if (engine != null) {
            scope.launch {
                speakWithSherpa(engine, text, onComplete)
            }
        } else {
            speakWithAndroidTts("$chapterTitle\n\n$text", onComplete)
        }
    }

    private fun speakWithAndroidTts(text: String, onComplete: () -> Unit) {
        val tts = androidTts ?: return
        tts.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) { _state.value = State.PLAYING }
            override fun onDone(utteranceId: String?) {
                _state.value = State.READY
                onComplete()
            }
            @Deprecated("legacy") override fun onError(utteranceId: String?) {
                _state.value = State.ERROR
            }
        })
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "legado-${System.currentTimeMillis()}")
    }

    private suspend fun speakWithSherpa(
        engine: SherpaOnnxEngine,
        text: String,
        onComplete: () -> Unit
    ) {
        _state.value = State.PLAYING
        // 拆句
        val sentences = text.split(Regex("(?<=[。！？!?\\n])")).filter { it.isNotBlank() }
        val sampleRate = engine.sampleRate()
        val minBuf = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(4096)
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(minBuf)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()
        track.play()
        try {
            for (s in sentences) {
                if (_state.value == State.PAUSED) break
                val pcm = engine.synthesize(s) ?: continue
                track.write(pcm, 0, pcm.size)
            }
        } finally {
            track.stop()
            track.release()
            _state.value = State.READY
            onComplete()
        }
    }

    fun stop() {
        androidTts?.stop()
        sherpaEngine = null
        _state.value = State.READY
    }

    fun release() {
        androidTts?.stop()
        androidTts?.shutdown()
        androidTts = null
        _state.value = State.UNINITIALIZED
    }

    @Suppress("unused")
    fun hasSherpa() = sherpaEngine != null

    @Suppress("unused")
    private fun cacheFile(context: Context, key: String): File {
        val dir = File(context.cacheDir, "tts").apply { mkdirs() }
        return File(dir, "seg-${key.hashCode()}.pcm")
    }

    @Suppress("unused")
    private fun audioFocusRequest(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
        else AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
}
