package com.example.frontend.data.models.user

data class ForgotPasswordRequest (
    val otp: String,
    val email: String,
    val newPassword: String,
    val confirmNewPassword: String
)
