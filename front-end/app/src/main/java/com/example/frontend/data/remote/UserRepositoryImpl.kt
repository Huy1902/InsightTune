package com.example.frontend.data.remote

import android.util.Log
import com.example.frontend.core.AppPreferences
import com.example.frontend.data.models.user.AuthResponseDto
import com.example.frontend.data.models.user.LogOutRequest
import com.example.frontend.data.models.user.LoginRequest
import com.example.frontend.data.models.user.LogoutResponseDto
import com.example.frontend.data.models.user.LogoutResult
import com.example.frontend.data.models.user.RefreshRequest
import com.example.frontend.data.models.user.RefreshResponseDto
import com.example.frontend.data.models.user.RegisterRequest
import com.example.frontend.data.models.user.RegisterResponseDto
import com.example.frontend.data.models.user.UserDto
import com.example.frontend.domain.repositories.UserRepository
import okhttp3.MultipartBody
import okhttp3.RequestBody

class UserRepositoryImpl(
    private val api: UserApi,
    private val prefs: AppPreferences
) : UserRepository {

    override suspend fun login(email: String, password: String): AuthResponseDto {
        val response = api.login(LoginRequest(email, password))
        Log.d("LOGIN", "response=${response.code()} body=${response.body()} error=${response.errorBody()?.string()}")
        if (response.isSuccessful) {
            val body = response.body() ?: throw Exception("Empty body")
            prefs.saveToken(body.result.token)
            prefs.saveRefreshToken(body.result.refreshToken)
            return body
        } else {
            val code = response.code()
            val friendly = when (code) {
                0 -> "No internet connection"
                else -> "Login failed ($code). Please try again."
            }
            throw Exception(friendly)
        }
    }


    override suspend fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        confirmPassword: String
    ): RegisterResponseDto {
        Log.d("REGISTER", "Sending register request: firstName=$firstName, lastName=$lastName, email=$email")

        val response = api.register(
            RegisterRequest(firstName, lastName, email, password, confirmPassword)
        )

        Log.d("REGISTER", "Response code=${response.code()}")

        if (response.isSuccessful) {
            val body = response.body()
            Log.d("REGISTER", "Response body=$body")
            return body ?: throw Exception("Empty body")
        } else {
            val errorBody = response.errorBody()?.string()
            Log.e("REGISTER", "Register failed: code=${response.code()} error=$errorBody")
            throw Exception("Register failed: ${response.code()} $errorBody")
        }
    }

    override suspend fun refreshToken(refreshToken: String): RefreshResponseDto {
        val response = api.refresh(RefreshRequest(refreshToken))
        if (response.isSuccessful) {
            val body = response.body() ?: throw Exception("Empty body")
            // Lưu token mới
            prefs.saveToken(body.result.token)
            prefs.saveRefreshToken(body.result.refreshToken)
            return body
        } else {
            throw Exception("Refresh failed: ${response.code()} ${response.errorBody()?.string()}")
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

    override suspend fun updateUserFirstName(newName: String): UserDto {
        val response = api.updateUserFirstName(mapOf("name" to newName))
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Empty body")
        } else {
            throw Exception("Update name failed: ${response.code()} ${response.errorBody()?.string()}")
        }
    }


    override suspend fun updateUserLastName(newName: String): UserDto {
        val response = api.updateUserLastName(mapOf("name" to newName))
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Empty body")
        } else {
            throw Exception("Update name failed: ${response.code()} ${response.errorBody()?.string()}")
        }
    }

    override suspend fun logout(refreshToken: String): LogoutResponseDto {
        val response = api.logout(LogOutRequest(refreshToken))
        if (response.isSuccessful) {
            val body = response.body()
            prefs.clearToken()
            prefs.clearRefreshToken()
            Log.d("LOGOUT", "Tokens cleared on logout")
            return body ?: LogoutResponseDto(200, "No body", LogoutResult("", "", "", false))
        } else {
            throw Exception("Logout failed: ${response.code()} ${response.errorBody()?.string()}")
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

    override suspend fun updateProfile(
        firstName: RequestBody?,
        lastName: RequestBody?,
        phone: RequestBody?,
        address: RequestBody?,
        role: RequestBody?,
        avatar: MultipartBody.Part?
    ): UserDto {
        val response = api.updateProfile(firstName, lastName, phone, address, role, avatar)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Empty response body")
        } else {
            throw Exception("Update profile failed: ${response.code()} ${response.message()}")
        }
    }

    override fun getRefreshToken(): String? {
        return prefs.getRefreshToken()
    }

    override fun clearToken() {
        prefs.clearToken()
    }

}
