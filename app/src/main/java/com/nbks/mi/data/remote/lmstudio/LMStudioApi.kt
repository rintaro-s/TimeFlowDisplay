package com.nbks.mi.data.remote.lmstudio

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

// ─────────────────────────────────────────────
// LMStudio / OpenAI互換 API
// ─────────────────────────────────────────────
interface LMStudioApi {
    @POST
    suspend fun chatCompletion(
        @Url url: String,
        @Body request: ChatCompletionRequest,
    ): ChatCompletionResponse
}

data class ChatCompletionRequest(
    val model: String,
    val messages: List<ChatMessage>,
    @SerializedName("max_tokens") val maxTokens: Int = 2048,
    val temperature: Double = 0.7,
    val stream: Boolean = false,
)

data class ChatMessage(
    val role: String,
    val content: String,
)

data class ChatCompletionResponse(
    val id: String,
    val choices: List<Choice>,
    val usage: Usage?,
)

data class Choice(
    val index: Int,
    val message: ChatMessage,
    @SerializedName("finish_reason") val finishReason: String?,
)

data class Usage(
    @SerializedName("prompt_tokens") val promptTokens: Int,
    @SerializedName("completion_tokens") val completionTokens: Int,
    @SerializedName("total_tokens") val totalTokens: Int,
)
