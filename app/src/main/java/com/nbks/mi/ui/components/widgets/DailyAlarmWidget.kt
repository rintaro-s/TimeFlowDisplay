package com.nbks.mi.ui.components.widgets

import android.app.TimePickerDialog
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nbks.mi.domain.model.DailyAlarmStatus
import com.nbks.mi.domain.model.TimerPreset
import com.nbks.mi.ui.components.WidgetHeader

private val CARD_COLORS = listOf(
    Color(0xFF1B5E20),
    Color(0xFF0D47A1),
    Color(0xFF4A148C),
    Color(0xFF880E4F),
    Color(0xFF1A237E),
    Color(0xFF004D40),
    Color(0xFFE65100),
    Color(0xFF37474F),
)

@Composable
fun DailyAlarmWidget(
    alarms: List<DailyAlarmStatus>,
    isDarkTheme: Boolean,
    onSave: (TimerPreset) -> Unit,
    onDelete: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        WidgetHeader(
            title = if (java.util.Locale.getDefault().language == "ja") "時刻アラーム" else "Daily Alarms",
            icon = Icons.Default.Alarm,
            isDarkTheme = isDarkTheme,
            actions = {
                IconButton(onClick = { showAddDialog = true }, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(18.dp))
                }
            }
        )

        if (alarms.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Alarm, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                    Text(
                        if (java.util.Locale.getDefault().language == "ja") "＋で時刻を追加\n毎日自動リセット" else "Tap ＋ to add alarms\nAuto-reset daily",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    )
                }
            }
        } else {
            // アクティブ→通過済みの順でソート
            val sorted = alarms.sortedWith(compareBy({ it.remainingMs < 0 }, { if (it.remainingMs >= 0) it.remainingMs else it.preset.dailyHour * 60 + it.preset.dailyMinute }))
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                itemsIndexed(sorted) { index, status ->
                    DailyAlarmCard(
                        status = status,
                        cardColor = CARD_COLORS[index % CARD_COLORS.size],
                        onDelete = { onDelete(status.preset.id) },
                    )
                }
                item { Spacer(Modifier.height(4.dp)) }
            }
        }
    }

    if (showAddDialog) {
        AddAlarmDialog(
            onSave = { preset -> onSave(preset); showAddDialog = false },
            onDismiss = { showAddDialog = false },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DailyAlarmCard(
    status: DailyAlarmStatus,
    cardColor: Color,
    onDelete: () -> Unit,
) {
    val passed = status.remainingMs < 0
    val remainMs = if (passed) 0L else status.remainingMs
    val h = remainMs / 3_600_000
    val m = (remainMs % 3_600_000) / 60_000
    val s = (remainMs % 60_000) / 1000
    val isJa = java.util.Locale.getDefault().language == "ja"

    val countdownText = when {
        passed -> if (isJa) "通過済み" else "Passed"
        h > 0 -> if (isJa) "%d時間%02d分%02d秒".format(h, m, s) else "%dh %02dm %02ds".format(h, m, s)
        m > 0 -> if (isJa) "%d分%02d秒".format(m, s) else "%dm %02ds".format(m, s)
        else -> if (isJa) "%d秒".format(s) else "%ds".format(s)
    }

    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = {}, onLongClick = { showDeleteConfirm = true })
            .alpha(if (passed) 0.45f else 1f),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (passed) 0.dp else 3.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                // 大きな時刻表示
                Text(
                    text = "%02d:%02d".format(status.preset.dailyHour, status.preset.dailyMinute),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                // ラベル (あれば)
                val label = status.preset.name
                val timeStr = "%02d:%02d".format(status.preset.dailyHour, status.preset.dailyMinute)
                if (label.isNotEmpty() && label != timeStr) {
                    Text(
                        text = label,
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.75f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                // カウントダウン
                Text(
                    text = if (passed) countdownText else (if (isJa) "あと $countdownText" else "in $countdownText"),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = if (passed) 0.6f else 0.9f),
                )
            }
            // 削除ボタン
            IconButton(
                onClick = { showDeleteConfirm = true },
                modifier = Modifier
                    .size(36.dp)
                    .background(Color.White.copy(alpha = 0.15f), CircleShape),
            ) {
                Icon(Icons.Default.Close, contentDescription = if (isJa) "削除" else "Delete", tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(if (isJa) "削除確認" else "Delete") },
            text = { Text(if (isJa) "「${status.preset.name}」を削除しますか？" else "Delete \"${status.preset.name}\"?") },
            confirmButton = { TextButton(onClick = { onDelete(); showDeleteConfirm = false }) { Text(if (isJa) "削除" else "Delete") } },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text(if (isJa) "キャンセル" else "Cancel") } },
        )
    }
}

@Composable
private fun AddAlarmDialog(
    onSave: (TimerPreset) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val isJa = java.util.Locale.getDefault().language == "ja"
    var pickedHour by remember { mutableIntStateOf(8) }
    var pickedMinute by remember { mutableIntStateOf(0) }
    var label by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isJa) "時刻アラームを追加" else "Add Daily Alarm") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = {
                        TimePickerDialog(context, { _, h, mn ->
                            pickedHour = h; pickedMinute = mn
                        }, pickedHour, pickedMinute, true).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "%02d:%02d".format(pickedHour, pickedMinute),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                    )
                }
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    placeholder = { Text(if (isJa) "ラベル (任意)" else "Label (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(TimerPreset(
                    name = label.ifBlank { "%02d:%02d".format(pickedHour, pickedMinute) },
                    durationMillis = 0,
                    isDaily = true,
                    dailyHour = pickedHour,
                    dailyMinute = pickedMinute,
                ))
            }) { Text(if (isJa) "追加" else "Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(if (isJa) "キャンセル" else "Cancel") } },
    )
}
