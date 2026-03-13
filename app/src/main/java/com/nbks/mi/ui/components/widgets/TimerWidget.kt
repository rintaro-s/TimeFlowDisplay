package com.nbks.mi.ui.components.widgets

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.nbks.mi.domain.model.TimerPreset
import com.nbks.mi.domain.model.TimerState
import com.nbks.mi.ui.components.WidgetHeader
import com.nbks.mi.ui.theme.LocalIsJa

private val QUICK_PRESETS = listOf(
    "1m" to 60L, "3m" to 180L, "5m" to 300L,
    "10m" to 600L, "25m" to 1500L, "30m" to 1800L, "1h" to 3600L,
)

@Composable
fun TimerWidget(
    timerState: TimerState,
    presets: List<TimerPreset>,
    isDarkTheme: Boolean,
    onStart: (Long, String, Boolean) -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    onLoadPreset: (TimerPreset) -> Unit,
    onSavePreset: (TimerPreset) -> Unit,
    onDeletePreset: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isJa = LocalIsJa.current
    val isRunningOrPaused = timerState.isRunning || timerState.isPaused

    Column(modifier = modifier.fillMaxSize()) {
        WidgetHeader(
            title = if (isJa) "タイマー" else "Timer",
            icon = Icons.Default.Timer,
            isDarkTheme = isDarkTheme,
        )
        if (isRunningOrPaused) {
            TimerRunningView(state = timerState, isJa = isJa, onPause = onPause, onResume = onResume, onStop = onStop)
        } else {
            TimerSetupView(
                presets = presets.filter { !it.isDaily },
                isJa = isJa,
                onStart = onStart,
                onLoadPreset = onLoadPreset,
            )
        }
    }
}

@Composable
private fun TimerSetupView(
    presets: List<TimerPreset>,
    isJa: Boolean,
    onStart: (Long, String, Boolean) -> Unit,
    onLoadPreset: (TimerPreset) -> Unit,
) {
    var minuteText by remember { mutableStateOf("5") }
    var secondText by remember { mutableStateOf("0") }
    var labelText by remember { mutableStateOf("") }
    var isSilent by remember { mutableStateOf(false) }

    val totalMillis: Long = run {
        val m = minuteText.toLongOrNull() ?: 0L
        val s = secondText.toLongOrNull() ?: 0L
        (m * 60 + s) * 1000L
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            items(QUICK_PRESETS) { (label, secs) ->
                SuggestionChip(
                    onClick = { minuteText = (secs / 60).toString(); secondText = (secs % 60).toString() },
                    label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            TimerNumberField(
                value = minuteText,
                label = if (isJa) "分" else "min",
                onChange = { minuteText = it.filter { c -> c.isDigit() }.take(3) },
                modifier = Modifier.width(72.dp),
            )
            Text(":", style = MaterialTheme.typography.headlineLarge.copy(fontFamily = FontFamily.Monospace), modifier = Modifier.padding(horizontal = 4.dp))
            TimerNumberField(
                value = secondText,
                label = if (isJa) "秒" else "sec",
                onChange = { val v = it.filter { c -> c.isDigit() }.take(2); secondText = if ((v.toLongOrNull() ?: 0) > 59) "59" else v },
                modifier = Modifier.width(72.dp),
            )
        }
        OutlinedTextField(
            value = labelText,
            onValueChange = { labelText = it },
            placeholder = { Text(if (isJa) "ラベル (任意)" else "Label (optional)", style = MaterialTheme.typography.labelSmall) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodySmall,
            trailingIcon = {
                IconToggleButton(checked = isSilent, onCheckedChange = { isSilent = it }, modifier = Modifier.size(24.dp)) {
                    Icon(
                        if (isSilent) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                        contentDescription = null, modifier = Modifier.size(16.dp),
                        tint = if (isSilent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
        )
        if (presets.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                items(presets) { preset ->
                    FilterChip(selected = false, onClick = { onLoadPreset(preset) }, label = { Text(preset.name, style = MaterialTheme.typography.labelSmall) })
                }
            }
        }
        Button(onClick = { onStart(totalMillis, labelText, isSilent) }, modifier = Modifier.fillMaxWidth(), enabled = totalMillis > 0) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(4.dp))
            Text(text = formatDuration(totalMillis), style = MaterialTheme.typography.labelMedium)
            Text(if (isJa) " 開始" else " Start", style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun TimerNumberField(value: String, label: String, onChange: (String) -> Unit, modifier: Modifier = Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedTextField(
            value = value, onValueChange = onChange, modifier = modifier,
            textStyle = MaterialTheme.typography.headlineMedium.copy(fontFamily = FontFamily.Monospace),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true,
        )
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun formatDuration(ms: Long): String {
    val totalSecs = ms / 1000
    val h = totalSecs / 3600
    val m = (totalSecs % 3600) / 60
    val s = totalSecs % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}

@Composable
private fun TimerRunningView(state: TimerState, isJa: Boolean, onPause: () -> Unit, onResume: () -> Unit, onStop: () -> Unit) {
    val remaining = state.remainingMillis
    val hours = remaining / 3600000
    val minutes = (remaining % 3600000) / 60000
    val seconds = (remaining % 60000) / 1000

    val pulseAnim = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by pulseAnim.animateFloat(
        initialValue = 1f, targetValue = if (state.isRunning) 0.4f else 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "alpha",
    )

    Column(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (state.label.isNotEmpty()) {
            Text(state.label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { state.progress }, modifier = Modifier.size(96.dp),
                strokeWidth = 6.dp, strokeCap = StrokeCap.Round,
                color = MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            Text(
                text = if (hours > 0) "%d:%02d:%02d".format(hours, minutes, seconds) else "%02d:%02d".format(minutes, seconds),
                style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.primary.copy(alpha = if (state.isRunning) pulseAlpha else 1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(onClick = onStop, modifier = Modifier.height(36.dp)) {
                Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(if (isJa) "停止" else "Stop", style = MaterialTheme.typography.labelMedium)
            }
            Button(onClick = if (state.isRunning) onPause else onResume, modifier = Modifier.height(36.dp)) {
                Icon(if (state.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(if (state.isRunning) (if (isJa) "一時停止" else "Pause") else (if (isJa) "再開" else "Resume"), style = MaterialTheme.typography.labelMedium)
            }
        }
        if (state.isSilent) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.VolumeOff, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                Spacer(Modifier.width(4.dp))
                Text(if (isJa) "サイレント" else "Silent", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
            }
        }
    }
}
