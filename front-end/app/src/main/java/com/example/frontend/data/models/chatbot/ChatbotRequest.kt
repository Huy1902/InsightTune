package com.example.frontend.data.models.chatbot

data class ChatbotRequest (
    val message: String,
    val thread_id: String?
)