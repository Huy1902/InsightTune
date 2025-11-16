package com.example.frontend

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import android.os.Bundle
import android.util.Log
import androidx.activity.SystemBarStyle
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.toArgb
import androidx.navigation.NavHostController
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.frontend.core.AppPreferences
import com.example.frontend.core.LocaleContextWrapper
import com.example.frontend.core.TokenRefreshWorker
import com.example.frontend.data.remote.ApiClient
import com.example.frontend.ui.AppNavHost
import com.example.frontend.ui.NavRoutes
import com.example.frontend.ui.theme.AppTheme
import com.example.frontend.ui.theme.ThemeSetting
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit


@OptIn(ExperimentalAnimationApi::class)
class AppMainActivity : ComponentActivity() {
    private lateinit var prefs: AppPreferences
    private lateinit var navController: NavHostController

    override fun attachBaseContext(newBase: Context) {
        val prefs = AppPreferences(newBase)
        val languageCode = prefs.getLanguage()
        super.attachBaseContext(LocaleContextWrapper.wrap(newBase, languageCode))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = AppPreferences(this)
        ApiClient.init(prefs)
        val workRequest = PeriodicWorkRequestBuilder<TokenRefreshWorker>(
            13, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "TokenRefreshWork",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
        enableEdgeToEdge()

        setContent {
            var currentTheme by remember { mutableStateOf(prefs.getTheme()) }

            val onThemeChange: (ThemeSetting) -> Unit = { theme ->
                prefs.saveTheme(theme)
                currentTheme = theme
            }

            val useDarkTheme = when (currentTheme) {
                ThemeSetting.LIGHT -> false
                ThemeSetting.DARK -> true
                ThemeSetting.SYSTEM -> isSystemInDarkTheme()
            }

            LaunchedEffect(useDarkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = if (useDarkTheme) {
                        SystemBarStyle.dark(
                            scrim = Color.Transparent.toArgb()
                        )
                    } else {
                        SystemBarStyle.light(
                            scrim = Color.Transparent.toArgb(),
                            darkScrim = Color.Transparent.toArgb()
                        )
                    }
                )
            }
            AppTheme(darkTheme = useDarkTheme) {
                navController = rememberNavController()
                AppNavHost(
                    prefs = prefs,
                    navController = navController,
                    themeSetting = currentTheme,
                    onThemeChange = onThemeChange)
            }
        }
    }

}

