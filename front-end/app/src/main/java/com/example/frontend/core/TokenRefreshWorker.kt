package com.example.frontend.core

import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.frontend.data.models.login.RefreshRequest
import com.example.frontend.data.remote.ApiClient
import com.example.frontend.data.remote.AuthApi
import org.json.JSONObject

class TokenRefreshWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val prefs = AppPreferences(context)
    private val authApi = ApiClient.authApi

    override suspend fun doWork(): Result {
        val token = prefs.getToken()
        val refreshToken = prefs.getRefreshToken()

        if (token.isNullOrBlank() || refreshToken.isNullOrBlank()) {
            Log.w("TOKEN_WORKER", "No token or refresh token found. Skipping refresh.")
            return Result.success()
        }

        val expiryTime = getExpiryTime(token)
        if (expiryTime == null) {
            Log.w("TOKEN_WORKER", "Failed to parse token expiration.")
            return Result.success()
        }

        val remaining = expiryTime - System.currentTimeMillis()
        if (remaining <= 10 * 60 * 1000) { // 10 minutes
            Log.d("TOKEN_WORKER", "Token is about to expire. Refreshing...")

            try {
                val response = authApi.refresh(
                    "Bearer $token",
                    RefreshRequest(refreshToken)
                ).execute()

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        val result = body.result
                        prefs.saveToken(result.token)
                        if (!result.refreshToken.isNullOrBlank()) {
                            prefs.saveRefreshToken(result.refreshToken)
                        }
                        Log.i("TOKEN_WORKER", "Token successfully refreshed.")
                    } else {
                        Log.e("TOKEN_WORKER", "Refresh succeeded but response body is null.")
                    }
                } else {
                    Log.e("TOKEN_WORKER", "Refresh failed: ${response.code()} ${response.message()}")
                    if (response.code() == 401) {
                        SessionManager.sendLogout()
                    }
                }
            } catch (e: Exception) {
                Log.e("TOKEN_WORKER", "Exception while refreshing token", e)
            }
        } else {
            Log.d(
                "TOKEN_WORKER",
                "Token still valid for ${remaining / 1000 / 60} minutes. No refresh needed."
            )
        }

        return Result.success()
    }

    private fun getExpiryTime(token: String): Long? {
        return try {
            val parts = token.split(".")
            if (parts.size < 2) return null
            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE))
            val json = JSONObject(payload)
            json.getLong("exp") * 1000
        } catch (e: Exception) {
            Log.e("TOKEN_WORKER", "Failed to decode token", e)
            null
        }
    }
}
