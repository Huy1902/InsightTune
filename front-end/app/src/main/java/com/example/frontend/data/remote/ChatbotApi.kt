package com.example.frontend.data.remote

import com.example.frontend.data.models.Chatbot.ChatbotRequest
import com.example.frontend.data.models.Chatbot.ChatbotResponse
import com.example.frontend.data.models.Chatbot.ChatbotVoiceResponse
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ChatbotApi {
    @POST("/chat")
    suspend fun getResponseChatbot(@Body chatbotRequest: ChatbotRequest) : ChatbotResponse

    @Multipart
    @POST("/voice-to-text")
    suspend fun getTextFromVoice(
        @Part file: MultipartBody.Part
    ) : ChatbotVoiceResponse
}