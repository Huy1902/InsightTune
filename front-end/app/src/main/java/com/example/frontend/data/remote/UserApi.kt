package com.example.frontend.data.remote

import com.example.frontend.data.models.user.AuthResponseDto
import com.example.frontend.data.models.user.CheckEmailResponse
import com.example.frontend.data.models.user.LoginRequest
import com.example.frontend.data.models.user.RegisterRequest
import com.example.frontend.data.models.user.UserDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Query

interface UserApi {
    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponseDto>

    @POST("api/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponseDto>

    @GET("api/auth/check-email")
    suspend fun checkEmail(@Query("email") email: String): CheckEmailResponse

    @GET("api/user/me")
    suspend fun getUserInfo(): Response<UserDto>

    @POST("api/user/update-name")
    suspend fun updateUserFirstName(@Body body: Map<String, String>): Response<UserDto>

    @POST("api/user/update-name")
    suspend fun updateUserLastName(@Body body: Map<String, String>): Response<UserDto>

    @POST("api/logout")
    suspend fun logout(): Response<Unit>

    @Multipart
    @PUT("user/profile")
    suspend fun updateProfile(
        @Part("firstName") firstName: RequestBody? = null,
        @Part("lastName") lastName: RequestBody? = null,
        @Part("phone") phone: RequestBody? = null,
        @Part("address") address: RequestBody? = null,
        @Part("role") role: RequestBody? = null,
        @Part avatar: MultipartBody.Part? = null
    ): Response<UserDto>


    @Multipart
    @POST("api/user/avatar")
    suspend fun updateAvatar(
        @Part avatar: MultipartBody.Part
    ): Response<UserDto>

}
