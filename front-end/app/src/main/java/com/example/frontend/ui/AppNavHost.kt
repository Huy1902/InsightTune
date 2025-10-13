package com.example.frontend.ui

import android.R.attr.duration
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.example.frontend.ui.home.BottomNavItem
import com.example.frontend.ui.NavRoutes
import com.example.frontend.core.AppPreferences
import com.example.frontend.core.SessionManager
import com.example.frontend.ui.home.HomeScreen
import com.example.frontend.ui.home.HomeViewModel
import com.example.frontend.ui.home.HomeViewModelFactory
import com.example.frontend.ui.login.LogInScreen
import com.example.frontend.ui.profile.ProfileScreen
import com.example.frontend.ui.profile.ProfileViewModel
import com.example.frontend.ui.profile.ProfileViewModelFactory
import com.example.frontend.ui.signup.AuthViewModel
import com.example.frontend.ui.signup.AuthViewModelFactory
import com.example.frontend.ui.signup.SignUpScreenStep1
import com.example.frontend.ui.signup.SignUpScreenStep2
import com.example.frontend.ui.signup.SignUpScreenStep3
import com.example.frontend.ui.start.StartScreen
import com.example.frontend.ui.theme.AppTheme
import com.example.frontend.ui.theme.ThemeSetting
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable

object AppGraph {
    const val AUTH = "auth_graph"
    const val MAIN = "main_graph"
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavHost(
    prefs: AppPreferences,
    navController: NavHostController,
    themeSetting: ThemeSetting,
    onThemeChange: (ThemeSetting) -> Unit
) {
    val navController = rememberNavController()
    val duration = 600
    val easing = FastOutSlowInEasing
    val context = LocalContext.current
    val startDestination = if (prefs.getToken() != null) AppGraph.MAIN else AppGraph.AUTH

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(key1 = lifecycleOwner, key2 = navController) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            SessionManager.logoutEvents.collect {
                if (navController.currentDestination?.parent?.route == AppGraph.MAIN) {
                    navController.navigate(AppGraph.AUTH) {
                        popUpTo(AppGraph.MAIN) { inclusive = true }
                    }
                }
            }
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
            authGraph(navController, prefs)
            mainGraph(navController, prefs, themeSetting, onThemeChange)
        }
    }
}


@OptIn(ExperimentalAnimationApi::class)
private fun NavGraphBuilder.authGraph(navController: NavHostController, prefs: AppPreferences) {
    navigation(
        startDestination = NavRoutes.StartScreen.route,
        route = AppGraph.AUTH
    ) {
        composable(NavRoutes.StartScreen.route) {
            val context = LocalContext.current
            StartScreen(
                onNextSignUp = { navController.navigate(NavRoutes.SignUpStep1.route) },
                onNextLogIn = { navController.navigate(NavRoutes.Login.route) },
                onGoogleLogin = {
                    navController.navigate(AppGraph.MAIN) {
                        popUpTo(AppGraph.AUTH) {
                            inclusive = true
                        }
                    }
                },
                prefs
            )
        }
        composable(NavRoutes.SignUpStep1.route) { navBackStackEntry ->
            val vm = navBackStackEntry.sharedAuthViewModel(navController = navController)
            SignUpScreenStep1(
                vm,
                onNext = { navController.navigate(NavRoutes.SignUpStep2.route) },
                onBack = { navController.popBackStack() })
        }

        composable(NavRoutes.SignUpStep2.route) { navBackStackEntry ->
            val vm = navBackStackEntry.sharedAuthViewModel(navController = navController)
            SignUpScreenStep2(
                vm,
                onNext = { navController.navigate(NavRoutes.SignUpStep3.route) },
                onBack = { navController.popBackStack() })
        }

        composable(NavRoutes.SignUpStep3.route) { navBackStackEntry ->
            val vm = navBackStackEntry.sharedAuthViewModel(navController = navController)
            SignUpScreenStep3(
                vm,
                onNext = { navController.navigate(NavRoutes.StartScreen.route) },
                onBack = { navController.popBackStack() })
        }

        composable(NavRoutes.Login.route) { navBackStackEntry ->
            val vm = navBackStackEntry.sharedAuthViewModel(navController = navController)
            LogInScreen(
                vm = vm,
                onNext = {
                    navController.navigate(AppGraph.MAIN) {
                        popUpTo(AppGraph.AUTH) {
                            inclusive = true
                        }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}


@OptIn(ExperimentalAnimationApi::class)
private fun NavGraphBuilder.mainGraph(
    navController: NavHostController,
    prefs: AppPreferences,
    themeSetting: ThemeSetting,
    onThemeChange: (ThemeSetting) -> Unit
) {
    navigation(
        startDestination = NavRoutes.Home.route,
        route = AppGraph.MAIN
    ) {

        composable(NavRoutes.Home.route) {
            val vm: HomeViewModel = viewModel(factory = HomeViewModelFactory(LocalContext.current))
            HomeScreen(
                vm,
                appNavController = navController,
                themeSetting = themeSetting,
                onThemeChange = onThemeChange
            )
        }

        composable(NavRoutes.Profile.route) {
            val vm: ProfileViewModel =
                viewModel(factory = ProfileViewModelFactory(LocalContext.current))
            ProfileScreen(
                vm = vm,
                onNavigateLogin = {
                    navController.navigate(AppGraph.AUTH) {
                        popUpTo(AppGraph.MAIN) {
                            inclusive = true
                        }
                    }
                },
                onBack = { navController.popBackStack() },
                themeSetting = themeSetting,
                onThemeChange = onThemeChange
            )
        }
    }
}

@Composable
fun NavBackStackEntry.sharedAuthViewModel(navController: NavHostController): AuthViewModel {
    val parentEntry = remember(this) {
        navController.getBackStackEntry(AppGraph.AUTH)
    }
    val context = LocalContext.current

    return viewModel(viewModelStoreOwner = parentEntry, factory = AuthViewModelFactory(context))
}