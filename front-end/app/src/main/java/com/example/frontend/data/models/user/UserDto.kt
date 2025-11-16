package com.example.frontend.data.models.user

data class UserDto(
    val code: Int,
    val message: String,
    val result: UserResult
)

data class UserResult(
    val id: Int,
    val email: String,
    val firstName: String,
    val lastName: String,
    val address: String,
    val phone: String,
    val role: String,
    val avatar: String
)

data class ChangeAvatarResponse(
    val code: Int,
    val message: String,
    val result: String
)

data class ChangePasswordResponse(
    val code: Int,
    val message: String,
    val result: String
)