package com.example.frontend

import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import android.os.Bundle
import android.util.Log
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavHostController
import com.example.frontend.core.AppPreferences
import com.example.frontend.data.remote.ApiClient
import com.example.frontend.ui.AppNavHost
import com.example.frontend.ui.NavRoutes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@OptIn(ExperimentalAnimationApi::class)
class AppMainActivity : ComponentActivity() {
    private lateinit var prefs: AppPreferences
    private lateinit var navController: NavHostController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = AppPreferences(this)
        ApiClient.init(prefs)

        setContent {
            navController = rememberNavController()
            AppNavHost(prefs = prefs, navController = navController)
        }

        handleDeepLink(intent) // xử lý khi app được mở bằng deep link
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        val data: Uri = intent?.data ?: return

        if (data.scheme == "com.example.frontend" && data.host == "oauth2redirect") {
            val code = data.getQueryParameter("code")
            if (code != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val jwt = ApiClient.loginWithGoogleCode(code)
                        prefs.saveToken(jwt)
                        withContext(Dispatchers.Main) {
                            navController.navigate(NavRoutes.Home.route) {
                                popUpTo(NavRoutes.StartScreen.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("OAUTH2", "Google login failed: ${e.message}")
                    }
                }
            }
        }
    }

}

