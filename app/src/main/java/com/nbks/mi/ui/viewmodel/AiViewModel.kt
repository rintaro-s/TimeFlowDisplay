package com.nbks.mi.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nbks.mi.data.local.preferences.AppPreferences
import com.nbks.mi.data.repository.AiMessageRepository
import com.nbks.mi.data.repository.AiRepository
import com.nbks.mi.domain.model.AiMessage
import com.nbks.mi.domain.model.AppSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class AiViewModel @Inject constructor(
    application: Application,
    private val aiRepository: AiRepository,
    private val messageRepository: AiMessageRepository,
    private val appPreferences: AppPreferences,
) : AndroidViewModel(application) {

    val messages: StateFlow<List<AiMessage>> = messageRepository.getAllMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val settings: StateFlow<AppSettings> = appPreferences.appSettings.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings()
    )

    fun sendMessage(userText: String) {
        if (userText.isBlank()) return
        viewModelScope.launch {
            val userMsg = AiMessage(
                role = "user",
                content = userText.trim(),
                timestamp = LocalDateTime.now(),
            )
            messageRepository.saveMessage(userMsg)

            _isLoading.value = true
            _error.value = null

            val s = settings.value
            val history = messageRepository.getRecentMessages(20)

            val result = aiRepository.sendMessage(
                baseUrl = s.lmStudioBaseUrl,
                model = s.lmStudioModel,
                maxTokens = s.lmStudioMaxTokens,
                history = history.dropLast(1), // exclude just-added user msg
                userMessage = userText.trim(),
            )

            result.fold(
                onSuccess = { reply ->
                    messageRepository.saveMessage(
                        AiMessage(
                            role = "assistant",
                            content = reply,
                            timestamp = LocalDateTime.now(),
                        )
                    )
                    messageRepository.trimHistory(100)
                },
                onFailure = { e ->
                    _error.value = e.message ?: "送信に失敗しました"
                    messageRepository.saveMessage(
                        AiMessage(
                            role = "assistant",
                            content = "エラー: ${e.message ?: "不明なエラー"}",
                            timestamp = LocalDateTime.now(),
                            isError = true,
                        )
                    )
                }
            )
            _isLoading.value = false
        }
    }

    fun clearHistory() = viewModelScope.launch { messageRepository.clearHistory() }
    fun dismissError() { _error.value = null }
}
