package com.nbks.mi.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.*
import androidx.compose.ui.zIndex
import com.nbks.mi.domain.model.WidgetConfig
import com.nbks.mi.domain.model.WidgetType
import com.nbks.mi.ui.theme.MiColors
import kotlin.math.roundToInt

// ─────────────────────────────────────────────
// ドラッグ可能ウィジェットコンテナ
// ─────────────────────────────────────────────
@Composable
fun DraggableWidget(
    config: WidgetConfig,
    isEditMode: Boolean,
    isDarkTheme: Boolean,
    widgetOpacity: Float,
    onPositionChanged: (Float, Float) -> Unit,
    onSizeChanged: (Int, Int) -> Unit,
    onBringToFront: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val density = LocalDensity.current

    var offsetX by remember(config.id) { mutableFloatStateOf(config.positionX) }
    var offsetY by remember(config.id) { mutableFloatStateOf(config.positionY) }
    var widthDp by remember(config.id) { mutableIntStateOf(config.widthDp) }
    var heightDp by remember(config.id) { mutableIntStateOf(config.heightDp) }

    // アニメーション
    val borderAlpha by animateFloatAsState(
        targetValue = if (isEditMode) 1f else 0f,
        animationSpec = tween(200),
        label = "border",
    )
    val elevation by animateDpAsState(
        targetValue = if (isEditMode) 12.dp else 4.dp,
        label = "elevation",
    )

    val bgColor = if (isDarkTheme) MiColors.WidgetBgDark else MiColors.WidgetBgLight
    val borderColor = if (isDarkTheme) MiColors.WidgetBorderDark else MiColors.WidgetBorderLight

    Box(
        modifier = modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .zIndex(config.zIndex.toFloat())
            .width(widthDp.dp)
            .height(heightDp.dp)
    ) {
        // ウィジェット本体
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (isEditMode) Modifier.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = borderAlpha * 0.8f),
                        shape = RoundedCornerShape(16.dp),
                    ) else Modifier
                )
                .pointerInput(config.id, isEditMode) {
                    if (isEditMode) {
                        detectDragGestures(
                            onDragStart = { onBringToFront() },
                            onDragEnd = { onPositionChanged(offsetX, offsetY) },
                        ) { change, dragAmount ->
                            change.consume()
                            offsetX += dragAmount.x
                            offsetY += dragAmount.y
                            offsetX = offsetX.coerceAtLeast(0f)
                            offsetY = offsetY.coerceAtLeast(0f)
                        }
                    }
                },
            shape = RoundedCornerShape(16.dp),
            color = bgColor.copy(alpha = widgetOpacity),
            shadowElevation = elevation,
            tonalElevation = 2.dp,
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                content()
            }
        }

        // 編集モード: 削除ボタン
        if (isEditMode) {
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 10.dp, y = (-10).dp)
                    .size(28.dp)
                    .background(
                        color = MaterialTheme.colorScheme.error,
                        shape = CircleShape,
                    )
                    .zIndex(10f),
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onError,
                    modifier = Modifier.size(16.dp),
                )
            }

            // リサイズハンドル（右下）
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 6.dp, y = 6.dp)
                    .size(24.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(4.dp),
                    )
                    .zIndex(10f)
                    .pointerInput(config.id) {
                        detectDragGestures(
                            onDragEnd = { onSizeChanged(widthDp, heightDp) }
                        ) { change, dragAmount ->
                            change.consume()
                            widthDp = (widthDp + (dragAmount.x / density.density).toInt()).coerceIn(180, 800)
                            heightDp = (heightDp + (dragAmount.y / density.density).toInt()).coerceIn(100, 900)
                        }
                    },
            ) {
                Icon(
                    Icons.Default.OpenInFull,
                    contentDescription = "Resize",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .size(14.dp)
                        .align(Alignment.Center),
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
// ウィジェット共通ヘッダー
// ─────────────────────────────────────────────
@Composable
fun WidgetHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        actions()
    }
    HorizontalDivider(
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
    )
}
