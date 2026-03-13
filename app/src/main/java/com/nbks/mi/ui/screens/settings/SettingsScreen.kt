package com.nbks.mi.ui.screens.settings

import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nbks.mi.domain.model.*
import com.nbks.mi.ui.theme.LocalIsJa
import com.nbks.mi.ui.viewmodel.SettingsViewModel

// ─────────────────────────────────────────────
// 設定画面
// ─────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val schedules by viewModel.schedules.collectAsStateWithLifecycle()
    val isJa = LocalIsJa.current

    var currentSection by remember { mutableStateOf<SettingsSection?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (currentSection == null) (if (isJa) "設定" else "Settings")
                        else if (isJa) currentSection!!.jaTitle else currentSection!!.enTitle,
                        style = MaterialTheme.typography.titleMedium,
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (currentSection != null) currentSection = null
                            else onNavigateBack()
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = if (isJa) "戻る" else "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        }
    ) { paddingValues ->
        AnimatedContent(
            targetState = currentSection,
            modifier = Modifier.padding(paddingValues),
            transitionSpec = {
                if (targetState != null) {
                    slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                } else {
                    slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                }
            },
            label = "settings_nav",
        ) { section ->
            if (section == null) {
                SettingsMainMenu(onSectionSelect = { currentSection = it })
            } else {
                when (section) {
                    SettingsSection.DISPLAY -> DisplaySettings(
                        settings = settings,
                        onDarkModeChange = viewModel::setDarkMode,
                        onDynamicColorChange = viewModel::setDynamicColor,
                        onAppLanguageChange = viewModel::setAppLanguage,
                        onClockStyleChange = viewModel::setClockStyle,
                        onWallpaperUriChange = viewModel::setWallpaperUri,
                        onWallpaperDimChange = viewModel::setWallpaperDimAlpha,
                        onKeepScreenOnChange = viewModel::setKeepScreenOn,
                        onWidgetOpacityChange = viewModel::setWidgetOpacity,
                    )
                    SettingsSection.LMSTUDIO -> LMStudioSettings(
                        settings = settings,
                        onUrlChange = viewModel::setLmStudioUrl,
                        onModelChange = viewModel::setLmStudioModel,
                        onMaxTokensChange = viewModel::setLmStudioMaxTokens,
                    )
                    SettingsSection.SCHEDULE -> ScheduleSettings(
                        settings = settings,
                        schedules = schedules,
                        onScheduleScreenDimChange = viewModel::setScheduleScreenDim,
                        onSaveSchedule = viewModel::saveSchedule,
                        onUpdateSchedule = viewModel::updateSchedule,
                        onDeleteSchedule = viewModel::deleteSchedule,
                    )
                }
            }
        }
    }
}

enum class SettingsSection(
    val jaTitle: String,
    val enTitle: String,
    val icon: ImageVector,
    val jaDescription: String,
    val enDescription: String,
) {
    DISPLAY("表示・テーマ", "Display", Icons.Default.Palette, "ダーク/ライト、壁紙、時計スタイル", "Theme, wallpaper, clock style"),
    LMSTUDIO("AI (LMStudio)", "AI (LMStudio)", Icons.Default.SmartToy, "外部AIサーバーの接続設定", "External AI server connection"),
    SCHEDULE("スケジュール", "Schedule", Icons.Default.Schedule, "時間帯による画面制御", "Screen behavior by time"),
}

@Composable
private fun SettingsMainMenu(onSectionSelect: (SettingsSection) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(SettingsSection.entries) { section ->
            SettingsSectionCard(section = section, onClick = { onSectionSelect(section) })
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun SettingsSectionCard(section: SettingsSection, onClick: () -> Unit) {
    val isJa = LocalIsJa.current
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    section.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(if (isJa) section.jaTitle else section.enTitle, style = MaterialTheme.typography.titleSmall)
                Text(
                    if (isJa) section.jaDescription else section.enDescription,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

// ─────────────────────────────────────────────
// 表示設定
// ─────────────────────────────────────────────
@Composable
fun DisplaySettings(
    settings: AppSettings,
    onDarkModeChange: (Boolean) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit,
    onAppLanguageChange: (AppLanguage) -> Unit,
    onClockStyleChange: (ClockStyle) -> Unit,
    onWallpaperUriChange: (String) -> Unit,
    onWallpaperDimChange: (Float) -> Unit,
    onKeepScreenOnChange: (Boolean) -> Unit,
    onWidgetOpacityChange: (Float) -> Unit,
) {
    val context = LocalContext.current
    val isJa = LocalIsJa.current
    val wallpaperLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onWallpaperUriChange(it.toString()) }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            SettingsCategoryLabel(if (isJa) "テーマ" else "Theme")
            SettingsToggleItem(
                title = if (isJa) "ダークモード" else "Dark mode",
                subtitle = if (isJa) "アプリの配色を暗くします" else "Use a dark color scheme",
                icon = Icons.Default.DarkMode,
                checked = settings.isDarkMode,
                onCheckedChange = onDarkModeChange,
            )
        }
        item {
            SettingsToggleItem(
                title = if (isJa) "ダイナミックカラー" else "Dynamic color",
                subtitle = if (isJa) "Android 12+ の壁紙カラーを使用" else "Use wallpaper colors on Android 12+",
                icon = Icons.Default.ColorLens,
                checked = settings.useDynamicColor,
                onCheckedChange = onDynamicColorChange,
            )
        }
        item {
            SettingsCategoryLabel(if (isJa) "言語" else "Language")
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                AppLanguage.entries.forEach { language ->
                    val label = when (language) {
                        AppLanguage.SYSTEM -> if (isJa) "システムに合わせる" else "Follow system"
                        AppLanguage.JAPANESE -> "日本語"
                        AppLanguage.ENGLISH -> "English"
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAppLanguageChange(language) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = settings.appLanguage == language,
                            onClick = { onAppLanguageChange(language) },
                        )
                        Text(label, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
        item {
            SettingsCategoryLabel(if (isJa) "時計スタイル" else "Clock style")
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                ClockStyle.entries.forEach { style ->
                    val label = when (style) {
                        ClockStyle.ANALOG -> if (isJa) "アナログ" else "Analog"
                        ClockStyle.DIGITAL -> if (isJa) "デジタル" else "Digital"
                        ClockStyle.BOTH -> if (isJa) "両方" else "Both"
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onClockStyleChange(style) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = settings.clockStyle == style,
                            onClick = { onClockStyleChange(style) },
                        )
                        Text(label, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
        item {
            SettingsCategoryLabel(if (isJa) "壁紙" else "Wallpaper")
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (settings.wallpaperUri.isNotEmpty()) {
                                if (isJa) "壁紙が設定されています" else "Wallpaper is set"
                            } else {
                                if (isJa) "壁紙未設定" else "No wallpaper selected"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f),
                        )
                        if (settings.wallpaperUri.isNotEmpty()) {
                            TextButton(onClick = { onWallpaperUriChange("") }) {
                                Text(if (isJa) "削除" else "Remove", color = MaterialTheme.colorScheme.error)
                            }
                        }
                        Button(onClick = { wallpaperLauncher.launch("image/*") }) {
                            Text(if (isJa) "選択" else "Choose", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    if (settings.wallpaperUri.isNotEmpty()) {
                        Text(if (isJa) "壁紙の暗さ: ${(settings.wallpaperDimAlpha * 100).toInt()}%" else "Wallpaper dim: ${(settings.wallpaperDimAlpha * 100).toInt()}%")
                        Slider(
                            value = settings.wallpaperDimAlpha,
                            onValueChange = onWallpaperDimChange,
                            valueRange = 0f..0.9f,
                        )
                    }
                }
            }
        }
        item {
            SettingsCategoryLabel(if (isJa) "ウィジェット" else "Widgets")
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(if (isJa) "ウィジェットの不透明度: ${(settings.widgetOpacity * 100).toInt()}%" else "Widget opacity: ${(settings.widgetOpacity * 100).toInt()}%")
                Slider(
                    value = settings.widgetOpacity,
                    onValueChange = onWidgetOpacityChange,
                    valueRange = 0.3f..1f,
                )
            }
        }
        item {
            SettingsCategoryLabel(if (isJa) "その他" else "Other")
            SettingsToggleItem(
                title = if (isJa) "画面常時点灯" else "Keep screen on",
                subtitle = if (isJa) "充電中など画面が自動消灯しないよう" else "Prevent the screen from sleeping",
                icon = Icons.Default.ScreenLockLandscape,
                checked = settings.keepScreenOn,
                onCheckedChange = onKeepScreenOnChange,
            )
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}

// ─────────────────────────────────────────────
// LMStudio設定
// ─────────────────────────────────────────────
@Composable
fun LMStudioSettings(
    settings: AppSettings,
    onUrlChange: (String) -> Unit,
    onModelChange: (String) -> Unit,
    onMaxTokensChange: (Int) -> Unit,
) {
    var urlText by remember(settings.lmStudioBaseUrl) { mutableStateOf(settings.lmStudioBaseUrl) }
    var modelText by remember(settings.lmStudioModel) { mutableStateOf(settings.lmStudioModel) }
    var maxTokensText by remember(settings.lmStudioMaxTokens) { mutableStateOf(settings.lmStudioMaxTokens.toString()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)),
            ) {
                Row(modifier = Modifier.padding(12.dp)) {
                    Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.tertiary)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Start LMStudio (https://lmstudio.ai) and enable the OpenAI-compatible server.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                }
            }
        }
        item {
            SettingsCategoryLabel("Connection")
            OutlinedTextField(
                value = urlText,
                onValueChange = {
                    urlText = it
                    onUrlChange(it)
                },
                label = { Text("Server URL") },
                placeholder = { Text("http://192.168.1.x:1234") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        }
        item {
            OutlinedTextField(
                value = modelText,
                onValueChange = {
                    modelText = it
                    onModelChange(it)
                },
                label = { Text("Model") },
                placeholder = { Text("local-model") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        }
        item {
            OutlinedTextField(
                value = maxTokensText,
                onValueChange = { text ->
                    maxTokensText = text
                    text.toIntOrNull()?.let { onMaxTokensChange(it) }
                },
                label = { Text("Max Tokens") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}

// ─────────────────────────────────────────────
// スケジュール設定
// ─────────────────────────────────────────────
@Composable
fun ScheduleSettings(
    settings: AppSettings,
    schedules: List<ScreenSchedule>,
    onScheduleScreenDimChange: (Boolean) -> Unit,
    onSaveSchedule: (ScreenSchedule) -> Unit,
    onUpdateSchedule: (ScreenSchedule) -> Unit,
    onDeleteSchedule: (Long) -> Unit,
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            SettingsToggleItem(
                title = "Enable schedule-based dimming",
                subtitle = "Dim the screen during configured time ranges",
                icon = Icons.Default.BrightnessLow,
                checked = settings.scheduleScreenDim,
                onCheckedChange = onScheduleScreenDimChange,
            )
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Schedules",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add")
                }
            }
        }
        if (schedules.isEmpty()) {
            item {
                Text(
                    "No schedules",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                )
            }
        } else {
            items(schedules) { schedule ->
                ScheduleItem(
                    schedule = schedule,
                    onToggle = { onUpdateSchedule(schedule.copy(isEnabled = !schedule.isEnabled)) },
                    onDelete = { onDeleteSchedule(schedule.id) },
                )
            }
        }
        item { Spacer(Modifier.height(80.dp)) }
    }

    if (showAddDialog) {
        ScheduleDialog(
            schedule = null,
            onSave = { onSaveSchedule(it); showAddDialog = false },
            onDismiss = { showAddDialog = false },
        )
    }
}

@Composable
private fun ScheduleItem(
    schedule: ScreenSchedule,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
) {
    val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")
    val activeDays = (0..6).filter { schedule.dayOfWeekBitmask and (1 shl it) != 0 }
        .joinToString("") { dayLabels[it] }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (schedule.isEnabled)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Default.BrightnessLow,
                contentDescription = null,
                tint = if (schedule.isEnabled) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                if (schedule.label.isNotEmpty()) {
                    Text(schedule.label, style = MaterialTheme.typography.labelMedium)
                }
                Text(
                    "%02d:%02d - %02d:%02d   Brightness: %d%%  [%s]".format(
                        schedule.startHour, schedule.startMinute,
                        schedule.endHour, schedule.endMinute,
                        (schedule.brightness * 100).toInt(),
                        if (activeDays.length == 7) "Every day" else activeDays,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(checked = schedule.isEnabled, onCheckedChange = { onToggle() }, modifier = Modifier.height(24.dp))
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
private fun ScheduleDialog(
    schedule: ScreenSchedule?,
    onSave: (ScreenSchedule) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")
    var label by remember { mutableStateOf(schedule?.label ?: "") }
    var startHour by remember { mutableIntStateOf(schedule?.startHour ?: 22) }
    var startMinute by remember { mutableIntStateOf(schedule?.startMinute ?: 0) }
    var endHour by remember { mutableIntStateOf(schedule?.endHour ?: 7) }
    var endMinute by remember { mutableIntStateOf(schedule?.endMinute ?: 0) }
    var brightness by remember { mutableFloatStateOf(schedule?.brightness ?: 0f) }
    var dayBitmask by remember { mutableIntStateOf(schedule?.dayOfWeekBitmask ?: 0x7F) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Schedule") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Label (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                // 曜日選択
                Text("Days", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    dayLabels.forEachIndexed { i, d ->
                        val selected = dayBitmask and (1 shl i) != 0
                        FilterChip(
                            selected = selected,
                            onClick = { dayBitmask = if (selected) dayBitmask and (1 shl i).inv() else dayBitmask or (1 shl i) },
                            label = { Text(d, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = {
                            TimePickerDialog(context, { _, h, m ->
                                startHour = h; startMinute = m
                            }, startHour, startMinute, true).show()
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Start: %02d:%02d".format(startHour, startMinute))
                    }
                    OutlinedButton(
                        onClick = {
                            TimePickerDialog(context, { _, h, m ->
                                endHour = h; endMinute = m
                            }, endHour, endMinute, true).show()
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("End: %02d:%02d".format(endHour, endMinute))
                    }
                }
                Text("Brightness: ${(brightness * 100).toInt()}% (0% = black)")
                Slider(
                    value = brightness,
                    onValueChange = { brightness = it },
                    valueRange = 0f..1f,
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(
                    ScreenSchedule(
                        id = schedule?.id ?: 0,
                        label = label,
                        startHour = startHour,
                        startMinute = startMinute,
                        endHour = endHour,
                        endMinute = endMinute,
                        brightness = brightness,
                        dayOfWeekBitmask = dayBitmask,
                    )
                )
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

// ─────────────────────────────────────────────
// 共通コンポーネント
// ─────────────────────────────────────────────
@Composable
fun SettingsCategoryLabel(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 4.dp),
    )
}

@Composable
fun SettingsToggleItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyMedium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}
