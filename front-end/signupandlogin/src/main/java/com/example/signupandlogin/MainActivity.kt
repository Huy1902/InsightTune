package com.example.signupandlogin

import android.R.attr.duration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.signupandlogin.controller.SignUpAndLogInController
import com.example.signupandlogin.model.SignupModel
import com.example.signupandlogin.ui.theme.FrontEndTheme
import com.example.signupandlogin.view.LogInScreen
import com.example.signupandlogin.view.SignUpScreenStep1
import com.example.signupandlogin.view.SignUpScreenStep2
import com.example.signupandlogin.view.SignUpScreenStep3
import com.example.signupandlogin.view.StartScreen
import com.google.accompanist.navigation.animation.AnimatedNavHost

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val model = SignupModel()
            val controller = SignUpAndLogInController(navController, model)
            val duration = 600
            val easing = FastOutSlowInEasing
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to Color(0xFFFF0000), // đỏ
                                0.3f to Color(0xFF8B0000), // đỏ sậm
                                0.6f to Color(0xFF000000)  // đen
                            )
                        )
                    )
            ) {
                AnimatedNavHost(
                    navController = navController,
                    startDestination = "start_screen",
                    enterTransition = {
                        slideInHorizontally(
                            initialOffsetX = { it }, // từ phải vào
                            animationSpec = tween(durationMillis = duration, easing = easing)
                        ) + fadeIn(animationSpec = tween(durationMillis = duration))
                    },
                    exitTransition = {
                        slideOutHorizontally(
                            targetOffsetX = { -it }, // trượt sang trái
                            animationSpec = tween(durationMillis = duration, easing = easing)
                        ) + fadeOut(animationSpec = tween(durationMillis = duration))
                    },
                    popEnterTransition = {
                        slideInHorizontally(
                            initialOffsetX = { -it }, // từ trái vào
                            animationSpec = tween(durationMillis = duration, easing = easing)
                        ) + fadeIn(animationSpec = tween(durationMillis = duration))
                    },
                    popExitTransition = {
                        slideOutHorizontally(
                            targetOffsetX = { it }, // trượt sang phải
                            animationSpec = tween(durationMillis = duration, easing = easing)
                        ) + fadeOut(animationSpec = tween(durationMillis = duration))
                    }
                ) {
                    composable("start_screen") { StartScreen(controller) }
                    composable("login") { LogInScreen(controller) }
                    composable("signup_step1") { SignUpScreenStep1(controller) }
                    composable("signup_step2") { SignUpScreenStep2(controller) }
                    composable("signup_step3") { SignUpScreenStep3(controller) }
                }

            }
        }

    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FrontEndTheme {
        Greeting("Android")
    }
}