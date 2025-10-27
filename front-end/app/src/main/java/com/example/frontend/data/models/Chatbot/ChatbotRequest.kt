package com.example.frontend.data.models.Chatbot

data class ChatbotRequest (
    val message: String,
    val thread_id: String?
)