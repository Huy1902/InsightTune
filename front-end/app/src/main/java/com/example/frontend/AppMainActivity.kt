package com.example.frontend

import android.content.Intent
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
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavHostController
import com.example.frontend.core.AppPreferences
import com.example.frontend.data.remote.ApiClient
import com.example.frontend.ui.AppNavHost
import com.example.frontend.ui.NavRoutes


@OptIn(ExperimentalAnimationApi::class)
class AppMainActivity : ComponentActivity() {
    lateinit var prefs: AppPreferences
    lateinit var navController: NavHostController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = AppPreferences(this)
        ApiClient.init(prefs)

        setContent {
            navController = rememberNavController()
            AppNavHost(prefs = prefs, navController = navController)
        }

        handleDeepLink(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        val data = intent?.data ?: return
        if (data.scheme == "myapp" && data.host == "oauth2" && data.path == "/callback") {
            val token = data.getQueryParameter("token")
            if (token != null) {
                prefs.saveToken(token)
                navController.navigate(NavRoutes.Home.route) {
                    popUpTo(NavRoutes.StartScreen.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }
}
