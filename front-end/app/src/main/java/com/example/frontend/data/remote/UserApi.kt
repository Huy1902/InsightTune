package com.example.frontend.data.remote

import com.example.frontend.data.models.user.AuthResponseDto
import com.example.frontend.data.models.user.CheckEmailResponse
import com.example.frontend.data.models.user.LogOutRequest
import com.example.frontend.data.models.user.LoginRequest
import com.example.frontend.data.models.user.LogoutResponseDto
import com.example.frontend.data.models.user.RefreshRequest
import com.example.frontend.data.models.user.RefreshResponseDto
import com.example.frontend.data.models.user.RegisterRequest
import com.example.frontend.data.models.user.RegisterResponseDto
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
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponseDto>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponseDto>
    @GET("api/auth/check-email")
    suspend fun checkEmail(@Query("email") email: String): CheckEmailResponse

    @GET("api/user/me")
    suspend fun getUserInfo(): Response<UserDto>

    @POST("api/user/update-name")
    suspend fun updateUserFirstName(@Body body: Map<String, String>): Response<UserDto>

    @POST("api/user/update-name")
    suspend fun updateUserLastName(@Body body: Map<String, String>): Response<UserDto>

    @POST("auth/logout")
    suspend fun logout(@Body request: LogOutRequest): Response<LogoutResponseDto>

    @POST("auth/refresh")
    suspend fun refresh(@Body request: RefreshRequest): Response<RefreshResponseDto>

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
