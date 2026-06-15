package com.legado.lite.tts

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.media.AudioAttributes
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.legado.lite.LegadoApp
import com.legado.lite.MainActivity
import com.legado.lite.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 听书前台服务（占位实现，可在此加入 MediaSession + ExoPlayer）。
 *
 * 当前版本通过通知呈现一个"正在听书"的提示，便于权限上更稳定，
 * 真实合成 / 播放逻辑仍由 [TtsController] 持有。
 */
class TtsPlaybackService : LifecycleService() {

    private val _currentChapter = MutableStateFlow<String?>(null)
    val currentChapter: StateFlow<String?> = _currentChapter.asStateFlow()

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, buildNotification("准备听书…"))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_PLAY -> {
                val title = intent.getStringExtra(EXTRA_CHAPTER) ?: "未知章节"
                _currentChapter.value = title
                startForeground(NOTIFICATION_ID, buildNotification("正在朗读：$title"))
            }
            ACTION_STOP -> {
                TtsController.stop()
                _currentChapter.value = null
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        TtsController.stop()
    }

    private fun buildNotification(content: String): Notification {
        val pi = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(this, LegadoApp.CHANNEL_TTS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(content)
            .setOngoing(true)
            .setContentIntent(pi)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .build()
    }

    companion object {
        const val NOTIFICATION_ID = 1001
        const val ACTION_PLAY = "com.legado.lite.tts.PLAY"
        const val ACTION_STOP = "com.legado.lite.tts.STOP"
        const val EXTRA_CHAPTER = "chapter"

        fun startPlay(context: android.content.Context, chapterTitle: String) {
            val i = Intent(context, TtsPlaybackService::class.java).apply {
                action = ACTION_PLAY
                putExtra(EXTRA_CHAPTER, chapterTitle)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(i)
            } else {
                context.startService(i)
            }
        }

        fun stop(context: android.content.Context) {
            val i = Intent(context, TtsPlaybackService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(i)
        }
    }
}
