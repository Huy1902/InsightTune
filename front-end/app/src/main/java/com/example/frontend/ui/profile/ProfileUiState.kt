package com.example.frontend.ui.profile

data class ProfileUiState(
    val name: String = "",
    val email: String = "",
    val avatarUrl: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
