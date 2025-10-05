package com.example.frontend.data.remote

import com.example.frontend.data.models.user.ChangeAvatarResponse
import com.example.frontend.data.models.user.UpdateAvatarRequest
import com.example.frontend.data.models.user.UpdateUserRequest
import com.example.frontend.data.models.user.UserDto
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part

interface UserApi {

    @PUT("users")
    suspend fun updateUser(
        @Body request: UpdateUserRequest
    ): Response<UserDto>

    @GET("users")
    suspend fun getUserInfo(): Response<UserDto>

    @Multipart
    @PUT("users/avatar")
    suspend fun changeAvatar(
        @Part file: MultipartBody.Part
    ): Response<ChangeAvatarResponse>

}