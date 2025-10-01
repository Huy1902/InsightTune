package com.example.frontend.core

import android.content.Context

class AppPreferences(context: Context) {
    private val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        prefs.edit().putString(Constants.KEY_TOKEN, token).apply()
    }

    fun getToken(): String? {
        return prefs.getString(Constants.KEY_TOKEN, null)
    }

    fun clearToken() {
        prefs.edit().remove(Constants.KEY_TOKEN).apply()
    }

    fun saveRefreshToken(refreshToken: String) {
        prefs.edit().putString(Constants.KEY_REFRESH_TOKEN, refreshToken).apply()
    }

    fun getRefreshToken(): String? {
        return prefs.getString(Constants.KEY_REFRESH_TOKEN, null)
    }

    fun clearRefreshToken() {
        prefs.edit().remove(Constants.KEY_REFRESH_TOKEN).apply()
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }
}
