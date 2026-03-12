package com.nbks.mi.data.repository

import com.nbks.mi.data.local.db.dao.*
import com.nbks.mi.data.local.db.entity.*
import com.nbks.mi.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

// ─────────────────────────────────────────────
// メモ リポジトリ
// ─────────────────────────────────────────────
@Singleton
class MemoRepository @Inject constructor(private val dao: MemoDao) {

    fun getAllMemos(): Flow<List<Memo>> = dao.getAllMemos().map { list ->
        list.map { it.toDomain() }
    }

    fun searchMemos(query: String): Flow<List<Memo>> = dao.searchMemos(query).map { list ->
        list.map { it.toDomain() }
    }

    suspend fun getMemoById(id: Long): Memo? = dao.getMemoById(id)?.toDomain()

    suspend fun saveMemo(memo: Memo): Long {
        val now = System.currentTimeMillis()
        return dao.insertMemo(
            MemoEntity(
                id = memo.id,
                title = memo.title,
                content = memo.content,
                colorIndex = memo.colorIndex,
                createdAt = if (memo.id == 0L) now else epochOf(memo.createdAt),
                updatedAt = now,
                isPinned = memo.isPinned,
            )
        )
    }

    suspend fun deleteMemo(id: Long) = dao.deleteMemoById(id)

    private fun MemoEntity.toDomain() = Memo(
        id = id,
        title = title,
        content = content,
        colorIndex = colorIndex,
        createdAt = localDateOf(createdAt),
        updatedAt = localDateOf(updatedAt),
        isPinned = isPinned,
    )
}

// ─────────────────────────────────────────────
// ウィジェット設定 リポジトリ
// ─────────────────────────────────────────────
@Singleton
class WidgetRepository @Inject constructor(private val dao: WidgetConfigDao) {

    fun getAllWidgets(): Flow<List<WidgetConfig>> = dao.getAllWidgets().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun saveWidget(config: WidgetConfig): Long = dao.insertWidget(config.toEntity())
    suspend fun updatePosition(id: Long, x: Float, y: Float) = dao.updatePosition(id, x, y)
    suspend fun updateSize(id: Long, w: Int, h: Int) = dao.updateSize(id, w, h)
    suspend fun updateZIndex(id: Long, z: Int) = dao.updateZIndex(id, z)
    suspend fun updateCustomSettings(id: Long, settings: String) = dao.updateCustomSettings(id, settings)
    suspend fun deleteWidget(id: Long) = dao.deleteWidgetById(id)
    suspend fun deleteAll() = dao.deleteAll()

    suspend fun getWidgetById(id: Long): WidgetConfig? = dao.getWidgetById(id)?.toDomain()

    private fun WidgetConfigEntity.toDomain() = WidgetConfig(
        id = id,
        type = runCatching { WidgetType.valueOf(type) }.getOrDefault(WidgetType.CLOCK),
        positionX = positionX,
        positionY = positionY,
        widthDp = widthDp,
        heightDp = heightDp,
        isVisible = isVisible,
        zIndex = zIndex,
        customSettings = customSettings,
        title = title,
    )

    private fun WidgetConfig.toEntity() = WidgetConfigEntity(
        id = id,
        type = type.name,
        positionX = positionX,
        positionY = positionY,
        widthDp = widthDp,
        heightDp = heightDp,
        isVisible = isVisible,
        zIndex = zIndex,
        customSettings = customSettings,
        title = title,
    )
}

// ─────────────────────────────────────────────
// タイマープリセット リポジトリ
// ─────────────────────────────────────────────
@Singleton
class TimerPresetRepository @Inject constructor(private val dao: TimerPresetDao) {

    fun getAllPresets(): Flow<List<TimerPreset>> = dao.getAllPresets().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun savePreset(preset: TimerPreset): Long = dao.insertPreset(preset.toEntity())
    suspend fun updatePreset(preset: TimerPreset) = dao.updatePreset(preset.toEntity())
    suspend fun deletePreset(id: Long) = dao.deletePresetById(id)

    private fun TimerPresetEntity.toDomain() = TimerPreset(
        id = id, name = name, durationMillis = durationMillis,
        isSilent = isSilent, isDaily = isDaily,
        dailyHour = dailyHour, dailyMinute = dailyMinute,
    )

    private fun TimerPreset.toEntity() = TimerPresetEntity(
        id = id, name = name, durationMillis = durationMillis,
        isSilent = isSilent, isDaily = isDaily,
        dailyHour = dailyHour, dailyMinute = dailyMinute,
    )
}

// ─────────────────────────────────────────────
// AI メッセージ リポジトリ
// ─────────────────────────────────────────────
@Singleton
class AiMessageRepository @Inject constructor(private val dao: AiMessageDao) {

    fun getAllMessages(): Flow<List<AiMessage>> = dao.getAllMessages().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun getRecentMessages(limit: Int = 20): List<AiMessage> =
        dao.getRecentMessages(limit).map { it.toDomain() }

    suspend fun saveMessage(msg: AiMessage): Long = dao.insertMessage(
        AiMessageEntity(
            id = msg.id,
            role = msg.role,
            content = msg.content,
            timestamp = epochOf(msg.timestamp),
            isError = msg.isError,
        )
    )

    suspend fun clearHistory() = dao.clearHistory()
    suspend fun trimHistory(keep: Int = 50) = dao.trimHistory(keep)

    private fun AiMessageEntity.toDomain() = AiMessage(
        id = id, role = role, content = content,
        timestamp = localDateOf(timestamp), isError = isError,
    )
}

// ─────────────────────────────────────────────
// (Discord notification repository removed)
// ─────────────────────────────────────────────

// ─────────────────────────────────────────────
// 画面スケジュール リポジトリ
// ─────────────────────────────────────────────
@Singleton
class ScreenScheduleRepository @Inject constructor(private val dao: ScreenScheduleDao) {

    fun getAllSchedules(): Flow<List<ScreenSchedule>> =
        dao.getAllSchedules().map { list -> list.map { it.toDomain() } }

    suspend fun saveSchedule(s: ScreenSchedule): Long = dao.insertSchedule(s.toEntity())
    suspend fun updateSchedule(s: ScreenSchedule) = dao.updateSchedule(s.toEntity())
    suspend fun deleteSchedule(id: Long) = dao.deleteScheduleById(id)

    private fun ScreenScheduleEntity.toDomain() = ScreenSchedule(
        id = id, label = label, startHour = startHour, startMinute = startMinute,
        endHour = endHour, endMinute = endMinute, brightness = brightness, isEnabled = isEnabled,
        dayOfWeekBitmask = dayOfWeekBitmask,
    )

    private fun ScreenSchedule.toEntity() = ScreenScheduleEntity(
        id = id, label = label, startHour = startHour, startMinute = startMinute,
        endHour = endHour, endMinute = endMinute, brightness = brightness, isEnabled = isEnabled,
        dayOfWeekBitmask = dayOfWeekBitmask,
    )
}

// ─────────────────────────────────────────────
// ユーティリティ
// ─────────────────────────────────────────────
internal fun epochOf(ldt: LocalDateTime): Long =
    ldt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

internal fun localDateOf(epochMillis: Long): LocalDateTime =
    LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.systemDefault())

// ─────────────────────────────────────────────
// 日課 リポジトリ
// ─────────────────────────────────────────────
@Singleton
class DailyTaskRepository @Inject constructor(private val dao: DailyTaskDao) {

    fun getAllTasks(): Flow<List<DailyTask>> = dao.getAllTasks().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun addTask(title: String): Long = dao.insertTask(
        DailyTaskEntity(title = title, sortOrder = System.currentTimeMillis().toInt() and 0x7FFFFFFF)
    )

    suspend fun deleteTask(id: Long) = dao.deleteTaskById(id)

    suspend fun setCompleted(id: Long, completed: Boolean) {
        val date = if (completed) java.time.LocalDate.now().toString() else null
        dao.setCompleted(id, completed, date)
    }

    suspend fun resetYesterdayTasks() {
        val today = java.time.LocalDate.now().toString()
        dao.getAllTasks().first()
            .filter { it.completedDate != null && it.completedDate != today }
            .forEach { dao.setCompleted(it.id, false, null) }
    }

    private fun DailyTaskEntity.toDomain() = DailyTask(
        id = id, title = title, isCompleted = isCompleted,
        completedDate = completedDate, sortOrder = sortOrder,
    )
}

// ─────────────────────────────────────────────
// Webhook ボタン リポジトリ
// ─────────────────────────────────────────────
@Singleton
class WebhookButtonRepository @Inject constructor(private val dao: WebhookButtonDao) {

    fun getButtonsForWidget(widgetKey: String): Flow<List<WebhookButton>> =
        dao.getButtonsForWidget(widgetKey).map { list -> list.map { it.toDomain() } }

    fun getAllButtons(): Flow<List<WebhookButton>> =
        dao.getAllButtons().map { list -> list.map { it.toDomain() } }

    suspend fun saveButton(button: WebhookButton): Long = dao.insertButton(button.toEntity())
    suspend fun updateButton(button: WebhookButton) = dao.updateButton(button.toEntity())
    suspend fun deleteButton(id: Long) = dao.deleteButtonById(id)

    private fun WebhookButtonEntity.toDomain() = WebhookButton(
        id = id, label = label, webhookUrl = webhookUrl,
        message = message, colorArgb = colorArgb, widgetKey = widgetKey, sortOrder = sortOrder,
    )

    private fun WebhookButton.toEntity() = WebhookButtonEntity(
        id = id, label = label, webhookUrl = webhookUrl,
        message = message, colorArgb = colorArgb, widgetKey = widgetKey, sortOrder = sortOrder,
    )
}
