package com.example.frontend.domain.repositories

import com.example.frontend.data.models.Chatbot.ChatbotResponse

interface ChatbotRepository {
    suspend fun getResponseChatbot(message: String, thread_id: String?): ChatbotResponse
}