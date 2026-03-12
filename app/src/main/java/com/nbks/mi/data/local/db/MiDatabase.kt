package com.nbks.mi.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.nbks.mi.data.local.db.dao.*
import com.nbks.mi.data.local.db.entity.*

@Database(
    entities = [
        MemoEntity::class,
        WidgetConfigEntity::class,
        TimerPresetEntity::class,
        AiMessageEntity::class,
        DiscordNotificationEntity::class,
        ScreenScheduleEntity::class,
        DailyTaskEntity::class,
        WebhookButtonEntity::class,
    ],
    version = 3,
    exportSchema = false,
)
abstract class MiDatabase : RoomDatabase() {
    abstract fun memoDao(): MemoDao
    abstract fun widgetConfigDao(): WidgetConfigDao
    abstract fun timerPresetDao(): TimerPresetDao
    abstract fun aiMessageDao(): AiMessageDao
    abstract fun discordNotificationDao(): DiscordNotificationDao
    abstract fun screenScheduleDao(): ScreenScheduleDao
    abstract fun dailyTaskDao(): DailyTaskDao
    abstract fun webhookButtonDao(): WebhookButtonDao

    companion object {
        const val DATABASE_NAME = "mi_database"
    }
}
