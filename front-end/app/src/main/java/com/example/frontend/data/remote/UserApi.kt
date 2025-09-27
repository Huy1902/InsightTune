package com.example.frontend.data.remote

import com.example.frontend.data.models.user.AuthResponseDto
import com.example.frontend.data.models.user.CheckEmailResponse
import com.example.frontend.data.models.user.LoginRequest
import com.example.frontend.data.models.user.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface UserApi {
    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponseDto>

    @POST("api/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponseDto>

    @GET("api/auth/check-email")
    suspend fun checkEmail(@Query("email") email: String): CheckEmailResponse

}
