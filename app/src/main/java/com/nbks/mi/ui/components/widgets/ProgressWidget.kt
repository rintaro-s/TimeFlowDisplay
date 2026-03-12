package com.nbks.mi.ui.components.widgets

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.nbks.mi.domain.model.ProgressData
import com.nbks.mi.ui.components.WidgetHeader
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.temporal.ChronoUnit

// ─────────────────────────────────────────────
// 進捗バーウィジェット
// ─────────────────────────────────────────────
@Composable
fun ProgressWidget(
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
) {
    val now = LocalDate.now()
    val time = LocalTime.now()

    val todayProgress = run {
        val totalMinutes = 24 * 60f
        val elapsed = time.hour * 60f + time.minute + time.second / 60f
        ProgressData(
            progress = (elapsed / totalMinutes).coerceIn(0f, 1f),
            label = "今日",
            sublabel = "${time.hour}:${"%02d".format(time.minute)}",
        )
    }

    val weekProgress = run {
        val dayOfWeekVal = now.dayOfWeek.value // Mon=1..Sun=7
        ProgressData(
            progress = ((dayOfWeekVal - 1) / 6f + (time.hour / 24f) / 7f).coerceIn(0f, 1f),
            label = "今週",
            sublabel = "${now.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.JAPANESE)}",
        )
    }

    val monthProgress = run {
        val daysInMonth = YearMonth.now().lengthOfMonth().toFloat()
        ProgressData(
            progress = ((now.dayOfMonth - 1 + time.hour / 24f) / daysInMonth).coerceIn(0f, 1f),
            label = "今月",
            sublabel = "${now.monthValue}月",
        )
    }

    val yearProgress = run {
        val dayOfYear = now.dayOfYear.toFloat()
        val daysInYear = (if (now.isLeapYear) 366 else 365).toFloat()
        ProgressData(
            progress = (dayOfYear / daysInYear).coerceIn(0f, 1f),
            label = "今年",
            sublabel = "${now.year}",
        )
    }

    Column(modifier = modifier.fillMaxSize()) {
        WidgetHeader(
            title = "時間進捗",
            icon = Icons.Default.TrendingUp,
            isDarkTheme = isDarkTheme,
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
        ) {
            listOf(todayProgress, weekProgress, monthProgress, yearProgress).forEach { data ->
                ProgressBarItem(data = data)
            }
        }
    }
}

@Composable
private fun ProgressBarItem(data: ProgressData) {
    val animatedProgress by animateFloatAsState(
        targetValue = data.progress,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "progress_${data.label}",
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = data.label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(36.dp),
        )
        Spacer(Modifier.width(4.dp))
        Box(modifier = Modifier.weight(1f).height(8.dp)) {
            // 背景
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRoundRect(
                    color = Color.Gray.copy(alpha = 0.2f),
                    size = size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f),
                )
                // 進捗
                val primary = Color(0xFF9C82F0)
                val secondary = Color(0xFF62D0C0)
                drawRoundRect(
                    brush = Brush.horizontalGradient(listOf(primary, secondary)),
                    size = Size(size.width * animatedProgress, size.height),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f),
                )
            }
        }
        Spacer(Modifier.width(6.dp))
        Text(
            text = "${(data.progress * 100).toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(32.dp),
            textAlign = TextAlign.End,
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = data.sublabel,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(28.dp),
        )
    }
}
