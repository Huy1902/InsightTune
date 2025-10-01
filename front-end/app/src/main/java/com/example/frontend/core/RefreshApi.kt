package com.example.frontend.core

import com.example.frontend.data.models.user.RefreshRequest
import com.example.frontend.data.models.user.RefreshResponseDto
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface RefreshApi {
    @POST("auth/refresh")
    fun refresh(@Body request: RefreshRequest): Call<RefreshResponseDto>
}
