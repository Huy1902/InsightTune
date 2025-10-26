package com.example.frontend.data.remote

import com.example.frontend.data.models.Chatbot.ChatbotRequest
import com.example.frontend.data.models.Chatbot.ChatbotResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ChatbotApi {
    @POST("chat")
    suspend fun getResponseChatbot(@Body chatbotRequest: ChatbotRequest) : ChatbotResponse
}