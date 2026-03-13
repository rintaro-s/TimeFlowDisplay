package com.nbks.mi.ui.screens

import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.nbks.mi.domain.model.*
import com.nbks.mi.ui.theme.LocalIsJa
import com.nbks.mi.ui.components.*
import com.nbks.mi.ui.components.widgets.*
import com.nbks.mi.ui.viewmodel.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.zIndex

// ─────────────────────────────────────────────
// ダッシュボード画面 (メイン)
// ─────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToSettings: () -> Unit,
    dashboardViewModel: DashboardViewModel = hiltViewModel(),
    aiViewModel: AiViewModel = hiltViewModel(),
    timerViewModel: TimerViewModel = hiltViewModel(),
    memoViewModel: MemoViewModel = hiltViewModel(),
    dailyTaskViewModel: DailyTaskViewModel = hiltViewModel(),
    webhookViewModel: WebhookViewModel = hiltViewModel(),
) {
    val settings by dashboardViewModel.settings.collectAsStateWithLifecycle()
    val widgets by dashboardViewModel.widgets.collectAsStateWithLifecycle()
    val isEditMode by dashboardViewModel.isEditMode.collectAsStateWithLifecycle()
    val showAddWidgetDialog by dashboardViewModel.showAddWidgetDialog.collectAsStateWithLifecycle()
    val screenDimAlpha by dashboardViewModel.screenDimAlpha.collectAsStateWithLifecycle()

    val aiMessages by aiViewModel.messages.collectAsStateWithLifecycle()
    val aiIsLoading by aiViewModel.isLoading.collectAsStateWithLifecycle()

    val timerState by timerViewModel.timerState.collectAsStateWithLifecycle()
    val timerPresets by timerViewModel.presets.collectAsStateWithLifecycle()
    val dailyAlarmStatuses by timerViewModel.dailyAlarmStatuses.collectAsStateWithLifecycle()

    val memos by memoViewModel.memos.collectAsStateWithLifecycle()
    val showMemoEditor by memoViewModel.showEditor.collectAsStateWithLifecycle()
    val editingMemo by memoViewModel.editingMemo.collectAsStateWithLifecycle()

    val dailyTasks by dailyTaskViewModel.tasks.collectAsStateWithLifecycle()
    val webhookButtons by webhookViewModel.buttons.collectAsStateWithLifecycle()
    val webhookSendStatus by webhookViewModel.sendStatus.collectAsStateWithLifecycle()

    // Keep screen on設定
    val view = LocalView.current
    LaunchedEffect(settings.keepScreenOn) {
        val window = (view.context as? android.app.Activity)?.window
        if (settings.keepScreenOn) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // 編集モード終了: バックキー
    BackHandler(enabled = isEditMode) {
        dashboardViewModel.setEditMode(false)
    }

    val isJa = java.util.Locale.getDefault().language == "ja"

    Box(modifier = Modifier.fillMaxSize()) {
        // ─── 壁紙/배경 ───
        if (settings.wallpaperUri.isNotEmpty()) {
            AsyncImage(
                model = settings.wallpaperUri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            // 壁紙のディム
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = settings.wallpaperDimAlpha))
            )
        } else {
            // デフォルト背景グラデーション
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = if (settings.isDarkMode)
                                listOf(Color(0xFF0F0F17), Color(0xFF1A1030))
                            else listOf(Color(0xFFF0EEFF), Color(0xFFE8F4F8))
                        )
                    )
            )
        }

        // ─── ウィジェット群 ───
        Box(modifier = Modifier.fillMaxSize()) {
            widgets.filter { it.isVisible }.forEach { config ->
                DraggableWidget(
                    config = config,
                    isEditMode = isEditMode,
                    isDarkTheme = settings.isDarkMode,
                    widgetOpacity = settings.widgetOpacity,
                    onPositionChanged = { x, y -> dashboardViewModel.updateWidgetPosition(config.id, x, y) },
                    onSizeChanged = { w, h -> dashboardViewModel.updateWidgetSize(config.id, w, h) },
                    onBringToFront = { dashboardViewModel.bringToFront(config.id) },
                    onDelete = { dashboardViewModel.deleteWidget(config.id) },
                ) {
                    WidgetContent(
                        config = config,
                        settings = settings,
                        isEditMode = isEditMode,
                        aiMessages = aiMessages,
                        aiIsLoading = aiIsLoading,
                        timerState = timerState,
                        timerPresets = timerPresets,
                        dailyAlarmStatuses = dailyAlarmStatuses,
                        memos = memos,
                        dailyTasks = dailyTasks,
                        webhookButtons = webhookButtons,
                        webhookSendStatus = webhookSendStatus,
                        onAiSend = { aiViewModel.sendMessage(it) },
                        onAiClear = { aiViewModel.clearHistory() },
                        onTimerStart = { dur, label, silent -> timerViewModel.startTimer(dur, label, silent) },
                        onTimerPause = { timerViewModel.pauseTimer() },
                        onTimerResume = { timerViewModel.resumeTimer() },
                        onTimerStop = { timerViewModel.stopTimer() },
                        onLoadTimerPreset = { timerViewModel.loadPreset(it) },
                        onSaveTimerPreset = { timerViewModel.savePreset(it) },
                        onDeleteTimerPreset = { timerViewModel.deletePreset(it) },
                        onNewMemo = { memoViewModel.newMemo() },
                        onEditMemo = { memoViewModel.editMemo(it) },
                        onDeleteMemo = { memoViewModel.deleteMemo(it) },
                        onToggleDailyTask = { dailyTaskViewModel.toggleTask(it) },
                        onAddDailyTask = { dailyTaskViewModel.addTask(it) },
                        onDeleteDailyTask = { dailyTaskViewModel.deleteTask(it) },
                        onWebhookSend = { webhookViewModel.sendWebhook(it) },
                        onAddWebhookButton = { webhookViewModel.addButton(it) },
                        onEditWebhookButton = { webhookViewModel.updateButton(it) },
                        onDeleteWebhookButton = { webhookViewModel.deleteButton(it) },
                        onUpdateWidgetSettings = { id, s -> dashboardViewModel.updateWidgetSettings(id, s) },
                    )
                }
            }
        }

        // ─── 画面暗くするオーバーレイ ───
        if (screenDimAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = screenDimAlpha))
                    .zIndex(100f)
            )
        }

        // ─── 編集モードバナー ───
        AnimatedVisibility(
            visible = isEditMode,
            modifier = Modifier.align(Alignment.TopCenter).zIndex(50f),
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically(),
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
                shadowElevation = 4.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .statusBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (isJa) "編集モード: ドラッグで移動，角をドラッグでリサイズ，×で削除" else "Edit Mode: Drag to move, drag corner to resize, × to delete",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(
                        onClick = { dashboardViewModel.setEditMode(false) },
                        modifier = Modifier.height(32.dp),
                    ) {
                        Text(if (isJa) "完了" else "Done", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        // ─── FAB エリア ───
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .navigationBarsPadding()
                .zIndex(50f),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // 設定
            SmallFloatingActionButton(
                onClick = onNavigateToSettings,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
            ) {
                Icon(Icons.Default.Settings, contentDescription = if (isJa) "設定" else "Settings", modifier = Modifier.size(18.dp))
            }

            // ウィジェット追加
            SmallFloatingActionButton(
                onClick = { dashboardViewModel.openAddWidgetDialog() },
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.9f),
            ) {
                Icon(Icons.Default.AddBox, contentDescription = if (isJa) "ウィジェット追加" else "Add Widget", modifier = Modifier.size(18.dp))
            }

            // 編集モード切り替え
            FloatingActionButton(
                onClick = { dashboardViewModel.toggleEditMode() },
                containerColor = if (isEditMode)
                    MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
            ) {
                Icon(
                    if (isEditMode) Icons.Default.Check else Icons.Default.EditNote,
                    contentDescription = if (isJa) "編集モード" else "Edit Mode",
                )
            }
        }

        // ─── メモエディタダイアログ ───
        if (showMemoEditor) {
            MemoEditorDialog(
                memo = editingMemo,
                isDarkTheme = settings.isDarkMode,
                onSave = { title, content, colorIndex, isPinned ->
                    memoViewModel.saveMemo(title, content, colorIndex, isPinned)
                },
                onDismiss = { memoViewModel.closeEditor() },
            )
        }

        // ─── ウィジェット追加ダイアログ ───
        if (showAddWidgetDialog) {
            AddWidgetDialog(
                onAdd = { type -> dashboardViewModel.addWidget(type) },
                onDismiss = { dashboardViewModel.closeAddWidgetDialog() },
            )
        }
    }
}

// ─────────────────────────────────────────────
// ウィジェットコンテンツのルーティング
// ─────────────────────────────────────────────
@Composable
private fun WidgetContent(
    config: WidgetConfig,
    settings: AppSettings,
    isEditMode: Boolean,
    aiMessages: List<AiMessage>,
    aiIsLoading: Boolean,
    timerState: TimerState,
    timerPresets: List<TimerPreset>,
    dailyAlarmStatuses: List<DailyAlarmStatus>,
    memos: List<Memo>,
    dailyTasks: List<DailyTask>,
    webhookButtons: List<WebhookButton>,
    webhookSendStatus: Map<Long, Boolean?>,
    onAiSend: (String) -> Unit,
    onAiClear: () -> Unit,
    onTimerStart: (Long, String, Boolean) -> Unit,
    onTimerPause: () -> Unit,
    onTimerResume: () -> Unit,
    onTimerStop: () -> Unit,
    onLoadTimerPreset: (TimerPreset) -> Unit,
    onSaveTimerPreset: (TimerPreset) -> Unit,
    onDeleteTimerPreset: (Long) -> Unit,
    onNewMemo: () -> Unit,
    onEditMemo: (Memo) -> Unit,
    onDeleteMemo: (Long) -> Unit,
    onToggleDailyTask: (Long) -> Unit,
    onAddDailyTask: (String) -> Unit,
    onDeleteDailyTask: (Long) -> Unit,
    onWebhookSend: (WebhookButton) -> Unit,
    onAddWebhookButton: (WebhookButton) -> Unit,
    onEditWebhookButton: (WebhookButton) -> Unit,
    onDeleteWebhookButton: (Long) -> Unit,
    onUpdateWidgetSettings: (Long, String) -> Unit,
) {
    when (config.type) {
        WidgetType.CLOCK -> ClockWidget(
            clockStyle = settings.clockStyle,
            isDarkTheme = settings.isDarkMode,
            modifier = Modifier.fillMaxSize(),
        )
        WidgetType.CALENDAR -> CalendarWidget(
            isDarkTheme = settings.isDarkMode,
            modifier = Modifier.fillMaxSize(),
        )
        WidgetType.MEMO -> MemoWidget(
            memos = memos,
            isDarkTheme = settings.isDarkMode,
            onNewMemo = onNewMemo,
            onEditMemo = onEditMemo,
            onDeleteMemo = onDeleteMemo,
            modifier = Modifier.fillMaxSize(),
        )
        WidgetType.DAILY_ALARM -> DailyAlarmWidget(
            alarms = dailyAlarmStatuses,
            isDarkTheme = settings.isDarkMode,
            onSave = onSaveTimerPreset,
            onDelete = onDeleteTimerPreset,
            modifier = Modifier.fillMaxSize(),
        )
        WidgetType.TIMER -> TimerWidget(
            timerState = timerState,
            presets = timerPresets,
            isDarkTheme = settings.isDarkMode,
            onStart = onTimerStart,
            onPause = onTimerPause,
            onResume = onTimerResume,
            onStop = onTimerStop,
            onLoadPreset = onLoadTimerPreset,
            onSavePreset = onSaveTimerPreset,
            onDeletePreset = onDeleteTimerPreset,
            modifier = Modifier.fillMaxSize(),
        )
        WidgetType.PROGRESS -> ProgressWidget(
            isDarkTheme = settings.isDarkMode,
            modifier = Modifier.fillMaxSize(),
        )
        WidgetType.AI -> AiWidget(
            messages = aiMessages,
            isLoading = aiIsLoading,
            isDarkTheme = settings.isDarkMode,
            isConfigured = settings.lmStudioBaseUrl.isNotEmpty(),
            onSend = onAiSend,
            onClearHistory = onAiClear,
            modifier = Modifier.fillMaxSize(),
        )
        WidgetType.DAILY_TASKS -> DailyTaskWidget(
            tasks = dailyTasks,
            isDarkTheme = settings.isDarkMode,
            onToggle = onToggleDailyTask,
            onAdd = onAddDailyTask,
            onDelete = onDeleteDailyTask,
            modifier = Modifier.fillMaxSize(),
        )
        WidgetType.WEBHOOK_BUTTONS -> WebhookButtonWidget(
            widgetKey = config.id.toString(),
            buttons = webhookButtons,
            sendStatus = webhookSendStatus,
            isDarkTheme = settings.isDarkMode,
            isEditMode = isEditMode,
            onSend = onWebhookSend,
            onAdd = onAddWebhookButton,
            onEdit = onEditWebhookButton,
            onDelete = onDeleteWebhookButton,
            modifier = Modifier.fillMaxSize(),
        )
        WidgetType.IMAGE -> ImageWidget(
            customSettings = config.customSettings ?: "",
            isDarkTheme = settings.isDarkMode,
            isEditMode = isEditMode,
            onUpdateSettings = { s -> onUpdateWidgetSettings(config.id, s) },
            modifier = Modifier.fillMaxSize(),
        )
    }
}

// ─────────────────────────────────────────────
// ウィジェット追加ダイアログ
// ─────────────────────────────────────────────
@Composable
private fun AddWidgetDialog(
    onAdd: (WidgetType) -> Unit,
    onDismiss: () -> Unit,
) {
    val isJa = LocalIsJa.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isJa) "ウィジェットを追加" else "Add Widget") },
        text = {
            // スクロール可能なリスト
            LazyColumn(
                modifier = Modifier.heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                itemsIndexed(WidgetType.entries) { _, type ->
                    ListItem(
                        headlineContent = { Text(if (isJa) type.jaName else type.enName) },
                        leadingContent = {
                            Icon(
                                imageVector = widgetIcon(type),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        },
                        modifier = Modifier
                            .clickable { onAdd(type) }
                            .clip(RoundedCornerShape(8.dp)),
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(if (isJa) "キャンセル" else "Cancel") }
        },
    )
}

@Composable
private fun widgetIcon(type: WidgetType) = when (type) {
    WidgetType.CLOCK -> Icons.Default.Schedule
    WidgetType.CALENDAR -> Icons.Default.CalendarMonth
    WidgetType.MEMO -> Icons.Default.Note
    WidgetType.TIMER -> Icons.Default.Timer
    WidgetType.PROGRESS -> Icons.Default.TrendingUp
    WidgetType.AI -> Icons.Default.SmartToy
    WidgetType.DAILY_ALARM -> Icons.Default.Alarm
    WidgetType.DAILY_TASKS -> Icons.Default.Repeat
    WidgetType.WEBHOOK_BUTTONS -> Icons.Default.Send
    WidgetType.IMAGE -> Icons.Default.Image
}
