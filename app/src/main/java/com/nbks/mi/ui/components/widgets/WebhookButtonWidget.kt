package com.nbks.mi.ui.components.widgets

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nbks.mi.domain.model.WebhookButton
import com.nbks.mi.ui.components.WidgetHeader
import kotlinx.coroutines.delay

// ─────────────────────────────────────────────
// Webhook ボタン ウィジェット
// ─────────────────────────────────────────────
@Composable
fun WebhookButtonWidget(
    widgetKey: String,
    buttons: List<WebhookButton>,
    sendStatus: Map<Long, Boolean?>,
    isDarkTheme: Boolean,
    isEditMode: Boolean,
    onSend: (WebhookButton) -> Unit,
    onAdd: (WebhookButton) -> Unit,
    onEdit: (WebhookButton) -> Unit,
    onDelete: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<WebhookButton?>(null) }

    val myButtons = buttons.filter { it.widgetKey == widgetKey || widgetKey.isEmpty() }

    Column(modifier = modifier.fillMaxSize()) {
        WidgetHeader(
            title = "Webhook",
            icon = Icons.Default.Send,
            isDarkTheme = isDarkTheme,
            actions = {
                if (isEditMode) {
                    IconButton(onClick = { showAddDialog = true }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Add, contentDescription = "追加", modifier = Modifier.size(14.dp))
                    }
                }
            }
        )

        if (myButtons.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (isEditMode) "編集モードで＋から追加" else "編集モードでボタンを追加",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 100.dp),
                modifier = Modifier.fillMaxSize().padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                items(myButtons, key = { it.id }) { button ->
                    WebhookButtonItem(
                        button = button,
                        status = sendStatus[button.id],
                        isEditMode = isEditMode,
                        onSend = { onSend(button) },
                        onEdit = { editTarget = button },
                        onDelete = { onDelete(button.id) },
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        WebhookButtonEditDialog(
            initial = null,
            widgetKey = widgetKey,
            onConfirm = { onAdd(it); showAddDialog = false },
            onDismiss = { showAddDialog = false },
        )
    }

    editTarget?.let { target ->
        WebhookButtonEditDialog(
            initial = target,
            widgetKey = widgetKey,
            onConfirm = { onEdit(it); editTarget = null },
            onDismiss = { editTarget = null },
        )
    }
}

@Composable
private fun WebhookButtonItem(
    button: WebhookButton,
    status: Boolean?,     // null = 未送信, null key = 送信中, true = 成功, false = 失敗
    isEditMode: Boolean,
    onSend: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val buttonColor = Color(button.colorArgb)
    val isSending = status == null && false  // ステータスがマップに存在するかどうかで判断
    var localFeedback by remember(button.id, status) { mutableStateOf(status) }

    // フィードバック表示後リセット
    LaunchedEffect(status) {
        if (status == true || status == false) {
            delay(2000)
            localFeedback = null
        }
    }

    val surfaceColor = when (localFeedback) {
        true -> Color(0xFF4CAF50)
        false -> MaterialTheme.colorScheme.error
        else -> buttonColor
    }

    Box {
        Button(
            onClick = if (isEditMode) onEdit else onSend,
            modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = surfaceColor),
            shape = RoundedCornerShape(10.dp),
        ) {
            when (localFeedback) {
                true -> Icon(Icons.Default.Check, contentDescription = "成功", modifier = Modifier.size(18.dp))
                false -> Icon(Icons.Default.Close, contentDescription = "失敗", modifier = Modifier.size(18.dp))
                else -> Text(button.label, style = MaterialTheme.typography.labelMedium, maxLines = 2)
            }
        }

        if (isEditMode) {
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(20.dp)
                    .align(Alignment.TopEnd)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.error),
            ) {
                Icon(Icons.Default.Close, contentDescription = "削除",
                    modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onError)
            }
        }
    }
}

// ─── 追加 / 編集ダイアログ ───
@Composable
private fun WebhookButtonEditDialog(
    initial: WebhookButton?,
    widgetKey: String,
    onConfirm: (WebhookButton) -> Unit,
    onDismiss: () -> Unit,
) {
    var label by remember { mutableStateOf(initial?.label ?: "") }
    var webhookUrl by remember { mutableStateOf(initial?.webhookUrl ?: "") }
    var message by remember { mutableStateOf(initial?.message ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "ボタンを追加" else "ボタンを編集") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("ボタン名") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = webhookUrl,
                    onValueChange = { webhookUrl = it },
                    label = { Text("Webhook URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("https://discord.com/api/webhooks/...") },
                )
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("送信メッセージ") },
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        WebhookButton(
                            id = initial?.id ?: 0,
                            label = label.trim(),
                            webhookUrl = webhookUrl.trim(),
                            message = message.trim(),
                            widgetKey = widgetKey,
                        )
                    )
                },
                enabled = label.isNotBlank() && webhookUrl.isNotBlank() && message.isNotBlank(),
            ) { Text(if (initial == null) "追加" else "保存") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("キャンセル") } },
    )
}
