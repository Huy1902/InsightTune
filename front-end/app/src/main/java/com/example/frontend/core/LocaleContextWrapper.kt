package com.example.frontend.core

import android.content.Context
import android.content.ContextWrapper
import android.os.LocaleList
import java.util.Locale

class LocaleContextWrapper(base: Context) : ContextWrapper(base) {
    companion object {
        fun wrap(context: Context, languageCode: String): ContextWrapper {
            val locale = Locale(languageCode)
            Locale.setDefault(locale)

            val config = context.resources.configuration
            config.setLocale(locale)
            val localeList = LocaleList(locale)
            LocaleList.setDefault(localeList)
            config.setLocales(localeList)

            val updatedContext = context.createConfigurationContext(config)
            return LocaleContextWrapper(updatedContext)
        }
    }
}