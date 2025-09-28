package com.example.frontend.data.remote

import android.util.Log
import com.example.frontend.core.AppPreferences
import com.example.frontend.data.models.user.AuthResponseDto
import com.example.frontend.data.models.user.LoginRequest
import com.example.frontend.data.models.user.RegisterRequest
import com.example.frontend.domain.repositories.UserRepository

class UserRepositoryImpl(
    private val api: UserApi,
    private val prefs: AppPreferences
) : UserRepository {

    override suspend fun login(email: String, password: String): AuthResponseDto {
        val response = api.login(LoginRequest(email, password))

        Log.d("API_DEBUG", "code=${response.code()}, url=${response.raw().request.url}, error=${response.errorBody()?.string()}")

        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                prefs.saveToken(body.token)
                return body
            } else {
                throw Exception("Empty body")
            }
        } else {
            throw Exception("Login failed: ${response.code()} ${response.errorBody()?.string()}")
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

}
