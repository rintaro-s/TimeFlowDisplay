package com.nbks.mi.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nbks.mi.domain.model.DailyAlarmStatus
import com.nbks.mi.domain.model.TimerPreset
import com.nbks.mi.domain.model.TimerState
import com.nbks.mi.data.repository.TimerPresetRepository
import com.nbks.mi.service.TimerForegroundService
import android.content.Intent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class TimerViewModel @Inject constructor(
    application: Application,
    private val presetRepository: TimerPresetRepository,
) : AndroidViewModel(application) {

    val presets: StateFlow<List<TimerPreset>> = presetRepository.getAllPresets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _timerState = MutableStateFlow(TimerState())
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    private val _dailyAlarmStatuses = MutableStateFlow<List<DailyAlarmStatus>>(emptyList())
    val dailyAlarmStatuses: StateFlow<List<DailyAlarmStatus>> = _dailyAlarmStatuses.asStateFlow()

    private var countdownJob: kotlinx.coroutines.Job? = null

    init {
        // 毎秒、日時アラームのカウントダウンを更新
        viewModelScope.launch {
            presets.collect { _ -> updateDailyAlarms() }
        }
        viewModelScope.launch {
            while (true) {
                updateDailyAlarms()
                kotlinx.coroutines.delay(1000)
            }
        }
    }

    private fun updateDailyAlarms() {
        val now = LocalTime.now()
        val nowMs = now.hour * 3_600_000L + now.minute * 60_000L + now.second * 1000L
        _dailyAlarmStatuses.value = presets.value
            .filter { it.isDaily }
            .map { preset ->
                val targetMs = preset.dailyHour * 3_600_000L + preset.dailyMinute * 60_000L
                DailyAlarmStatus(preset, targetMs - nowMs)
            }
            .sortedBy { it.preset.dailyHour * 60 + it.preset.dailyMinute }
    }

    fun startTimer(durationMillis: Long, label: String = "", isSilent: Boolean = false) {
        countdownJob?.cancel()
        _timerState.value = TimerState(
            label = label,
            durationMillis = durationMillis,
            remainingMillis = durationMillis,
            isRunning = true,
            isPaused = false,
            isSilent = isSilent,
        )
        startCountdown()
        val intent = Intent(getApplication(), TimerForegroundService::class.java).apply {
            action = TimerForegroundService.ACTION_START
            putExtra(TimerForegroundService.EXTRA_DURATION_MS, durationMillis)
            putExtra(TimerForegroundService.EXTRA_LABEL, label)
            putExtra(TimerForegroundService.EXTRA_SILENT, isSilent)
        }
        getApplication<Application>().startService(intent)
    }

    fun pauseTimer() {
        countdownJob?.cancel()
        _timerState.update { it.copy(isRunning = false, isPaused = true) }
        val intent = Intent(getApplication(), TimerForegroundService::class.java).apply {
            action = TimerForegroundService.ACTION_PAUSE
        }
        getApplication<Application>().startService(intent)
    }

    fun resumeTimer() {
        if (_timerState.value.isPaused) {
            _timerState.update { it.copy(isRunning = true, isPaused = false) }
            startCountdown()
            val intent = Intent(getApplication(), TimerForegroundService::class.java).apply {
                action = TimerForegroundService.ACTION_RESUME
            }
            getApplication<Application>().startService(intent)
        }
    }

    fun stopTimer() {
        countdownJob?.cancel()
        _timerState.value = TimerState()
        val intent = Intent(getApplication(), TimerForegroundService::class.java).apply {
            action = TimerForegroundService.ACTION_STOP
        }
        getApplication<Application>().startService(intent)
    }

    private fun startCountdown() {
        countdownJob = viewModelScope.launch {
            while (_timerState.value.remainingMillis > 0 && _timerState.value.isRunning) {
                kotlinx.coroutines.delay(100)
                _timerState.update { state ->
                    val remaining = (state.remainingMillis - 100).coerceAtLeast(0)
                    if (remaining == 0L) {
                        state.copy(remainingMillis = 0, isRunning = false, isFinished = true)
                    } else {
                        state.copy(remainingMillis = remaining)
                    }
                }
            }
        }
    }

    fun savePreset(preset: TimerPreset) = viewModelScope.launch {
        presetRepository.savePreset(preset)
    }

    fun deletePreset(id: Long) = viewModelScope.launch {
        presetRepository.deletePreset(id)
    }

    fun loadPreset(preset: TimerPreset) {
        if (!preset.isDaily) {
            startTimer(
                durationMillis = preset.durationMillis,
                label = preset.name,
                isSilent = preset.isSilent,
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
    }
}
