package com.example.frontend.data.models.user

data class AuthResponseDto(
    val code: Int,
    val message: String,
    val result: AuthResult
)

data class AuthResult(
    val token: String,
    val refreshToken: String,
    val email: String,
    val authenticated: Boolean
)

data class RegisterResponseDto(
    val code: Int,
    val message: String,
    val result: RegisterResult
)

data class RegisterResult(
    val id: Int,
    val email: String,
    val fullName: String,
    val role: String
)

data class LogoutResponseDto(
    val code: Int,
    val message: String,
    val result: LogoutResult
)

data class LogoutResult(
    val token: String,
    val refreshToken: String,
    val email: String,
    val authenticated: Boolean
)

data class RefreshResponseDto(
    val code: Int,
    val message: String,
    val result: RefreshResult
)

data class RefreshResult(
    val token: String,
    val refreshToken: String,
    val email: String,
    val authenticated: Boolean
)