package com.example.frontend.ui

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.rememberNavController
import com.example.frontend.ui.home.BottomNavItem
import com.example.frontend.ui.NavRoutes
import com.example.frontend.core.AppPreferences
import com.example.frontend.ui.home.HomeScreen
import com.example.frontend.ui.login.LogInScreen
import com.example.frontend.ui.profile.ProfileScreen
import com.example.frontend.ui.profile.ProfileViewModel
import com.example.frontend.ui.profile.ProfileViewModelFactory
import com.example.frontend.ui.signup.AuthViewModel
import com.example.frontend.ui.signup.SignUpScreenStep1
import com.example.frontend.ui.signup.SignUpScreenStep2
import com.example.frontend.ui.signup.SignUpScreenStep3
import com.example.frontend.ui.start.StartScreen
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavHost(prefs: AppPreferences) {
    val navController = rememberNavController()
    val duration = 600
    val easing = FastOutSlowInEasing
    val context = LocalContext.current
    val vm = remember { AuthViewModel(context) }
    val startDestination = if (prefs.getToken() != null) {
        NavRoutes.Home.route
    } else {
        NavRoutes.StartScreen.route
    }

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
            startDestination = startDestination,
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
                    vm,
                    onNext = { navController.navigate(NavRoutes.SignUpStep2.route) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(NavRoutes.SignUpStep2.route) {
                SignUpScreenStep2(
                    vm,
                    onNext = { navController.navigate(NavRoutes.SignUpStep3.route) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(NavRoutes.SignUpStep3.route) {
                SignUpScreenStep3(
                    vm,
                    onNext = { navController.navigate(NavRoutes.Login.route) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(NavRoutes.Login.route) {
                LogInScreen(
                    vm,
                    onNext = {
                        // Khi đăng nhập thành công, đi đến Home và XÓA SẠCH back stack cũ
                        navController.navigate(NavRoutes.Home.route) {
                            // Xóa tất cả các màn hình trước đó khỏi back stack
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = true
                            }
                            // Đảm bảo không tạo thêm bản sao của Home nếu đã có
                            launchSingleTop = true
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(NavRoutes.Home.route) {
                HomeScreen(navController)
            }
            composable(NavRoutes.Profile.route) {
                val context = LocalContext.current
                val vm: ProfileViewModel = viewModel(
                    factory = ProfileViewModelFactory(context)
                )
                ProfileScreen(
                    vm = vm,
                    onNavigateLogin = {
                        navController.navigate(NavRoutes.StartScreen.route) {
                            popUpTo(0) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
