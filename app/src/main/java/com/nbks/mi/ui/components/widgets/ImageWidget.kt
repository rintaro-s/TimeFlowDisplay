package com.nbks.mi.ui.components.widgets

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.nbks.mi.ui.components.WidgetHeader
import com.nbks.mi.ui.theme.LocalIsJa
import org.json.JSONObject

// ─────────────────────────────────────────────
// 画像ウィジェット
// customSettings JSON: {"imageUri": "content://..."}
// ─────────────────────────────────────────────
@Composable
fun ImageWidget(
    customSettings: String,
    isDarkTheme: Boolean,
    isEditMode: Boolean,
    onUpdateSettings: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val isJa = LocalIsJa.current
    val imageUri: String? = remember(customSettings) {
        runCatching { JSONObject(customSettings).optString("imageUri", "") }
            .getOrDefault("").ifBlank { null }
    }

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            // 永続的な読み取り権限を取得 (一部URIでは失敗するためtry-catch)
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            } catch (_: SecurityException) { }
            val newSettings = JSONObject().apply { put("imageUri", uri.toString()) }.toString()
            onUpdateSettings(newSettings)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        WidgetHeader(
            title = if (isJa) "画像" else "Image",
            icon = Icons.Default.Image,
            isDarkTheme = isDarkTheme,
            actions = {
                if (isEditMode) {
                    IconButton(
                        onClick = { picker.launch("image/*") },
                        modifier = Modifier.size(24.dp),
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = if (isJa) "画像を選択" else "Select image",
                            modifier = Modifier.size(14.dp))
                    }
                }
            }
        )

        Box(modifier = Modifier.fillMaxSize()) {
            if (imageUri != null) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = "配置画像",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            if (isEditMode) (if (isJa) "編集モードで画像を選択" else "Select an image in edit mode") else (if (isJa) "編集モードで画像を選択できます" else "Select an image in edit mode"),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        )
                    }
                }
            }

            // 編集モード時に変更ボタンをオーバーレイ
            if (isEditMode && imageUri != null) {
                SmallFloatingActionButton(
                    onClick = { picker.launch("image/*") },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp),
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f),
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "画像変更",
                        modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
