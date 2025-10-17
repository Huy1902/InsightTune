package com.example.frontend.data.remote

import com.example.frontend.data.models.user.AuthResponseDto
import com.example.frontend.data.models.user.ChangePasswordRequest
import com.example.frontend.data.models.user.ChangePasswordResponse
import com.example.frontend.data.models.user.CheckEmailResponse
import com.example.frontend.data.models.user.ForgotPasswordRequest
import com.example.frontend.data.models.user.ForgotPasswordResponse
import com.example.frontend.data.models.user.GoogleRequest
import com.example.frontend.data.models.user.GoogleResponse
import com.example.frontend.data.models.user.LogOutRequest
import com.example.frontend.data.models.user.LoginRequest
import com.example.frontend.data.models.user.LogoutResponseDto
import com.example.frontend.data.models.user.RefreshRequest
import com.example.frontend.data.models.user.RefreshResponseDto
import com.example.frontend.data.models.user.RegisterRequest
import com.example.frontend.data.models.user.RegisterResponseDto
import com.example.frontend.data.models.user.UpdateUserRequest
import com.example.frontend.data.models.user.UserDto
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Query

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponseDto>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponseDto>
    @GET("api/auth/check-email")
    suspend fun checkEmail(@Query("email") email: String): CheckEmailResponse

    @POST("auth/logout")
    suspend fun logout(@Body request: LogOutRequest): Response<LogoutResponseDto>


    @Headers("Content-Type: application/json")
    @POST("auth/refresh")
    fun refresh(
        @Header("Authorization") accessToken: String,
        @Body request: RefreshRequest
    ): Call<RefreshResponseDto>

    @PUT("user/changePassword")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<ChangePasswordResponse>

    @POST("auth/forgot_password")
    suspend fun forgotPassword(@Query("email") email: String): Response<ForgotPasswordResponse>

    @PATCH("user/forgotPassword")
    suspend fun resetPassword(
        @Body request: ForgotPasswordRequest
    ): Response<ForgotPasswordResponse>

}

interface GoogleAuthApi {
    @POST("api/auth/google")
    suspend fun verifyIdToken(@Body body: Map<String, String>): Response<GoogleResponse>
}

