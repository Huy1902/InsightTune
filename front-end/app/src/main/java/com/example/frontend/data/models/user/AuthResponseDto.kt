package com.example.frontend.data.models.user

data class AuthResponseDto(
    val token: String,
    val user: UserDto
)
