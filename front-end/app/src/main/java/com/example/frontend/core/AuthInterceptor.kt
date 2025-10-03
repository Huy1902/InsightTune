package com.example.frontend.core

import android.util.Log
import com.example.frontend.data.models.user.RefreshRequest
import com.example.frontend.data.models.user.RefreshResponseDto
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AuthInterceptor(
    private val prefs: AppPreferences
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val token = prefs.getToken()

        if (token != null) {
            request = request.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        }

        val response = chain.proceed(request)

        if (response.code == 401) {
            response.close()
            val refreshToken = prefs.getRefreshToken()

            if (refreshToken != null) {
                try {
                    Log.d("AuthInterceptor", "Access token expired, refreshing...")

                    val refreshApi = Retrofit.Builder()
                        .baseUrl(Constants.BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .client(OkHttpClient.Builder().build())
                        .build()
                        .create(RefreshApi::class.java)

                    val refreshResponse = refreshApi.refresh(RefreshRequest(refreshToken)).execute()

                    if (refreshResponse.isSuccessful) {
                        val body: RefreshResponseDto? = refreshResponse.body()
                        if (body != null && body.code == 0) {
                            val newToken = body.result.token
                            val newRefreshToken = body.result.refreshToken

                            prefs.saveToken(newToken)
                            prefs.saveRefreshToken(newRefreshToken)

                            val newRequest = request.newBuilder()
                                .removeHeader("Authorization")
                                .addHeader("Authorization", "Bearer $newToken")
                                .build()
                            return chain.proceed(newRequest)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("AuthInterceptor", "Refresh token failed: ${e.message}")
                }
            }
        }

        return response
    }
}
