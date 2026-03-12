package com.nbks.mi.data.local.db.dao

import androidx.room.*
import com.nbks.mi.data.local.db.entity.*
import kotlinx.coroutines.flow.Flow

// ─────────────────────────────────────────────
// メモ DAO
// ─────────────────────────────────────────────
@Dao
interface MemoDao {
    @Query("SELECT * FROM memos ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllMemos(): Flow<List<MemoEntity>>

    @Query("SELECT * FROM memos WHERE id = :id")
    suspend fun getMemoById(id: Long): MemoEntity?

    @Query("SELECT * FROM memos WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%'")
    fun searchMemos(query: String): Flow<List<MemoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemo(memo: MemoEntity): Long

    @Update
    suspend fun updateMemo(memo: MemoEntity)

    @Delete
    suspend fun deleteMemo(memo: MemoEntity)

    @Query("DELETE FROM memos WHERE id = :id")
    suspend fun deleteMemoById(id: Long)
}

// ─────────────────────────────────────────────
// ウィジェット設定 DAO
// ─────────────────────────────────────────────
@Dao
interface WidgetConfigDao {
    @Query("SELECT * FROM widget_configs ORDER BY zIndex ASC")
    fun getAllWidgets(): Flow<List<WidgetConfigEntity>>

    @Query("SELECT * FROM widget_configs WHERE id = :id")
    suspend fun getWidgetById(id: Long): WidgetConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWidget(widget: WidgetConfigEntity): Long

    @Update
    suspend fun updateWidget(widget: WidgetConfigEntity)

    @Delete
    suspend fun deleteWidget(widget: WidgetConfigEntity)

    @Query("DELETE FROM widget_configs WHERE id = :id")
    suspend fun deleteWidgetById(id: Long)

    @Query("UPDATE widget_configs SET positionX = :x, positionY = :y WHERE id = :id")
    suspend fun updatePosition(id: Long, x: Float, y: Float)

    @Query("UPDATE widget_configs SET widthDp = :w, heightDp = :h WHERE id = :id")
    suspend fun updateSize(id: Long, w: Int, h: Int)

    @Query("UPDATE widget_configs SET zIndex = :z WHERE id = :id")
    suspend fun updateZIndex(id: Long, z: Int)

    @Query("UPDATE widget_configs SET customSettings = :settings WHERE id = :id")
    suspend fun updateCustomSettings(id: Long, settings: String)

    @Query("DELETE FROM widget_configs")
    suspend fun deleteAll()
}

// ─────────────────────────────────────────────
// タイマープリセット DAO
// ─────────────────────────────────────────────
@Dao
interface TimerPresetDao {
    @Query("SELECT * FROM timer_presets ORDER BY id ASC")
    fun getAllPresets(): Flow<List<TimerPresetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: TimerPresetEntity): Long

    @Update
    suspend fun updatePreset(preset: TimerPresetEntity)

    @Delete
    suspend fun deletePreset(preset: TimerPresetEntity)

    @Query("DELETE FROM timer_presets WHERE id = :id")
    suspend fun deletePresetById(id: Long)
}

// ─────────────────────────────────────────────
// AI メッセージ DAO
// ─────────────────────────────────────────────
@Dao
interface AiMessageDao {
    @Query("SELECT * FROM ai_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<AiMessageEntity>>

    @Query("SELECT * FROM ai_messages ORDER BY timestamp ASC LIMIT :limit")
    suspend fun getRecentMessages(limit: Int): List<AiMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(msg: AiMessageEntity): Long

    @Query("DELETE FROM ai_messages")
    suspend fun clearHistory()

    @Query("DELETE FROM ai_messages WHERE id NOT IN (SELECT id FROM ai_messages ORDER BY timestamp DESC LIMIT :keep)")
    suspend fun trimHistory(keep: Int)
}

// ─────────────────────────────────────────────
// Discord 通知 DAO
// ─────────────────────────────────────────────
@Dao
interface DiscordNotificationDao {
    @Query("SELECT * FROM discord_notifications ORDER BY timestamp DESC LIMIT 100")
    fun getNotifications(): Flow<List<DiscordNotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(n: DiscordNotificationEntity)

    @Query("UPDATE discord_notifications SET isRead = 1 WHERE id = :id")
    suspend fun markRead(id: String)

    @Query("UPDATE discord_notifications SET isRead = 1")
    suspend fun markAllRead()

    @Query("DELETE FROM discord_notifications")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM discord_notifications WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>
}

// ─────────────────────────────────────────────
// 画面スケジュール DAO
// ─────────────────────────────────────────────
@Dao
interface ScreenScheduleDao {
    @Query("SELECT * FROM screen_schedules ORDER BY startHour ASC, startMinute ASC")
    fun getAllSchedules(): Flow<List<ScreenScheduleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(s: ScreenScheduleEntity): Long

    @Update
    suspend fun updateSchedule(s: ScreenScheduleEntity)

    @Delete
    suspend fun deleteSchedule(s: ScreenScheduleEntity)

    @Query("DELETE FROM screen_schedules WHERE id = :id")
    suspend fun deleteScheduleById(id: Long)
}

// ─────────────────────────────────────────────
// 日課タスク DAO
// ─────────────────────────────────────────────
@Dao
interface DailyTaskDao {
    @Query("SELECT * FROM daily_tasks ORDER BY sortOrder ASC, id ASC")
    fun getAllTasks(): Flow<List<DailyTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: DailyTaskEntity): Long

    @Update
    suspend fun updateTask(task: DailyTaskEntity)

    @Query("DELETE FROM daily_tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)

    @Query("UPDATE daily_tasks SET isCompleted = :completed, completedDate = :date WHERE id = :id")
    suspend fun setCompleted(id: Long, completed: Boolean, date: String?)
}

// ─────────────────────────────────────────────
// Webhook ボタン DAO
// ─────────────────────────────────────────────
@Dao
interface WebhookButtonDao {
    @Query("SELECT * FROM webhook_buttons WHERE widgetKey = :widgetKey ORDER BY sortOrder ASC, id ASC")
    fun getButtonsForWidget(widgetKey: String): Flow<List<WebhookButtonEntity>>

    @Query("SELECT * FROM webhook_buttons ORDER BY sortOrder ASC, id ASC")
    fun getAllButtons(): Flow<List<WebhookButtonEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertButton(button: WebhookButtonEntity): Long

    @Update
    suspend fun updateButton(button: WebhookButtonEntity)

    @Query("DELETE FROM webhook_buttons WHERE id = :id")
    suspend fun deleteButtonById(id: Long)
}

// ─────────────────────────────────────────────
// WidgetConfig 追加クエリ
// ─────────────────────────────────────────────
// (既存の WidgetConfigDao に追加クエリを別インタフェースで分けず、Daos.kt 末尾に記述済み)
