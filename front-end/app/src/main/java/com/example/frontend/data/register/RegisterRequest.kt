package com.example.frontend.data.register

data class RegisterRequest(
    val firstname: String,
    val lastname: String,
    val email: String,
    val password: String,
    val confirmPassword: String
)