package com.example.frontend.data.remote

import com.example.frontend.core.AppPreferences
import com.example.frontend.core.AuthInterceptor
import com.example.frontend.core.Constants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

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
}
