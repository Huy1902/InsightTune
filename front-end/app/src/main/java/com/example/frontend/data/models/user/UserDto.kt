package com.example.frontend.data.models.user

data class UserDto(
    val email: String,
    val firstName: String,
    val lastName: String,
    val address: String? = null,
    val phone: String? = null,
    val avatarUrl: String? = null,
    val role: String = "user"
)
