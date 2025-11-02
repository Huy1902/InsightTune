package com.example.frontend.core

import android.util.Base64
import android.util.Log
import com.example.frontend.data.models.login.RefreshRequest
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

    override fun intercept(chain: Interceptor.Chain): Response {
        synchronized(this) {
            val originalRequest = chain.request()
            val currentToken = prefs.getToken()

            Log.d("AUTH_DEBUG", "Intercepting request for: ${originalRequest.url}")
            Log.d("AUTH_DEBUG", "Current token exists: ${!currentToken.isNullOrBlank()}")
            val tokenToUse = if (currentToken != null && isTokenAboutToExpire(currentToken)) {
                Log.d("AUTH", "⏳ Token is about to expire. Proactively refreshing...")
                performRefresh(currentToken) ?: currentToken
            } else {
                currentToken
            }

            if (tokenToUse.isNullOrBlank()) {
                return chain.proceed(originalRequest)
            }

            Log.d("AUTH_DEBUG", "Attaching token to header.")
            val newRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer $tokenToUse")
                .build()

            val response = chain.proceed(newRequest)

            if (response.code == 401) {
                Log.w("AUTH", "Received 401 despite proactive checks. Session might be invalid.")
                SessionManager.sendLogout()
            }

            return response
        }
    }

    private fun performRefresh(expiredToken: String): String? {
        val refreshToken = prefs.getRefreshToken()
        Log.d("AUTH_REFRESH", "Attempting to refresh. Refresh token exists: ${!refreshToken.isNullOrBlank()}")
        if (refreshToken.isNullOrBlank()) {
            Log.e("AUTH", "❌ Cannot refresh without a refresh token.")
            return null
        }

        Log.d("AUTH", "🚀 Executing refresh API call...")
        return try {
            val responseSync = authApiProvider().refresh(
                "Bearer $expiredToken",
                RefreshRequest(refreshToken)
            ).execute()
            Log.d("AUTH_REFRESH", "Refresh API response code: ${responseSync.code()}")
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
                    Log.e("AUTH", "❌ Refresh HTTP OK but body is null.")
                    null
                }
            } else {
                Log.e("AUTH_REFRESH", "Refresh failed with error body: ${responseSync.errorBody()?.string()}")
                Log.e("AUTH", "❌ Refresh HTTP fail: ${responseSync.code()} ${responseSync.message()}")
                if (responseSync.code() == 401) {
                    SessionManager.sendLogout()
                }
                null
            }
        } catch (e: Exception) {
            Log.e("AUTH", "❌ Refresh exception", e)
            null
        }
    }

    private fun isTokenAboutToExpire(token: String): Boolean {
        val expiryTime = getExpiryTime(token) ?: return false
        val twoMinutesInMillis = 10 * 60 * 1000
        return (expiryTime - System.currentTimeMillis()) <= twoMinutesInMillis
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

