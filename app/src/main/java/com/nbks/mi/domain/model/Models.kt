package com.nbks.mi.domain.model

import java.time.LocalDateTime
import java.time.LocalTime



// ─────────────────────────────────────────────
// メモ
// ─────────────────────────────────────────────
data class Memo(
    val id: Long = 0,
    val title: String,
    val content: String,
    val colorIndex: Int = 0,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val isPinned: Boolean = false,
)

// ─────────────────────────────────────────────
// タイマー
// ─────────────────────────────────────────────
data class TimerPreset(
    val id: Long = 0,
    val name: String,
    val durationMillis: Long,
    val isSilent: Boolean = false,
    val isDaily: Boolean = false,
    val dailyHour: Int = 0,
    val dailyMinute: Int = 0,
)

// 時刻アラームウィジェット用: 今日のプリセットまでの残り時間
data class DailyAlarmStatus(
    val preset: TimerPreset,
    val remainingMs: Long,  // 負 = 今日は既に過ぎた
)

data class TimerState(
    val presetId: Long? = null,
    val label: String = "",
    val durationMillis: Long = 0L,
    val remainingMillis: Long = 0L,
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val isSilent: Boolean = false,
    val isFinished: Boolean = false,
) {
    val progress: Float
        get() = if (durationMillis == 0L) 1f else 1f - (remainingMillis.toFloat() / durationMillis.toFloat())
}

// ─────────────────────────────────────────────
// ウィジェット設定
// ─────────────────────────────────────────────
enum class WidgetType(val jaName: String, val enName: String, val iconName: String) {
    CLOCK("時計", "Clock", "schedule"),
    CALENDAR("カレンダー", "Calendar", "calendar_month"),
    MEMO("メモ", "Memo", "note"),
    TIMER("タイマー", "Timer", "timer"),
    DAILY_ALARM("時刻アラーム", "Daily Alarm", "alarm"),
    PROGRESS("進捗バー", "Progress", "trending_up"),
    AI("AI チャット", "AI Chat", "smart_toy"),
    DAILY_TASKS("日課", "Daily Tasks", "repeat"),
    WEBHOOK_BUTTONS("Webhookボタン", "Webhook", "send"),
    IMAGE("画像", "Image", "image"),
    ;

    val displayName: String
        get() = if (java.util.Locale.getDefault().language == "ja") jaName else enName
}

data class WidgetConfig(
    val id: Long = 0,
    val type: WidgetType,
    val positionX: Float = 0f,
    val positionY: Float = 0f,
    val widthDp: Int = 300,
    val heightDp: Int = 200,
    val isVisible: Boolean = true,
    val zIndex: Int = 0,
    val customSettings: String = "{}",
    val title: String = "",
)

// ─────────────────────────────────────────────
// AI チャット
// ─────────────────────────────────────────────
data class AiMessage(
    val id: Long = 0,
    val role: String, // "user" or "assistant"
    val content: String,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val isError: Boolean = false,
)

// ─────────────────────────────────────────────
// 画面暗くするスケジュール
// ─────────────────────────────────────────────
data class ScreenSchedule(
    val id: Long = 0,
    val label: String = "",
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val brightness: Float = 0f, // 0.0 = 真っ黒, 1.0 = 最大
    val isEnabled: Boolean = true,
    // bit0=月, bit1=火, bit2=水, bit3=木, bit4=金, bit5=土, bit6=日  0x7F=全日
    val dayOfWeekBitmask: Int = 0x7F,
)

// ─────────────────────────────────────────────
// 進捗
// ─────────────────────────────────────────────
data class ProgressData(
    val progress: Float,       // 0.0 〜 1.0
    val label: String,
    val sublabel: String = "",
)

// ─────────────────────────────────────────────
// アプリ設定
// ─────────────────────────────────────────────
data class AppSettings(
    val isDarkMode: Boolean = true,
    val useDynamicColor: Boolean = false,
    val clockStyle: ClockStyle = ClockStyle.DIGITAL,
    val wallpaperUri: String = "",
    val wallpaperDimAlpha: Float = 0.3f,
    val lmStudioBaseUrl: String = "http://localhost:1234",
    val lmStudioModel: String = "local-model",
    val lmStudioMaxTokens: Int = 2048,
    val discordBotToken: String = "",
    val discordGuildId: String = "",
    val discordChannelIds: String = "",  // comma separated
    val keepScreenOn: Boolean = false,
    val scheduleScreenDim: Boolean = false,
    val primaryColorSeed: Long = 0xFF6650A4,
    val widgetOpacity: Float = 0.85f,
    val widgetBlur: Boolean = false,
)

enum class ClockStyle { ANALOG, DIGITAL, BOTH }

// ─────────────────────────────────────────────
// 日課タスク
// ─────────────────────────────────────────────
data class DailyTask(
    val id: Long = 0,
    val title: String,
    val isCompleted: Boolean = false,
    val completedDate: String? = null,   // "yyyy-MM-dd" 形式
    val sortOrder: Int = 0,
) {
    val isCompletedToday: Boolean
        get() = completedDate == java.time.LocalDate.now().toString()
}

// ─────────────────────────────────────────────
// Discord Webhook ボタン
// ─────────────────────────────────────────────
data class WebhookButton(
    val id: Long = 0,
    val label: String,
    val webhookUrl: String,
    val message: String,
    val colorArgb: Long = 0xFF6200EE,
    val widgetKey: String = "",
    val sortOrder: Int = 0,
)
