package com.example.frontend.data.remote

import com.example.frontend.core.AppPreferences
import com.example.frontend.data.models.chatbot.ChatbotVoiceResponse
import com.example.frontend.data.models.chatbot.ChatbotRequest
import com.example.frontend.data.models.chatbot.ChatbotResponse
import com.example.frontend.domain.repositories.ChatbotRepository
import okhttp3.MultipartBody

class ChatbotRepositoryImpl (
    private val chatbotApi: ChatbotApi,
    private val prefs: AppPreferences
) : ChatbotRepository {
    override suspend fun getResponseChatbot(message: String, thread_id: String?): ChatbotResponse {
        return try {
            val request = ChatbotRequest(message, thread_id)
            val response = chatbotApi.getResponseChatbot(request)
            response
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun getTextFromVoice(voice: MultipartBody.Part): ChatbotVoiceResponse {
        return try {
            val response = chatbotApi.getTextFromVoice(voice)
            response
        } catch (e: Exception) {
            throw e
        }
    }
}