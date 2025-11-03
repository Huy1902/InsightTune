package com.example.frontend.ui.chatbot

import android.app.Application
import android.media.MediaRecorder
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend.R
import com.example.frontend.data.models.Chatbot.ChatbotRequest
import com.example.frontend.data.models.Chatbot.ChatbotResponse
import com.example.frontend.data.models.song.GetTracksResponse
import com.example.frontend.data.remote.ChatbotApi
import com.example.frontend.domain.repositories.TrackRepository
import com.example.frontend.ui.playingsong.MusicPlayerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

class ChatbotViewModel(
    application: Application,
    val chatbotApi: ChatbotApi,
    val trackRepository: TrackRepository,
) : AndroidViewModel(application) {

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

    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var isRecording = false

    fun startVoiceInteraction() {
        if (isRecording) {
            stopRecordingAndSend()
        } else {
            startRecording()
        }
    }

    private fun startRecording() {
        try {
            outputFile = File.createTempFile("voice_", ".m4a")
            recorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(16000)
                setOutputFile(outputFile!!.absolutePath)
                prepare()
                start()
            }
            isRecording = true
            Log.d("Chatbot", "Bắt đầu ghi âm: ${outputFile!!.absolutePath}")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

//    private fun stopRecordingAndSend() {
//        try {
//            recorder?.apply {
//                stop()
//                release()
//            }
//            recorder = null
//            isRecording = false
//            Log.d("Chatbot", "Dừng ghi âm")
//            outputFile?.let {
//                file ->
//                viewModelScope.launch(Dispatchers.IO) {
//                    sendVoiceToServer(file)
//                }
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }
private fun stopRecordingAndSend() {
    // Tạm thời bỏ qua việc dừng ghi âm thật
    // recorder?.apply { ... }
    isRecording = false
    Log.d("Chatbot_Test", "Bỏ qua ghi âm, bắt đầu gửi file mẫu.")

    // Bắt đầu logic gửi file mẫu từ resource
    viewModelScope.launch(Dispatchers.IO) {
        try {
            val context = getApplication<Application>().applicationContext
            val inputStream = context.resources.openRawResource(R.raw.tungla) // Lưu ý: không có đuôi file ở đây

            val tempFile = File.createTempFile("test_audio", ".m4a", context.cacheDir)

            inputStream.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            Log.d("Chatbot_Test", "Đã tạo file tạm từ resource: ${tempFile.absolutePath}")

            // 4. GỌI HÀM GỐC với file tạm vừa tạo. Bây giờ nó sẽ hoạt động!
            sendVoiceToServer(tempFile)

        } catch (e: Exception) {
            Log.e("Chatbot_Test", "Lỗi khi xử lý và gửi file mẫu", e)
        }
    }
}

    private suspend fun sendVoiceToServer(audioFile: File) {
        try {

            val requestFile = audioFile.asRequestBody("audio/mp4".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", audioFile.name, requestFile)
            val response = chatbotApi.getTextFromVoice(body)

            Log.d("Chatbot", "Phản hồi thô từ API: $response")

            val text = response.text ?: ""

            if (text.isNotBlank()) {
                Log.d("Chatbot", "Văn bản nhận được: $text")

                viewModelScope.launch(Dispatchers.Main) {
                    sendMessage(text)
                }
            } else {
                Log.e("Chatbot", "API không trả về text hợp lệ")
            }
        } catch (e: Exception) {
            Log.e("Chatbot", "Lỗi khi gửi voice", e)
        }
    }


    fun sendMessage(userText: String) {
        if (userText.isBlank()) return

        // Thêm tin nhắn người dùng
        val userMessage = Message(text = userText, isFromUser = true)
        _messages.value = _messages.value + userMessage

        // Bắt đầu quá trình bot phản hồi (giả lập / gọi API thật)
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

