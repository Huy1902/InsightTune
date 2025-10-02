package com.example.frontend.data.remote

import com.example.frontend.core.AppPreferences
import com.example.frontend.core.AuthInterceptor
import com.example.frontend.core.Constants
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

object ApiClient {

    private lateinit var prefs: AppPreferences

    fun init(prefs: AppPreferences) {
        this.prefs = prefs
    }

    private val client by lazy {
        okhttp3.OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(prefs))
            .build()
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val userApi: UserApi by lazy {
        retrofit.create(UserApi::class.java)
    }

    private val googleAuthApi: GoogleAuthApi by lazy {
        retrofit.create(GoogleAuthApi::class.java)
    }

    suspend fun loginWithGoogleCode(code: String): String {
        val response = googleAuthApi.exchangeCode(mapOf("code" to code))
        if (response.isSuccessful) {
            return response.body()?.get("token") ?: throw Exception("Don't get JWT")
        } else {
            throw Exception("Backend error ${response.code()}")
        }
    }
}

interface GoogleAuthApi {
    @POST("auth/google/callback")
    suspend fun exchangeCode(@Body body: Map<String, String>): Response<Map<String, String>>
}

