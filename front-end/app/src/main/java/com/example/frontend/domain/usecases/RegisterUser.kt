package com.example.frontend.domain.usecases

import com.example.frontend.core.Resource
import com.example.frontend.data.models.user.AuthResponseDto
import com.example.frontend.domain.repositories.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class RegisterUser(private val repo: UserRepository) {
    operator fun invoke(firstName: String, lastName: String, email: String, password: String): Flow<Resource<AuthResponseDto>> = flow {
        emit(Resource.Loading)
        try {
            val res = repo.register(firstName, lastName, email, password)
            emit(Resource.Success(res))
        } catch (e: Exception) {
            emit(Resource.Error(e.message, e))
        }
    }
}
