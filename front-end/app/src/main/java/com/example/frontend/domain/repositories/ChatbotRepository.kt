package com.example.frontend.domain.repositories

import com.example.frontend.data.models.chatbot.ChatbotVoiceResponse
import okhttp3.MultipartBody
import com.example.frontend.data.models.chatbot.ChatbotResponse

interface ChatbotRepository {
    suspend fun getResponseChatbot(message: String, thread_id: String?): ChatbotResponse

    suspend fun getTextFromVoice(voice: MultipartBody.Part) : ChatbotVoiceResponse
}