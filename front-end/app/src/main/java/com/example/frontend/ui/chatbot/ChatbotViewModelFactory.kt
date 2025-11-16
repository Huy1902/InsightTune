package com.example.frontend.ui.chatbot

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.frontend.data.remote.ChatbotApi
import com.example.frontend.domain.repositories.TrackRepository
import com.example.frontend.ui.home.PlayingViewModel

class ChatbotViewModelFactory (
    val chatbotApi: ChatbotApi,
    val trackRepository: TrackRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>) : T {
        if (modelClass.isAssignableFrom(ChatbotViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatbotViewModel(
                chatbotApi,
                trackRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}