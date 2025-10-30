package com.example.frontend.ui.chatbot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend.data.models.Chatbot.ChatbotRequest
import com.example.frontend.data.remote.ChatbotApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatbotViewModel(
    val chatbotApi: ChatbotApi
)
 : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private val _isBotTyping = MutableStateFlow(false)
    val isBotTyping: StateFlow<Boolean> = _isBotTyping

    init {
        _messages.value = listOf(
            Message(text = "Xin chào! Mình có thể giúp gì cho bạn?", isFromUser = false)
        )
    }

    fun sendMessage(userText: String) {
        if (userText.isBlank()) return

        // 1️⃣ Thêm tin nhắn người dùng
        val userMessage = Message(text = userText, isFromUser = true)
        _messages.value = _messages.value + userMessage

        // 2️⃣ Bắt đầu quá trình bot phản hồi (giả lập / gọi API thật)
        simulateBotResponse(userText)
    }

    private fun simulateBotResponse(userText: String) {
        viewModelScope.launch {
            _isBotTyping.value = true

            delay(1000) // mô phỏng thời gian bot xử lý


            val replyText = chatbotApi.getResponseChatbot(ChatbotRequest(userText, "default"))

            val botMessage = Message(text = replyText.response, isFromUser = false)
            _messages.value = _messages.value + botMessage

            _isBotTyping.value = false
        }
    }
}