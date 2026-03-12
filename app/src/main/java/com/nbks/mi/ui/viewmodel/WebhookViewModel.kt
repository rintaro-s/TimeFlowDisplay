package com.nbks.mi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nbks.mi.data.repository.WebhookButtonRepository
import com.nbks.mi.domain.model.WebhookButton
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class WebhookViewModel @Inject constructor(
    private val repo: WebhookButtonRepository,
    private val okHttpClient: OkHttpClient,
) : ViewModel() {

    val buttons: StateFlow<List<WebhookButton>> = repo.getAllButtons()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // buttonId → null(送信中) / true(成功) / false(失敗)
    private val _sendStatus = MutableStateFlow<Map<Long, Boolean?>>(emptyMap())
    val sendStatus: StateFlow<Map<Long, Boolean?>> = _sendStatus.asStateFlow()

    fun sendWebhook(button: WebhookButton) {
        viewModelScope.launch {
            _sendStatus.update { it + (button.id to null) }
            try {
                val body = JSONObject().apply { put("content", button.message) }
                    .toString()
                    .toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url(button.webhookUrl)
                    .post(body)
                    .build()
                val response = withContext(Dispatchers.IO) {
                    okHttpClient.newCall(request).execute()
                }
                val ok = response.isSuccessful
                response.close()
                _sendStatus.update { it + (button.id to ok) }
            } catch (e: Exception) {
                _sendStatus.update { it + (button.id to false) }
            }
        }
    }

    fun addButton(button: WebhookButton) {
        viewModelScope.launch { repo.saveButton(button) }
    }

    fun updateButton(button: WebhookButton) {
        viewModelScope.launch { repo.updateButton(button) }
    }

    fun deleteButton(id: Long) {
        viewModelScope.launch { repo.deleteButton(id) }
    }

    fun clearStatus(id: Long) {
        _sendStatus.update { it - id }
    }
}
