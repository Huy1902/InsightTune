package com.example.frontend.data.models.user

data class UpdateUserRequest(
    val firstname: String,
    val lastname: String,
    val address: String,
    val phone: String,
    val role: String
)

data class UpdateAvatarRequest(
    val avatar: String
)

data class ChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String
)