package com.legado.lite

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.legado.lite.data.AppContainer
import com.legado.lite.data.DefaultAppContainer
import com.legado.lite.tts.TtsController

/**
 * 应用入口。
 * 负责：
 *  - 初始化全局 AppContainer（依赖容器）
 *  - 创建 TTS 通知渠道
 *  - 异步预加载 TTS 引擎（不阻塞 UI）
 */
class LegadoApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        container = DefaultAppContainer(this)
        createNotificationChannel()
        // 后台预初始化 TTS 引擎
        TtsController.initAsync(applicationContext)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_TTS,
                "听书播放",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "本地 TTS 听书播放通知"
                setShowBadge(false)
            }
            val mgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            mgr.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_TTS = "tts_playback"
        private lateinit var instance: LegadoApp
        fun get(): LegadoApp = instance
    }
}
