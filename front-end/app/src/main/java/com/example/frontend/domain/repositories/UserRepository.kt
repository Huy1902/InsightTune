package com.example.frontend.domain.repositories

import com.example.frontend.data.models.user.AuthResponseDto
import com.example.frontend.data.models.user.UserDto
import okhttp3.MultipartBody

interface UserRepository {
    suspend fun login(email: String, password: String): AuthResponseDto
    suspend fun register(username: String, email: String, password: String): AuthResponseDto

    suspend fun checkEmail(email: String): Boolean

    fun getToken(): String?

    suspend fun getUserInfo(): UserDto
    suspend fun updateUserName(newName: String): UserDto
    suspend fun logout(): Boolean
    suspend fun updateAvatar(avatar: MultipartBody.Part): UserDto
    fun clearToken()
}
