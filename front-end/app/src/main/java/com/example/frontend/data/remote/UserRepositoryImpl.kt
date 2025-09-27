package com.example.frontend.data.remote

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
        val res = api.login(LoginRequest(email, password))
        prefs.saveToken(res.token)
        return res
    }

    override suspend fun register(username: String, email: String, password: String): AuthResponseDto {
        val res = api.register(RegisterRequest(username, email, password))
        prefs.saveToken(res.token)
        return res
    }

    override suspend fun checkEmail(email: String): Boolean {
        return api.checkEmail(email).exists
    }

}
