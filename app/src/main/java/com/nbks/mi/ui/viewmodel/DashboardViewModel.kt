package com.nbks.mi.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nbks.mi.data.local.preferences.AppPreferences
import com.nbks.mi.data.repository.WidgetRepository
import com.nbks.mi.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val widgetRepository: WidgetRepository,
    val appPreferences: AppPreferences,
) : ViewModel() {

    val settings = appPreferences.appSettings.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        AppSettings(),
    )

    val widgets: StateFlow<List<WidgetConfig>> = widgetRepository.getAllWidgets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode.asStateFlow()

    private val _showAddWidgetDialog = MutableStateFlow(false)
    val showAddWidgetDialog: StateFlow<Boolean> = _showAddWidgetDialog.asStateFlow()

    private val _screenDimAlpha = MutableStateFlow(0f)
    val screenDimAlpha: StateFlow<Float> = _screenDimAlpha.asStateFlow()

    init {
        ensureDefaultWidgets()
        startScreenDimMonitor()
    }

    private fun ensureDefaultWidgets() {
        viewModelScope.launch {
            val current = widgetRepository.getAllWidgets().first()
            if (current.isEmpty()) {
                val defaults = listOf(
                    WidgetConfig(type = WidgetType.CLOCK, positionX = 20f, positionY = 100f, widthDp = 320, heightDp = 180, zIndex = 0),
                    WidgetConfig(type = WidgetType.CALENDAR, positionX = 20f, positionY = 300f, widthDp = 320, heightDp = 220, zIndex = 1),
                    WidgetConfig(type = WidgetType.PROGRESS, positionX = 20f, positionY = 540f, widthDp = 320, heightDp = 160, zIndex = 2),
                    WidgetConfig(type = WidgetType.MEMO, positionX = 360f, positionY = 100f, widthDp = 300, heightDp = 200, zIndex = 3),
                    WidgetConfig(type = WidgetType.TIMER, positionX = 360f, positionY = 320f, widthDp = 300, heightDp = 300, zIndex = 4),
                )
                defaults.forEach { widgetRepository.saveWidget(it) }
            }
        }
    }

    private fun startScreenDimMonitor() {
        viewModelScope.launch {
            appPreferences.appSettings.collect { settings ->
                if (settings.scheduleScreenDim) {
                    // 現在時刻に基づいて画面の暗さを更新
                    // ScreenScheduleWorkerが実際の制御を行う
                }
            }
        }
    }

    fun toggleEditMode() { _isEditMode.value = !_isEditMode.value }
    fun setEditMode(value: Boolean) { _isEditMode.value = value }

    fun openAddWidgetDialog() { _showAddWidgetDialog.value = true }
    fun closeAddWidgetDialog() { _showAddWidgetDialog.value = false }

    fun addWidget(type: WidgetType) {
        viewModelScope.launch {
            val existingCount = widgets.value.size
            val defaultSizes = mapOf(
                WidgetType.CLOCK to Pair(320, 180),
                WidgetType.CALENDAR to Pair(320, 220),
                WidgetType.MEMO to Pair(300, 200),
                WidgetType.TIMER to Pair(280, 160),
                WidgetType.DAILY_ALARM to Pair(300, 350),
                WidgetType.PROGRESS to Pair(320, 160),
                WidgetType.AI to Pair(340, 400),
                WidgetType.DAILY_TASKS to Pair(300, 280),
                WidgetType.WEBHOOK_BUTTONS to Pair(300, 240),
                WidgetType.IMAGE to Pair(280, 200),
            )
            val (w, h) = defaultSizes[type] ?: Pair(300, 200)
            widgetRepository.saveWidget(
                WidgetConfig(
                    type = type,
                    positionX = (30 + (existingCount % 3) * 30).toFloat(),
                    positionY = (120 + (existingCount % 4) * 30).toFloat(),
                    widthDp = w,
                    heightDp = h,
                    zIndex = existingCount,
                )
            )
            _showAddWidgetDialog.value = false
        }
    }

    fun updateWidgetPosition(id: Long, x: Float, y: Float) {
        viewModelScope.launch { widgetRepository.updatePosition(id, x, y) }
    }

    fun updateWidgetSize(id: Long, w: Int, h: Int) {
        viewModelScope.launch { widgetRepository.updateSize(id, w, h) }
    }

    fun bringToFront(id: Long) {
        viewModelScope.launch {
            val maxZ = widgets.value.maxOfOrNull { it.zIndex } ?: 0
            widgetRepository.updateZIndex(id, maxZ + 1)
        }
    }

    fun deleteWidget(id: Long) {
        viewModelScope.launch { widgetRepository.deleteWidget(id) }
    }

    fun updateWidgetSettings(id: Long, settings: String) {
        viewModelScope.launch { widgetRepository.updateCustomSettings(id, settings) }
    }

    fun setScreenDimAlpha(alpha: Float) { _screenDimAlpha.value = alpha }
}
