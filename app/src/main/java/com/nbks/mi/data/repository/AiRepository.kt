package com.nbks.mi.data.repository

import com.nbks.mi.data.remote.lmstudio.ChatCompletionRequest
import com.nbks.mi.data.remote.lmstudio.ChatMessage
import com.nbks.mi.data.remote.lmstudio.LMStudioApi
import com.nbks.mi.domain.model.AiMessage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiRepository @Inject constructor(
    private val api: LMStudioApi,
) {
    suspend fun sendMessage(
        baseUrl: String,
        model: String,
        maxTokens: Int,
        history: List<AiMessage>,
        userMessage: String,
    ): Result<String> = runCatching {
        val messages = buildList {
            add(ChatMessage(role = "system", content = "あなたは親切なAIアシスタントです。"))
            history.takeLast(20).forEach { msg ->
                add(ChatMessage(role = msg.role, content = msg.content))
            }
            add(ChatMessage(role = "user", content = userMessage))
        }

        val url = baseUrl.trimEnd('/') + "/v1/chat/completions"
        val response = api.chatCompletion(
            url = url,
            request = ChatCompletionRequest(
                model = model,
                messages = messages,
                maxTokens = maxTokens,
            ),
        )
        response.choices.firstOrNull()?.message?.content
            ?: throw IllegalStateException("AIからの応答が空です")
    }
}
