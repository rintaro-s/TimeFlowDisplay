package com.nbks.mi.ui.components.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nbks.mi.ui.components.WidgetHeader
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun CalendarWidget(
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
) {
    val today = LocalDate.now()
    val dateFormatter = DateTimeFormatter.ofPattern("M/d")
    val weekdays = listOf("月", "火", "水", "木", "金", "土", "日")

    Column(modifier = modifier.fillMaxSize()) {
        WidgetHeader(
            title = "カレンダー",
            icon = Icons.Default.CalendarMonth,
            isDarkTheme = isDarkTheme,
        )
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            for (i in 0..4) {
                val day = today.plusDays(i.toLong())
                val dowIndex = day.dayOfWeek.value - 1
                val isToday = i == 0
                val isTomorrow = i == 1
                val badge = when { isToday -> "今日"; isTomorrow -> "明日"; else -> null }
                val rowColor = if (isToday) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else Color.Transparent
                val dowColor = when { dowIndex == 6 -> MaterialTheme.colorScheme.error; dowIndex == 5 -> MaterialTheme.colorScheme.tertiary; else -> MaterialTheme.colorScheme.onSurface }
                Row(
                    modifier = Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(rowColor).padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(weekdays[dowIndex], style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = dowColor, modifier = Modifier.width(22.dp))
                    Text(day.format(dateFormatter), style = MaterialTheme.typography.bodyMedium, color = dowColor, modifier = Modifier.width(48.dp))
                    if (badge != null) {
                        Surface(shape = RoundedCornerShape(4.dp), color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary) {
                            Text(badge, style = MaterialTheme.typography.labelSmall, color = if (isToday) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }
            }
        }
    }
}

// ─── 共通ウィジェットプレースホルダー ───

@Composable
fun NotConnectedPlaceholder(icon: ImageVector, service: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
        Spacer(Modifier.height(8.dp))
        Text("$service 未設定", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
        Text("設定から連携してください", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f))
    }
}

@Composable
fun EmptyPlaceholder(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        )
    }
}
