package com.nbks.mi.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.*
import androidx.core.app.NotificationCompat
import com.nbks.mi.R
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

class TimerForegroundService : Service() {

    companion object {
        const val ACTION_START = "com.nbks.mi.timer.START"
        const val ACTION_PAUSE = "com.nbks.mi.timer.PAUSE"
        const val ACTION_RESUME = "com.nbks.mi.timer.RESUME"
        const val ACTION_STOP = "com.nbks.mi.timer.STOP"
        const val ACTION_TICK = "com.nbks.mi.timer.TICK"

        const val EXTRA_DURATION_MS = "extra_duration_ms"
        const val EXTRA_LABEL = "extra_label"
        const val EXTRA_SILENT = "extra_silent"
        const val EXTRA_REMAINING_MS = "extra_remaining_ms"

        const val CHANNEL_ID = "mi_timer_channel"
        const val NOTIFICATION_ID = 1001

        fun buildStartIntent(
            context: Context,
            durationMs: Long,
            label: String,
            isSilent: Boolean,
        ): Intent = Intent(context, TimerForegroundService::class.java).apply {
            action = ACTION_START
            putExtra(EXTRA_DURATION_MS, durationMs)
            putExtra(EXTRA_LABEL, label)
            putExtra(EXTRA_SILENT, isSilent)
        }
    }

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var totalDurationMs = 0L
    private var remainingMs = 0L
    private var isSilent = false
    private var label = ""
    private var timerJob: Job? = null
    private var isPaused = false

    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): TimerForegroundService = this@TimerForegroundService
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                totalDurationMs = intent.getLongExtra(EXTRA_DURATION_MS, 0L)
                label = intent.getStringExtra(EXTRA_LABEL) ?: ""
                isSilent = intent.getBooleanExtra(EXTRA_SILENT, false)
                remainingMs = totalDurationMs
                isPaused = false
                startTimer()
            }
            ACTION_PAUSE -> {
                isPaused = true
                timerJob?.cancel()
                updateNotification()
            }
            ACTION_RESUME -> {
                if (isPaused) {
                    isPaused = false
                    startTimer()
                }
            }
            ACTION_STOP -> {
                stopTimer()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun startTimer() {
        startForeground(NOTIFICATION_ID, buildNotification())
        timerJob?.cancel()
        timerJob = scope.launch {
            val intervalMs = 100L
            while (remainingMs > 0 && !isPaused) {
                delay(intervalMs)
                remainingMs = (remainingMs - intervalMs).coerceAtLeast(0L)
                broadcastTick(remainingMs)
                if (remainingMs % 1000 == 0L) updateNotification()
            }
            if (remainingMs == 0L && !isPaused) {
                onTimerComplete()
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        isPaused = false
        remainingMs = 0L
    }

    private fun onTimerComplete() {
        broadcastTick(0L)
        if (!isSilent) {
            triggerVibration()
        }
        val notification = buildCompletionNotification()
        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(NOTIFICATION_ID + 1, notification)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun triggerVibration() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        val pattern = longArrayOf(0, 500, 200, 500, 200, 1000)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }
    }

    private fun broadcastTick(remaining: Long) {
        sendBroadcast(Intent(ACTION_TICK).putExtra(EXTRA_REMAINING_MS, remaining))
    }

    private fun buildNotification(): Notification {
        val stopIntent = Intent(this, TimerForegroundService::class.java).apply { action = ACTION_STOP }
        val stopPi = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val pauseOrResumeIntent = Intent(this, TimerForegroundService::class.java).apply {
            action = if (isPaused) ACTION_RESUME else ACTION_PAUSE
        }
        val pausePi = PendingIntent.getService(this, 1, pauseOrResumeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val minutesLeft = TimeUnit.MILLISECONDS.toMinutes(remainingMs)
        val secondsLeft = TimeUnit.MILLISECONDS.toSeconds(remainingMs) % 60

        val title = if (label.isNotEmpty()) "タイマー: $label" else "タイマー"
        val content = "%02d:%02d 残り".format(minutesLeft, secondsLeft)

        val progress = if (totalDurationMs > 0)
            ((totalDurationMs - remainingMs) * 100 / totalDurationMs).toInt()
        else 0

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(content)
            .setProgress(100, progress, false)
            .addAction(
                android.R.drawable.ic_media_pause,
                if (isPaused) "再開" else "一時停止",
                pausePi,
            )
            .addAction(android.R.drawable.ic_delete, "停止", stopPi)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun buildCompletionNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("タイマー完了")
            .setContentText(if (label.isNotEmpty()) "$label が終了しました" else "タイマーが終了しました")
            .setAutoCancel(true)
            .setSilent(isSilent)
            .build()
    }

    private fun updateNotification() {
        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(NOTIFICATION_ID, buildNotification())
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "タイマー",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "タイマー実行中の通知"
            setSound(null, null)
        }
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
