package com.example.frontend.data.remote

import com.example.frontend.core.AppPreferences
import com.example.frontend.core.AuthInterceptor
import com.example.frontend.core.Constants
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

object ApiClient {

    private lateinit var prefs: AppPreferences
    private val mainClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(prefs) { refreshAuthApi }) // Sửa ở đây
            .addInterceptor(logging)
            .build()
    }

    private val refreshClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    private val gatewayRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(mainClient) // Dùng mainClient
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val refreshRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(refreshClient) // Dùng refreshClient
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }


    // AuthApi dùng cho các request thông thường (login, register...)
    val authApi: AuthApi by lazy {
        gatewayRetrofit.create(AuthApi::class.java)
    }

    // AuthApi "sạch" chỉ để cung cấp cho Interceptor
    private val refreshAuthApi: AuthApi by lazy {
        refreshRetrofit.create(AuthApi::class.java)
    }

    // Các Api khác của bạn giữ nguyên, nhưng đảm bảo chúng dùng mainClient
    val userApi: UserApi by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.USER_SERVICE_BASE_URL)
            .client(mainClient) // Đảm bảo dùng client chính
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UserApi::class.java)
    }

    // Hàm init của bạn không cần thay đổi nhiều
    fun init(prefs: AppPreferences) {
        this.prefs = prefs
    }
    suspend fun loginWithGoogleCode(code: String): String {
        val response = googleAuthApi.exchangeCode(mapOf("code" to code))
        if (response.isSuccessful) {
            return response.body()?.get("token") ?: throw Exception("Don't get JWT")
        } else {
            throw Exception("Backend error ${response.code()}")
        }
    }
    val googleAuthApi: GoogleAuthApi by lazy {
        gatewayRetrofit.create(GoogleAuthApi::class.java)
    }
}

interface GoogleAuthApi {
    @POST("auth/google/callback")
    suspend fun exchangeCode(@Body body: Map<String, String>): Response<Map<String, String>>
}

