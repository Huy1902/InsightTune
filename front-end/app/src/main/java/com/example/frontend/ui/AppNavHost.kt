package com.example.frontend.ui

import HomeScreen
import android.R.attr.duration
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.app.ui.NavRoutes
import com.example.frontend.ui.login.LogInScreen
import com.example.frontend.ui.signup.SignUpScreenStep1
import com.example.frontend.ui.signup.SignUpScreenStep2
import com.example.frontend.ui.signup.SignUpScreenStep3
import com.example.frontend.ui.start.StartScreen
import com.example.playingasong.view.Playingasong
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavHost() {
    val navController = rememberNavController()
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
            startDestination = NavRoutes.StartScreen.route,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(durationMillis = duration, easing = easing)
                ) + fadeIn(animationSpec = tween(durationMillis = duration))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(durationMillis = duration, easing = easing)
                ) + fadeOut(animationSpec = tween(durationMillis = duration))
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(durationMillis = duration, easing = easing)
                ) + fadeIn(animationSpec = tween(durationMillis = duration))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(durationMillis = duration, easing = easing)
                ) + fadeOut(animationSpec = tween(durationMillis = duration))
            }
        ) {
            composable(NavRoutes.StartScreen.route) {
                StartScreen(
                    onNextSignUp = { navController.navigate(NavRoutes.SignUpStep1.route) },
                    onNextLogIn = { navController.navigate(NavRoutes.Login.route) }
                )
            }

            composable(NavRoutes.SignUpStep1.route) {
                SignUpScreenStep1(
                    onNext = { navController.navigate(NavRoutes.SignUpStep2.route) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(NavRoutes.SignUpStep2.route) {
                SignUpScreenStep2(
                    onNext = { navController.navigate(NavRoutes.SignUpStep3.route) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(NavRoutes.SignUpStep3.route) {
                SignUpScreenStep3(
                    onNext = { navController.navigate(NavRoutes.Login.route) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(NavRoutes.Login.route) {
                LogInScreen(
                    onNext = { navController.navigate(NavRoutes.Home.route) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(NavRoutes.Home.route) {
                HomeScreen()
            }

        }
    }
}
