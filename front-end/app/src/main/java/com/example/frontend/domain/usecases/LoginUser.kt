package com.example.frontend.domain.usecases

import com.example.frontend.core.Resource
import com.example.frontend.data.models.user.AuthResponseDto
import com.example.frontend.domain.repositories.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class LoginUser(private val repo: UserRepository) {
    operator fun invoke(email: String, password: String): Flow<Resource<AuthResponseDto>> = flow {
        emit(Resource.Loading)
        try {
            val res = repo.login(email, password)
            emit(Resource.Success(res))
        } catch (e: Exception) {
            val msg = if (e.message?.contains("401") == true) {
                "Wrong email or password, please try again."
            } else {
                e.message ?: "Unexpected error"
            }
            emit(Resource.Error(msg, e))
        }
    }
}
