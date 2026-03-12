package com.nbks.mi.ui.components.widgets

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import com.nbks.mi.domain.model.Memo
import com.nbks.mi.ui.components.WidgetHeader
import com.nbks.mi.ui.theme.MiColors
import java.time.format.DateTimeFormatter

// ─────────────────────────────────────────────
// メモウィジェット
// ─────────────────────────────────────────────
@Composable
fun MemoWidget(
    memos: List<Memo>,
    isDarkTheme: Boolean,
    onNewMemo: () -> Unit,
    onEditMemo: (Memo) -> Unit,
    onDeleteMemo: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        WidgetHeader(
            title = "メモ",
            icon = Icons.Default.Note,
            isDarkTheme = isDarkTheme,
            actions = {
                IconButton(onClick = onNewMemo, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Add, contentDescription = "新規メモ", modifier = Modifier.size(16.dp))
                }
            }
        )

        if (memos.isEmpty()) {
            EmptyPlaceholder(message = "メモはありません\n右上の＋から追加できます")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                items(memos) { memo ->
                    MemoCard(
                        memo = memo,
                        isDarkTheme = isDarkTheme,
                        onClick = { onEditMemo(memo) },
                        onDelete = { onDeleteMemo(memo.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun MemoCard(
    memo: Memo,
    isDarkTheme: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val colorList = if (isDarkTheme) MiColors.MemoColors else MiColors.MemoColorsLight
    val cardColor = colorList.getOrElse(memo.colorIndex) { colorList.first() }
    val dateStr = memo.updatedAt.format(DateTimeFormatter.ofPattern("M/d HH:mm"))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
            Column(modifier = Modifier.fillMaxWidth().padding(end = 24.dp)) {
                if (memo.title.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (memo.isPinned) {
                            Icon(
                                Icons.Default.PushPin,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            )
                            Spacer(Modifier.width(4.dp))
                        }
                        Text(
                            text = memo.title,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Spacer(Modifier.height(2.dp))
                }
                Text(
                    text = memo.content,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                )
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.align(Alignment.TopEnd).size(20.dp),
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "削除",
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
// メモエディタダイアログ
// ─────────────────────────────────────────────
@Composable
fun MemoEditorDialog(
    memo: Memo?,
    isDarkTheme: Boolean,
    onSave: (title: String, content: String, colorIndex: Int, isPinned: Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    var title by remember(memo) { mutableStateOf(memo?.title ?: "") }
    var content by remember(memo) { mutableStateOf(memo?.content ?: "") }
    var colorIndex by remember(memo) { mutableIntStateOf(memo?.colorIndex ?: 0) }
    var isPinned by remember(memo) { mutableStateOf(memo?.isPinned ?: false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (memo?.id == 0L || memo == null) "新規メモ" else "メモを編集")
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { isPinned = !isPinned }, modifier = Modifier.size(32.dp)) {
                    Icon(
                        if (isPinned) Icons.Default.PushPin else Icons.Default.PushPin,
                        contentDescription = "ピン留め",
                        tint = if (isPinned) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("タイトル") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    placeholder = { Text("内容を入力...") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp, max = 260.dp),
                    maxLines = 12,
                )
                // カラーピッカー
                Text("色", style = MaterialTheme.typography.labelSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val colors = if (isDarkTheme) MiColors.MemoColors else MiColors.MemoColorsLight
                    colors.forEachIndexed { idx, color ->
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(color, RoundedCornerShape(4.dp))
                                .border(
                                    width = if (idx == colorIndex) 2.dp else 0.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(4.dp),
                                )
                                .clickable { colorIndex = idx },
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(title, content, colorIndex, isPinned) },
                enabled = content.isNotBlank() || title.isNotBlank(),
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("キャンセル") }
        },
    )
}
