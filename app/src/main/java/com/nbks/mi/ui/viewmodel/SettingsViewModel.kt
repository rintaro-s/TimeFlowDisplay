package com.nbks.mi.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nbks.mi.data.local.preferences.AppPreferences
import com.nbks.mi.data.repository.*
import com.nbks.mi.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.time.LocalDateTime
import java.time.ZonedDateTime
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    val appPreferences: AppPreferences,
    private val widgetRepository: WidgetRepository,
    private val screenScheduleRepository: ScreenScheduleRepository,
) : ViewModel() {

    val settings = appPreferences.appSettings.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        AppSettings(),
    )

    val schedules: StateFlow<List<ScreenSchedule>> = screenScheduleRepository.getAllSchedules()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ─────────────────────────────────────────────
    // テーマ
    // ─────────────────────────────────────────────
    fun setDarkMode(dark: Boolean) = viewModelScope.launch { appPreferences.setDarkMode(dark) }
    fun setDynamicColor(v: Boolean) = viewModelScope.launch { appPreferences.setDynamicColor(v) }
    fun setAppLanguage(language: AppLanguage) = viewModelScope.launch { appPreferences.setAppLanguage(language) }
    fun setClockStyle(style: ClockStyle) = viewModelScope.launch { appPreferences.setClockStyle(style) }
    fun setPrimaryColorSeed(seed: Long) = viewModelScope.launch { appPreferences.setPrimaryColorSeed(seed) }
    fun setWidgetOpacity(v: Float) = viewModelScope.launch { appPreferences.setWidgetOpacity(v) }
    fun setWallpaperUri(uri: String) = viewModelScope.launch { appPreferences.setWallpaperUri(uri) }
    fun setWallpaperDimAlpha(v: Float) = viewModelScope.launch { appPreferences.setWallpaperDimAlpha(v) }
    fun setKeepScreenOn(v: Boolean) = viewModelScope.launch { appPreferences.setKeepScreenOn(v) }
    fun setScheduleScreenDim(v: Boolean) = viewModelScope.launch { appPreferences.setScheduleScreenDim(v) }

    // ─────────────────────────────────────────────
    // LMStudio
    // ─────────────────────────────────────────────
    fun setLmStudioUrl(url: String) = viewModelScope.launch { appPreferences.setLmStudioUrl(url) }
    fun setLmStudioModel(m: String) = viewModelScope.launch { appPreferences.setLmStudioModel(m) }
    fun setLmStudioMaxTokens(n: Int) = viewModelScope.launch { appPreferences.setLmStudioMaxTokens(n) }

    // ─────────────────────────────────────────────
    // 画面スケジュール
    // ─────────────────────────────────────────────
    fun saveSchedule(schedule: ScreenSchedule) = viewModelScope.launch {
        screenScheduleRepository.saveSchedule(schedule)
    }

    fun updateSchedule(schedule: ScreenSchedule) = viewModelScope.launch {
        screenScheduleRepository.updateSchedule(schedule)
    }

    fun deleteSchedule(id: Long) = viewModelScope.launch {
        screenScheduleRepository.deleteSchedule(id)
    }
}
