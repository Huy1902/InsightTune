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

    fun clear() {
        prefs.edit().clear().apply()
    }
}
