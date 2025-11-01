package com.example.frontend.domain.repositories

import com.example.frontend.data.models.chatbot.ChatbotResponse

interface ChatbotRepository {
    suspend fun getResponseChatbot(message: String, thread_id: String?): ChatbotResponse
}