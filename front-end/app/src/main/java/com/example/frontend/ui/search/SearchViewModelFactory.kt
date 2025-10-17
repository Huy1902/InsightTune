package com.example.frontend.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.frontend.core.AppPreferences
import com.example.frontend.domain.repositories.TrackRepository


class SearchViewModelFactory(
    private val trackRepository: TrackRepository,
    private val prefs: AppPreferences
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SearchViewModel(trackRepository, prefs) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class for SearchViewModelFactory")
    }
}