package com.example.frontend.core

import android.util.Base64
import android.util.Log
import com.example.frontend.data.models.user.RefreshRequest
import com.example.frontend.data.remote.AuthApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import okhttp3.Interceptor
import okhttp3.Response
import org.json.JSONObject


class AuthInterceptor(
    private val prefs: AppPreferences,
    private val authApiProvider: () -> AuthApi
) : Interceptor {

    private val lock = Any()

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val currentToken = prefs.getToken()

        if (!currentToken.isNullOrBlank()) {
            val exp = getExpiryTime(currentToken)
            val now = System.currentTimeMillis()

            if (exp != null && exp - now <= 2 * 60 * 1000) {
                refreshTokenIfNeeded(currentToken)
            }
        }

        val requestBuilder = original.newBuilder()
        prefs.getToken()?.let {
            requestBuilder.header("Authorization", "Bearer $it")
        }
        val request = requestBuilder.build()

        val response = chain.proceed(request)

        if (response.code == 401) {
            response.close()

            val newToken = refreshTokenIfNeeded(currentToken)

            if (newToken != null) {
                val newRequest = request.newBuilder()
                    .header("Authorization", "Bearer $newToken")
                    .build()
                return chain.proceed(newRequest)
            } else {
                SessionManager.sendLogout()
            }
        }

        return response
    }

    private fun refreshTokenIfNeeded(tokenUsedInCall: String?): String? {
        synchronized(lock) {
            val currentToken = prefs.getToken()
            if (currentToken != null && currentToken != tokenUsedInCall) {
                Log.d("AUTH", "↪️ Old token still valid. Skip refresh.")
                return currentToken
            }

            val oldAccess = prefs.getToken()
            val refresh = prefs.getRefreshToken()

            if (oldAccess.isNullOrBlank() || refresh.isNullOrBlank()) {
                Log.e("AUTH", "❌ Cannot refresh without both access and refresh token.")
                return null
            }

            Log.d("AUTH", "⏳ Starting refresh...")
            return try {
                val responseSync = authApiProvider().refresh(
                    "Bearer $oldAccess",
                    RefreshRequest(refresh)
                ).execute()

                if (responseSync.isSuccessful) {
                    val body = responseSync.body()
                    if (body != null) {
                        val result = body.result
                        prefs.saveToken(result.token)
                        if (!result.refreshToken.isNullOrBlank()) {
                            prefs.saveRefreshToken(result.refreshToken)
                        }
                        Log.d("AUTH", "✅ Refresh OK! New token has been saved.")
                        result.token
                    } else {
                        Log.e("AUTH", "❌ Refresh HTTP OK but body null.")
                        null
                    }
                } else {
                    Log.e("AUTH", "❌ Refresh HTTP fail: ${responseSync.code()} ${responseSync.message()}")
                    null
                }
            } catch (e: Exception) {
                Log.e("AUTH", "❌ Refresh exception", e)
                null
            }
        }
    }

    private fun getExpiryTime(token: String): Long? {
        return try {
            val parts = token.split(".")
            if (parts.size < 2) return null
            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE))
            val json = JSONObject(payload)
            json.getLong("exp") * 1000
        } catch (e: Exception) {
            null
        }
    }
}

object SessionManager {
    private val _logoutEvents = MutableSharedFlow<Unit>(replay = 0)
    val logoutEvents: SharedFlow<Unit> = _logoutEvents

    fun sendLogout() {
        _logoutEvents.tryEmit(Unit)
    }
}

