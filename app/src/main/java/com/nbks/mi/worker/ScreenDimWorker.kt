package com.nbks.mi.worker

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.nbks.mi.data.local.db.dao.ScreenScheduleDao
import com.nbks.mi.data.local.db.entity.ScreenScheduleEntity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.concurrent.TimeUnit

@HiltWorker
class ScreenDimWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val screenScheduleDao: ScreenScheduleDao,
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "screen_dim_checker"
        const val CHANNEL_ID = "mi_screen_dim_channel"

        /**
         * 毎分実行の定期Workerをスケジュール。
         * BootReceiver および SettingsViewModel から呼ばれる。
         */
        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<ScreenDimWorker>(15, TimeUnit.MINUTES)
                .setConstraints(Constraints.NONE)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request,
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val schedules = screenScheduleDao.getAllSchedules().first()
            val activeSchedule = findActiveSchedule(schedules)
            sendDimBroadcast(activeSchedule)
            Result.success()
        } catch (e: Exception) {
            Log.e("ScreenDimWorker", "Worker failed", e)
            Result.retry()
        }
    }

    private fun findActiveSchedule(schedules: List<ScreenScheduleEntity>): ScreenScheduleEntity? {
        val now = Calendar.getInstance()
        val currentHour = now.get(Calendar.HOUR_OF_DAY)
        val currentMinute = now.get(Calendar.MINUTE)
        val currentTotal = currentHour * 60 + currentMinute

        return schedules.firstOrNull { schedule ->
            if (!schedule.isEnabled) return@firstOrNull false
            // 曜日チェック (Calendar.DAY_OF_WEEK: 1=Sun, 2=Mon, ...7=Sat → bit 0=月, ..., bit 6=日)
            val calendarDay = now.get(Calendar.DAY_OF_WEEK)
            val bit = (calendarDay - 2 + 7) % 7
            if (schedule.dayOfWeekBitmask and (1 shl bit) == 0) return@firstOrNull false
            val startTotal = schedule.startHour * 60 + schedule.startMinute
            val endTotal = schedule.endHour * 60 + schedule.endMinute
            if (startTotal <= endTotal) {
                // 同日内のスケジュール (例: 09:00 〜 17:00)
                currentTotal in startTotal until endTotal
            } else {
                // 深夜をまたぐスケジュール (例: 22:00 〜 07:00)
                currentTotal >= startTotal || currentTotal < endTotal
            }
        }
    }

    private fun sendDimBroadcast(schedule: ScreenScheduleEntity?) {
        val intent = Intent(ScreenDimReceiver.ACTION_DIM_UPDATE).apply {
            setPackage(applicationContext.packageName)
            putExtra(ScreenDimReceiver.EXTRA_BRIGHTNESS, schedule?.brightness ?: -1f)
            putExtra(ScreenDimReceiver.EXTRA_SCHEDULE_ACTIVE, schedule != null)
        }
        applicationContext.sendBroadcast(intent)
    }
}

// ─────────────────────────────────────────────
// ブロードキャストのアクション定数
// ─────────────────────────────────────────────
object ScreenDimReceiver {
    const val ACTION_DIM_UPDATE = "com.nbks.mi.SCREEN_DIM_UPDATE"
    const val EXTRA_BRIGHTNESS = "extra_brightness"
    const val EXTRA_SCHEDULE_ACTIVE = "extra_schedule_active"
}
