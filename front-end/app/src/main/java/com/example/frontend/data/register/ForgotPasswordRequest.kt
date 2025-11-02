package com.example.frontend.data.register

data class ForgotPasswordRequest (
    val otp: String,
    val email: String,
    val newPassword: String,
    val confirmNewPassword: String
)