package com.example.frontend.ui.profile

data class ProfileUiState(
    val fullName: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val address: String = "",
    val phone: String = "",
    val avatarUrl: String? = null,
    val role: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)
