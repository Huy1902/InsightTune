package com.example.frontend.data.remote

import android.util.Log
import com.example.frontend.core.AppPreferences
import com.example.frontend.data.models.user.AuthResponseDto
import com.example.frontend.data.models.user.ChangeAvatarResponse
import com.example.frontend.data.models.user.ChangePasswordRequest
import com.example.frontend.data.models.user.ChangePasswordResponse
import com.example.frontend.data.models.user.LogOutRequest
import com.example.frontend.data.models.user.LoginRequest
import com.example.frontend.data.models.user.LogoutResponseDto
import com.example.frontend.data.models.user.LogoutResult
import com.example.frontend.data.models.user.RefreshRequest
import com.example.frontend.data.models.user.RefreshResponseDto
import com.example.frontend.data.models.user.RegisterRequest
import com.example.frontend.data.models.user.RegisterResponseDto
import com.example.frontend.data.models.user.UpdateAvatarRequest
import com.example.frontend.data.models.user.UpdateUserRequest
import com.example.frontend.data.models.user.UserDto
import com.example.frontend.data.models.user.UserResult
import com.example.frontend.domain.repositories.UserRepository
import okhttp3.MultipartBody
import org.json.JSONObject

class UserRepositoryImpl(
    private val authApi: AuthApi,
    private val userAPI: UserApi,
    private val prefs: AppPreferences
) : UserRepository {

    override suspend fun login(email: String, password: String): AuthResponseDto {
        val response = authApi.login(LoginRequest(email, password))
        Log.d(
            "LOGIN",
            "response=${response.code()} body=${response.body()} error=${
                response.errorBody()?.string()
            }"
        )
        if (response.isSuccessful) {
            val body = response.body() ?: throw Exception("Empty body")
            // *** THÊM LOG KIỂM TRA Ở ĐÂY ***
            Log.d("LOGIN_DEBUG", "Access Token nhận được: ${body.result.token}")
            Log.d("LOGIN_DEBUG", "Refresh Token nhận được: ${body.result.refreshToken}")
            prefs.saveToken(body.result.token)
            prefs.saveRefreshToken(body.result.refreshToken)
            Log.d("TOKEN_SAVE", "Token saved: ${prefs.getToken()}")
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
        Log.d(
            "REGISTER",
            "Sending register request: firstName=$firstName, lastName=$lastName, email=$email"
        )

        val response = authApi.register(
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



    override suspend fun checkEmail(email: String): Boolean {
        return authApi.checkEmail(email).exists
    }

    override fun getToken(): String? {
        return prefs.getToken()
    }

    override suspend fun getUserInfo(): UserResult {
        val response = userAPI.getUserInfo()
        if (response.isSuccessful) {
            val body = response.body() ?: throw Exception("Empty body")
            if (body.code == 200) {
                return body.result
            } else {
                throw Exception("API error: ${body.message}")
            }
        } else {
            throw Exception("HTTP error: ${response.code()}")
        }
    }

    override suspend fun logout(refreshToken: String): LogoutResponseDto {
        val response = authApi.logout(LogOutRequest(refreshToken))
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


    override suspend fun updateAvatar(avatar: String): ChangeAvatarResponse {
        val response = userAPI.changeAvatar(UpdateAvatarRequest(avatar))
        if (response.isSuccessful) {
            val body = response.body() ?: throw Exception("Empty response")
            if (body.code != 200) {
                throw Exception("API error: ${body.message}")
            }
            return body ?: ChangeAvatarResponse(200, "No body", "No result")

        } else {
            throw Exception("HTTP ${response.code()}")
        }
    }

    override suspend fun changePassword(
        oldPassword: String,
        newPassword: String
    ): ChangePasswordResponse {
        val response = authApi.changePassword(ChangePasswordRequest(oldPassword, newPassword))
        if (response.isSuccessful) {
            val body = response.body() ?: throw Exception("Empty response")
            if (body.code != 200) {
                throw Exception("API error: ${body.message}")
            }
            return body ?: ChangePasswordResponse(200, "No body", "No result")

        } else {
            val errorBody = response.errorBody()?.string()
            throw Exception("Changed password failed: ${response.code()} $errorBody")}
    }

    override suspend fun updateProfile(
        firstname: String,
        lastname: String,
        address: String,
        phone: String,
        role: String
    ): UserResult {
        val request = UpdateUserRequest(firstname, lastname, address, phone, role)
        Log.d("PROFILE", "Sending update profile request: $request")

        val response = userAPI.updateUser(request)

        Log.d("PROFILE", "HTTP status: ${response.code()} - ${response.message()}")

        if (response.isSuccessful) {
            val body = response.body() ?: throw Exception("Empty response")
            Log.d("PROFILE", "Response body: $body")

            if (body.code == 200 || body.code == 0) { // tùy backend
                Log.d("PROFILE", "Update profile success: ${body.result}")
                return body.result
            } else {
                Log.e("PROFILE", "API error: ${body.message}")
                throw Exception(body.message)
            }
        } else {
            val errorBody = response.errorBody()?.string()
            val errorMessage = try {
                val json = JSONObject(errorBody ?: "{}")
                json.optString("message", "Unknown error")
            } catch (e: Exception) {
                "Unknown error"
            }
            throw Exception(errorMessage)
        }
    }


    override fun getRefreshToken(): String? {
        return prefs.getRefreshToken()
    }

    override fun clearToken() {
        prefs.clearToken()
    }

}
