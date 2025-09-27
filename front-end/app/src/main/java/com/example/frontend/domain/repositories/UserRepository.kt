package com.example.frontend.domain.repositories

import com.example.frontend.data.models.user.AuthResponseDto

interface UserRepository {
    suspend fun login(email: String, password: String): AuthResponseDto
    suspend fun register(username: String, email: String, password: String): AuthResponseDto

    suspend fun checkEmail(email: String): Boolean

    fun getToken(): String?
}
