package com.nbks.mi.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memos")
data class MemoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val colorIndex: Int,
    val createdAt: Long,
    val updatedAt: Long,
    val isPinned: Boolean,
)

@Entity(tableName = "widget_configs")
data class WidgetConfigEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val positionX: Float,
    val positionY: Float,
    val widthDp: Int,
    val heightDp: Int,
    val isVisible: Boolean,
    val zIndex: Int,
    val customSettings: String,
    val title: String,
)

@Entity(tableName = "timer_presets")
data class TimerPresetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val durationMillis: Long,
    val isSilent: Boolean,
    val isDaily: Boolean,
    val dailyHour: Int,
    val dailyMinute: Int,
)

@Entity(tableName = "ai_messages")
data class AiMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val role: String,
    val content: String,
    val timestamp: Long,
    val isError: Boolean,
)

@Entity(tableName = "discord_notifications")
data class DiscordNotificationEntity(
    @PrimaryKey val id: String,
    val guildName: String,
    val channelName: String,
    val author: String,
    val avatarUrl: String?,
    val content: String,
    val timestamp: Long,
    val isRead: Boolean,
)

@Entity(tableName = "screen_schedules")
data class ScreenScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val label: String,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val brightness: Float,
    val isEnabled: Boolean,
    val dayOfWeekBitmask: Int = 0x7F,
)

@Entity(tableName = "daily_tasks")
data class DailyTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val isCompleted: Boolean = false,
    val completedDate: String? = null,
    val sortOrder: Int = 0,
)

@Entity(tableName = "webhook_buttons")
data class WebhookButtonEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val label: String,
    val webhookUrl: String,
    val message: String,
    val colorArgb: Long = 0xFF6200EE,
    val widgetKey: String = "",
    val sortOrder: Int = 0,
)
