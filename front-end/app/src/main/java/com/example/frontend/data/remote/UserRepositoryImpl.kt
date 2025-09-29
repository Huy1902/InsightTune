package com.example.frontend.data.remote

import android.util.Log
import com.example.frontend.core.AppPreferences
import com.example.frontend.data.models.user.AuthResponseDto
import com.example.frontend.data.models.user.LoginRequest
import com.example.frontend.data.models.user.RegisterRequest
import com.example.frontend.data.models.user.UserDto
import com.example.frontend.domain.repositories.UserRepository
import okhttp3.MultipartBody

class UserRepositoryImpl(
    private val api: UserApi,
    private val prefs: AppPreferences
) : UserRepository {

    override suspend fun login(email: String, password: String): AuthResponseDto {
        val response = api.login(LoginRequest(email, password))

        if (response.isSuccessful) {
            val body = response.body() ?: throw Exception("Empty body")
            prefs.saveToken(body.token)
            return body
        } else {
            val code = response.code()
            val friendly = when (code) {
                400, 401 -> "Wrong email or password, please try again."
                else -> "Login failed ($code). Please try again."
            }
            throw Exception(friendly)
        }
    }


    override suspend fun register(username: String, email: String, password: String): AuthResponseDto {
        val response = api.register(RegisterRequest(username, email, password))

        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                prefs.saveToken(body.token)
                return body
            } else {
                throw Exception("Empty body")
            }
        } else {
            throw Exception("Register failed: ${response.code()} ${response.errorBody()?.string()}")
        }
    }

    override suspend fun checkEmail(email: String): Boolean {
        return api.checkEmail(email).exists
    }

    override fun getToken(): String? {
        return prefs.getToken()
    }

    override suspend fun getUserInfo(): UserDto {
        val response = api.getUserInfo()
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Empty body")
        } else {
            throw Exception("Get user info failed: ${response.code()} ${response.errorBody()?.string()}")
        }
    }

    override suspend fun updateUserName(newName: String): UserDto {
        val response = api.updateUserName(mapOf("name" to newName))
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Empty body")
        } else {
            throw Exception("Update name failed: ${response.code()} ${response.errorBody()?.string()}")
        }
    }

    override suspend fun logout(): Boolean {
        val response = api.logout()
        return if (response.isSuccessful) {
            prefs.clearToken()
            true
        } else {
            false
        }
    }

    override suspend fun updateAvatar(avatar: MultipartBody.Part): UserDto {
        val response = api.updateAvatar(avatar)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Empty body")
        } else {
            throw Exception("Upload failed: ${response.code()}")
        }
    }


    override fun clearToken() {
        prefs.clearToken()
    }

}
