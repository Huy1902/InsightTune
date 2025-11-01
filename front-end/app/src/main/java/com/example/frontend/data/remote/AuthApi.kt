package com.example.frontend.data.remote

import com.example.frontend.data.register.AuthResponseDto
import com.example.frontend.data.models.user.ChangePasswordRequest
import com.example.frontend.data.models.user.ChangePasswordResponse
import com.example.frontend.data.register.CheckEmailResponse
import com.example.frontend.data.register.ForgotPasswordRequest
import com.example.frontend.data.register.ForgotPasswordResponse
import com.example.frontend.data.register.GoogleResponse
import com.example.frontend.data.models.login.LogOutRequest
import com.example.frontend.data.models.login.LoginRequest
import com.example.frontend.data.register.LogoutResponseDto
import com.example.frontend.data.models.login.RefreshRequest
import com.example.frontend.data.register.RefreshResponseDto
import com.example.frontend.data.register.RegisterRequest
import com.example.frontend.data.register.RegisterResponseDto
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
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

