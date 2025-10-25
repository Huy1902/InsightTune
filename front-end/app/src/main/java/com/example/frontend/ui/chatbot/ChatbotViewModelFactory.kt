package com.example.frontend.ui.chatbot

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.frontend.ui.home.PlayingViewModel

class ChatbotViewModelFactory (private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>) : T {
        if (modelClass.isAssignableFrom(ChatbotViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatbotViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}