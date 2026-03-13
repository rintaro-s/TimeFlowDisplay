package com.nbks.mi.ui.components.widgets

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ToggleOff
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import com.nbks.mi.domain.model.ClockStyle
import com.nbks.mi.ui.components.WidgetHeader
import com.nbks.mi.ui.theme.LocalIsJa
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.cos
import kotlin.math.sin

// ─────────────────────────────────────────────
// 時計ウィジェット（アナログ + デジタル）
// ─────────────────────────────────────────────
@Composable
fun ClockWidget(
    clockStyle: ClockStyle,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
) {
    val isJa = LocalIsJa.current
    var now by remember { mutableStateOf(LocalDateTime.now()) }

    // 毎秒更新
    LaunchedEffect(Unit) {
        while (true) {
            now = LocalDateTime.now()
            delay(1000)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        WidgetHeader(
            title = if (isJa) "時計" else "Clock",
            icon = Icons.Default.Schedule,
            isDarkTheme = isDarkTheme,
        )

        when (clockStyle) {
            ClockStyle.DIGITAL -> DigitalClock(now, modifier = Modifier.fillMaxSize())
            ClockStyle.ANALOG -> AnalogClock(now, isDarkTheme = isDarkTheme, modifier = Modifier.fillMaxSize())
            ClockStyle.BOTH -> {
                Row(
                    modifier = Modifier.fillMaxSize().padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AnalogClock(
                        now,
                        isDarkTheme = isDarkTheme,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    )
                    DigitalClock(
                        now,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    )
                }
            }
        }
    }
}

@Composable
private fun DigitalClock(
    now: LocalDateTime,
    modifier: Modifier = Modifier,
) {
    val isJa = LocalIsJa.current
    val timeStr = now.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
    val dateStr = if (isJa)
        now.format(DateTimeFormatter.ofPattern("yyyy年M月d日 (E)", java.util.Locale.JAPANESE))
    else
        now.format(DateTimeFormatter.ofPattern("EEE, MMM d yyyy", java.util.Locale.ENGLISH))

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = timeStr,
            style = MaterialTheme.typography.displaySmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Light,
                letterSpacing = 2.sp,
            ),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = dateStr,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AnalogClock(
    now: LocalDateTime,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
) {
    val hour = now.hour % 12
    val minute = now.minute
    val second = now.second

    val hourAngle = (hour * 30f) + (minute * 0.5f)
    val minuteAngle = minute * 6f
    val secondAngle = second * 6f

    val faceColor = if (isDarkTheme)
        Color(0xFF1E1E30) else Color(0xFFF5F3FF)
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val outline = MaterialTheme.colorScheme.outline

    Canvas(modifier = modifier.padding(8.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = minOf(size.width, size.height) / 2f - 4f

        // 文字盤背景
        drawCircle(color = faceColor, radius = radius, center = center)
        drawCircle(color = outline.copy(alpha = 0.3f), radius = radius, center = center, style = Stroke(2f))

        // 目盛り
        for (i in 0 until 60) {
            val angle = Math.toRadians((i * 6.0) - 90.0)
            val isHour = i % 5 == 0
            val tickStart = radius * if (isHour) 0.82f else 0.90f
            val tickEnd = radius * 0.96f
            drawLine(
                color = if (isHour) onSurface.copy(alpha = 0.6f) else onSurface.copy(alpha = 0.2f),
                start = Offset(
                    center.x + (tickStart * cos(angle)).toFloat(),
                    center.y + (tickStart * sin(angle)).toFloat(),
                ),
                end = Offset(
                    center.x + (tickEnd * cos(angle)).toFloat(),
                    center.y + (tickEnd * sin(angle)).toFloat(),
                ),
                strokeWidth = if (isHour) 3f else 1.5f,
                cap = StrokeCap.Round,
            )
        }

        // 時針
        rotate(hourAngle, center) {
            drawLine(
                color = onSurface,
                start = center.copy(y = center.y + radius * 0.15f),
                end = center.copy(y = center.y - radius * 0.55f),
                strokeWidth = 5f,
                cap = StrokeCap.Round,
            )
        }

        // 分針
        rotate(minuteAngle, center) {
            drawLine(
                color = onSurface.copy(alpha = 0.9f),
                start = center.copy(y = center.y + radius * 0.1f),
                end = center.copy(y = center.y - radius * 0.75f),
                strokeWidth = 3f,
                cap = StrokeCap.Round,
            )
        }

        // 秒針
        rotate(secondAngle, center) {
            drawLine(
                color = secondaryColor,
                start = center.copy(y = center.y + radius * 0.2f),
                end = center.copy(y = center.y - radius * 0.85f),
                strokeWidth = 1.5f,
                cap = StrokeCap.Round,
            )
        }

        // 中心点
        drawCircle(color = primaryColor, radius = 6f, center = center)
        drawCircle(color = onSurface, radius = 3f, center = center)
    }
}
