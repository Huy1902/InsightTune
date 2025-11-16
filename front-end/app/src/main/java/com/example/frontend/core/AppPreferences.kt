package com.example.frontend.core

import android.content.Context
import com.example.frontend.ui.theme.ThemeSetting

class AppPreferences(context: Context) {
    private val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)

    private val KEY_RECENT_SEARCHES = "recent_searches"
    private val MAX_RECENT_SEARCHES = 10

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

    fun saveTheme(theme: ThemeSetting) {
        prefs.edit().putString("app_theme", theme.name).apply()
    }

    fun getTheme(): ThemeSetting {
        val themeName = prefs.getString("app_theme", ThemeSetting.SYSTEM.name)
        return ThemeSetting.valueOf(themeName ?: ThemeSetting.SYSTEM.name)
    }

    fun saveLanguage(languageCode: String) {
        prefs.edit().putString("app_language", languageCode).apply()
    }

    fun getLanguage(): String {
        return prefs.getString("app_language", "en") ?: "en"
    }

    fun getRecentSearches(): List<String> {
        val searchSet = prefs.getStringSet(KEY_RECENT_SEARCHES, emptySet()) ?: emptySet()
        return searchSet.toList().reversed()
    }

    fun addRecentSearch(query: String) {
        if (query.isBlank()) return

        val oldSearches = prefs.getStringSet(KEY_RECENT_SEARCHES, emptySet())?.toMutableSet() ?: mutableSetOf()

        oldSearches.remove(query)

        oldSearches.add(query)

        var searchesList = oldSearches.toList()
        if (searchesList.size > MAX_RECENT_SEARCHES) {
            searchesList = searchesList.takeLast(MAX_RECENT_SEARCHES)
        }

        prefs.edit().putStringSet(KEY_RECENT_SEARCHES, searchesList.toSet()).apply()
    }
}
