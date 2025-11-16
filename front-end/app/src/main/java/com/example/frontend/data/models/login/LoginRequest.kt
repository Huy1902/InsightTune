package com.example.frontend.data.models.login

data class LoginRequest(
    val email: String, val password: String
)

data class GoogleRequest(
    val idToken: String
)