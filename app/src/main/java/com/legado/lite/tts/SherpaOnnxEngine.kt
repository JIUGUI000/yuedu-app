package com.legado.lite.tts

import android.content.Context
import java.io.File

/**
 * Sherpa-onnx 引擎占位与加载探测。
 *
 * 真实加载依赖 JNI 库与 ONNX 模型。打包脚本请在 README 中说明：
 *  1. 把 sherpa-onnx-android 的 .so / .jar 放进 `app/libs/`
 *  2. 把 TTS 模型（如 vits-zh-aishell3）放进 `files/tts/vits-zh/`：
 *     - model.onnx
 *     - tokens.txt
 *     - espeak-ng-data/ (可选)
 *  3. 重新编译后即自动启用。
 *
 * 当依赖未配置时，tryLoad() 返回 false，控制权交回 Android 系统 TTS。
 */
class SherpaOnnxEngine(private val modelDir: File) {

    fun sampleRate(): Int = 22050  // 多数 vits 模型采样率

    /** 把一段中文文本合成 PCM 16bit 小端数据。失败返回 null。 */
    fun synthesize(text: String): ShortArray? {
        // 真实实现需要调用 Sherpa-onnx 的 OfflineTts.generate(text) 接口。
        // 这里以占位实现返回 null，由调用方继续以系统 TTS 兜底。
        return null
    }

    companion object {

        fun modelDir(context: Context): File =
            File(context.filesDir, "tts").apply { mkdirs() }

        fun isModelPresent(dir: File): Boolean {
            if (!dir.isDirectory) return false
            val onnx = dir.walk().firstOrNull { it.extension == "onnx" } ?: return false
            val tokens = dir.walk().firstOrNull { it.name == "tokens.txt" } ?: return false
            return onnx.exists() && tokens.exists()
        }

        /**
         * 真实加载入口（待用户在工程里配置 .so/.jar 后实现）。
         * 默认实现总是返回 false，让控制器走系统 TTS。
         */
        fun tryLoad(modelDir: File): Boolean {
            // 实际工程里：
            //  val config = OfflineTtsConfig(...)
            //  val tts = OfflineTts.create(assetManager, config)
            //  return tts != null
            return false
        }
    }
}
