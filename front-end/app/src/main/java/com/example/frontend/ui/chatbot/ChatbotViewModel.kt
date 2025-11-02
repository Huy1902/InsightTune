package com.example.frontend.ui.chatbot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend.data.models.chatbot.ChatbotRequest
import com.example.frontend.data.remote.ChatbotApi
import com.example.frontend.domain.repositories.TrackRepository
import com.example.frontend.ui.playingsong.MusicPlayerViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.logging.Logger
import kotlin.math.log

class ChatbotViewModel(
    val chatbotApi: ChatbotApi,
    val trackRepository: TrackRepository,
)
 : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private val _isBotTyping = MutableStateFlow(false)
    val isBotTyping: StateFlow<Boolean> = _isBotTyping

    private val _trackToPlay = MutableStateFlow<GetTracksResponse?> (null)
    val trackToPlay: StateFlow<GetTracksResponse?> = _trackToPlay

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
//            val replyText = when {
//                userText.contains("hello", true) -> "Chào bạn 👋"
//                userText.contains("music", true) -> "Mình có thể gợi ý nhạc cho bạn!"
//                else -> "Mình chưa hiểu lắm 😅, bạn có thể nói rõ hơn không?"
//            }
            if (replyText.type == "reply" && replyText.response != null) {

                val botMessage = Message(text = replyText.response, isFromUser = false)
                _messages.value = _messages.value + botMessage
            }
            else if (replyText.type == "playMusic" && replyText.song_name != null)
            {
                val searchResult = trackRepository.searchTracks(replyText.song_name)
                val track = searchResult.firstOrNull()
                if (track != null) {
                    val foundMessage = Message(text = "Đã tìm thấy chuẩn bị phát nhạc", isFromUser = false)
                    _messages.value = _messages.value + foundMessage
                    _trackToPlay.value = track
                } else {
                    val notFoundMessage = Message(text = "Rất tiếc, mình không tìm thấy bài hát nào có tên '${replyText.song_name}'.", isFromUser = false)
                    _messages.value = _messages.value + notFoundMessage
                }
            }


            _isBotTyping.value = false
        }

    }

    fun onTrackPlayed() {
        _trackToPlay.value = null
    }
}

