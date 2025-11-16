package com.example.frontend.domain.usecases

import com.example.frontend.core.Resource
import com.example.frontend.data.register.RegisterResponseDto
import com.example.frontend.domain.repositories.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class RegisterUser(
    private val repo: UserRepository
) {
    operator fun invoke(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Flow<Resource<RegisterResponseDto>> = flow {
        emit(Resource.Loading)
        try {
            val response = repo.register(firstName, lastName, email, password, confirmPassword)
            emit(Resource.Success(response))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Unknown error"))
        }
    }
}
