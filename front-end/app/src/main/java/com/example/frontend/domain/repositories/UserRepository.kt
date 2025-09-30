package com.example.frontend.domain.repositories

import com.example.frontend.data.models.user.AuthResponseDto
import com.example.frontend.data.models.user.UserDto
import okhttp3.MultipartBody
import okhttp3.RequestBody

interface UserRepository {
    suspend fun login(email: String, password: String): AuthResponseDto
    suspend fun register(firstName: String, lastName: String, email: String, password: String): AuthResponseDto

    suspend fun checkEmail(email: String): Boolean

    fun getToken(): String?

    suspend fun getUserInfo(): UserDto
    suspend fun updateUserFirstName(newName: String): UserDto
    suspend fun updateUserLastName(newName: String): UserDto
    suspend fun logout(): Boolean
    suspend fun updateAvatar(avatar: MultipartBody.Part): UserDto
    suspend fun updateProfile(
        firstName: RequestBody?,
        lastName: RequestBody?,
        phone: RequestBody?,
        address: RequestBody?,
        role: RequestBody?,
        avatar: MultipartBody.Part?
    ): UserDto

    fun clearToken()
}
