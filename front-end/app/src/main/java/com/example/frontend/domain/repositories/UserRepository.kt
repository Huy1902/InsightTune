package com.example.frontend.domain.repositories

import android.content.Context
import android.net.Uri
import com.example.frontend.data.register.AuthResponseDto
import com.example.frontend.data.models.user.ChangeAvatarResponse
import com.example.frontend.data.models.user.ChangePasswordResponse
import com.example.frontend.data.register.ForgotPasswordResponse
import com.example.frontend.data.register.LogoutResponseDto
import com.example.frontend.data.register.RegisterResponseDto
import com.example.frontend.data.models.user.UserResult

interface UserRepository {
    suspend fun login(email: String, password: String): AuthResponseDto
    suspend fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        confirmPassword: String
    ): RegisterResponseDto

    suspend fun checkEmail(email: String): Boolean

    fun getToken(): String?

    suspend fun getUserInfo(): UserResult
    suspend fun logout(refreshToken: String): LogoutResponseDto

    suspend fun updateAvatar(context: Context, uri: Uri): ChangeAvatarResponse
    suspend fun updateProfile(
        firstname: String,
        lastname: String,
        address: String,
        phone: String,
        role: String
    ): UserResult

    suspend fun changePassword(oldPassword: String, newPassword: String): ChangePasswordResponse

    fun getRefreshToken(): String?
    fun clearToken()

    suspend fun requestOtp(email: String): ForgotPasswordResponse?

    suspend fun forgetPassword(otp: String, email: String, newPassword: String, confirmNewPassword: String): ForgotPasswordResponse?
}
