package com.nbks.mi.ui.components.widgets

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import com.nbks.mi.domain.model.AiMessage
import com.nbks.mi.ui.components.WidgetHeader
import com.nbks.mi.ui.theme.LocalIsJa

@Composable
fun AiWidget(
    messages: List<AiMessage>,
    isLoading: Boolean,
    isDarkTheme: Boolean,
    isConfigured: Boolean,
    onSend: (String) -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val isJa = LocalIsJa.current

    var isListening by remember { mutableStateOf(false) }
    val speechRecognizer = remember {
        if (SpeechRecognizer.isRecognitionAvailable(context))
            SpeechRecognizer.createSpeechRecognizer(context)
        else null
    }

    var showMicPermissionDialog by remember { mutableStateOf(false) }
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startSpeechRecognition(context, speechRecognizer, if (isJa) "ja-JP" else "en-US") { result ->
                inputText = result
                isListening = false
            }
        } else {
            isListening = false
            showMicPermissionDialog = true
        }
    }

    DisposableEffect(Unit) {
        onDispose { speechRecognizer?.destroy() }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    Column(modifier = modifier.fillMaxSize()) {
        WidgetHeader(
            title = if (isJa) "AI チャット" else "AI Chat",
            icon = Icons.Default.SmartToy,
            isDarkTheme = isDarkTheme,
            actions = {
                if (messages.isNotEmpty()) {
                    IconButton(onClick = onClearHistory, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = if (isJa) "履歴消去" else "Clear history", modifier = Modifier.size(14.dp))
                    }
                }
            }
        )

        if (!isConfigured) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(Icons.Default.SmartToy, contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                    Spacer(Modifier.height(8.dp))
                    Text(if (isJa) "LMStudio URLを\n設定してください" else "Configure\nLMStudio URL",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center)
                }
            }
            return@Column
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(vertical = 6.dp),
        ) {
            if (messages.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Mic, contentDescription = null,
                                modifier = Modifier.size(28.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                            Spacer(Modifier.height(4.dp))
                            Text(if (isJa) "マイクボタンで音声入力，\nまたはテキストを入力" else "Tap mic to speak\nor type a message",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                textAlign = TextAlign.Center)
                        }
                    }
                }
            }
            items(messages) { msg -> AiChatBubble(message = msg) }
            if (isLoading) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text(if (isJa) "考え中..." else "Thinking...", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                    }
                }
            }
        }

        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                IconButton(
                    onClick = {
                        if (isListening) {
                            speechRecognizer?.stopListening()
                            isListening = false
                        } else {
                            isListening = true
                            permLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    modifier = Modifier.size(40.dp).background(
                        color = if (isListening) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape),
                ) {
                    Icon(
                        if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = if (isListening) (if (isJa) "停止" else "Stop") else (if (isJa) "音声入力" else "Voice input"),
                        modifier = Modifier.size(18.dp),
                        tint = if (isListening) MaterialTheme.colorScheme.onError
                        else MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.width(6.dp))
            }

            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = {
                    Text(if (isListening) (if (isJa) "耸いています..." else "Listening...") else (if (isJa) "メッセージを入力..." else "Type a message..."),
                        style = MaterialTheme.typography.labelSmall)
                },
                modifier = Modifier.weight(1f),
                maxLines = 3,
                textStyle = MaterialTheme.typography.bodySmall,
                shape = RoundedCornerShape(20.dp),
                enabled = !isListening,
            )
            Spacer(Modifier.width(6.dp))
            IconButton(
                onClick = {
                    if (inputText.isNotBlank() && !isLoading) {
                        onSend(inputText.trim())
                        inputText = ""
                    }
                },
                enabled = inputText.isNotBlank() && !isLoading,
                modifier = Modifier.size(40.dp).background(
                    color = if (inputText.isNotBlank() && !isLoading)
                        MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    shape = CircleShape),
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = if (isJa) "送信" else "Send",
                    modifier = Modifier.size(18.dp),
                    tint = if (inputText.isNotBlank() && !isLoading)
                        MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }

    if (showMicPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showMicPermissionDialog = false },
            title = { Text(if (isJa) "マイク権限が必要" else "Microphone Permission Required") },
            text = { Text(if (isJa) "音声入力を使うにはマイクのアクセス権限が必要です。設定から許可してください。" else "Microphone permission is needed for voice input. Please allow it in Settings.") },
            confirmButton = { TextButton(onClick = { showMicPermissionDialog = false }) { Text("OK") } },
        )
    }
}

private fun startSpeechRecognition(
    context: android.content.Context,
    recognizer: SpeechRecognizer?,
    language: String,
    onResult: (String) -> Unit,
) {
    recognizer ?: return
    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
    }
    recognizer.setRecognitionListener(object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}
        override fun onError(error: Int) { onResult("") }
        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            onResult(matches?.firstOrNull() ?: "")
        }
        override fun onPartialResults(partial: Bundle?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    })
    recognizer.startListening(intent)
}

@Composable
private fun AiChatBubble(message: AiMessage) {
    val isUser = message.role == "user"
    val bubbleColor = when {
        message.isError -> MaterialTheme.colorScheme.errorContainer
        isUser -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.secondaryContainer
    }
    val textColor = when {
        message.isError -> MaterialTheme.colorScheme.onErrorContainer
        isUser -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSecondaryContainer
    }
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val shape = if (isUser)
        RoundedCornerShape(16.dp, 4.dp, 16.dp, 16.dp)
    else
        RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp)

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = alignment) {
        Surface(shape = shape, color = bubbleColor, modifier = Modifier.widthIn(max = 240.dp)) {
            Text(text = message.content,
                style = MaterialTheme.typography.bodySmall, color = textColor,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
        }
    }
}
