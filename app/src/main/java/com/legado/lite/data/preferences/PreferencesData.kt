package com.legado.lite.data.preferences

/** 阅读偏好。 */
data class ReaderPreferences(
    val fontSize: Int = 18,
    val lineSpacing: Float = 1.4f,
    val paragraphSpacing: Int = 12,
    val bgColor: String = "white",     // white / green / black / yellow
    val pageMode: String = "slide",    // slide / simulation / cover
    val brightness: Float = 0.4f,
    val keepScreenOn: Boolean = true,
    val volumeKeyPage: Boolean = true
)

/** TTS 偏好。 */
data class TtsPreferences(
    val modelId: String = "",          // 当前加载的模型 id
    val modelPath: String = "",        // 模型在 app 内路径
    val speed: Float = 1.0f,
    val pitch: Float = 1.0f,
    val voice: String = "default"
)

/** 应用偏好。 */
data class AppPreferences(
    val firstRun: Boolean = true,
    val importOnStart: Boolean = false
)
