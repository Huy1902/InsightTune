package com.example.frontend.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.frontend.core.AppPreferences
import com.example.frontend.core.SessionManager
import com.example.frontend.ui.home.HomeScreen
import com.example.frontend.ui.home.HomeViewModel
import com.example.frontend.ui.home.HomeViewModelFactory
import com.example.frontend.ui.login.LogInScreen
import com.example.frontend.ui.playingsong.MusicPlayer
import com.example.frontend.ui.playingsong.MusicPlayerViewModel
import com.example.frontend.ui.playingsong.MusicPlayerViewModelFactory
import com.example.frontend.ui.profile.ProfileScreen
import com.example.frontend.ui.profile.ProfileViewModel
import com.example.frontend.ui.profile.ProfileViewModelFactory
import com.example.frontend.ui.signup.AuthViewModel
import com.example.frontend.ui.signup.AuthViewModelFactory
import com.example.frontend.ui.signup.SignUpScreenStep1
import com.example.frontend.ui.signup.SignUpScreenStep2
import com.example.frontend.ui.signup.SignUpScreenStep3
import com.example.frontend.ui.start.StartScreen
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable

object AppGraph {
    const val AUTH = "auth_graph"
    const val MAIN = "main_graph"
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavHost(prefs: AppPreferences, navController: NavHostController) {
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
                when (targetState.destination.route) {
                    NavRoutes.Track.route ->
                        slideInVertically(
                            initialOffsetY = { it },
                            animationSpec = tween(durationMillis = duration, easing = easing)
                        )

                    else ->
                        slideInHorizontally(
                            initialOffsetX = { it },
                            animationSpec = tween(durationMillis = duration, easing = easing)
                        ) + fadeIn(animationSpec = tween(durationMillis = duration))
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    NavRoutes.Track.route ->
                        fadeOut(animationSpec = tween(durationMillis = duration))
                    else ->
                        slideOutHorizontally(
                            targetOffsetX = { -it },
                            animationSpec = tween(durationMillis = duration, easing = easing)
                        ) + fadeOut(animationSpec = tween(durationMillis = duration))
                }
            },
            popEnterTransition = {
                when (initialState.destination.route) {
                    NavRoutes.Track.route ->
                        EnterTransition.None
                    else ->
                        slideInHorizontally(
                            initialOffsetX = { -it },
                            animationSpec = tween(durationMillis = duration, easing = easing)
                        ) + fadeIn(animationSpec = tween(durationMillis = duration))
                }
            },
            popExitTransition = {
                when (initialState.destination.route) {
                    NavRoutes.Track.route ->
                        slideOutVertically (
                            targetOffsetY = {it},
                            animationSpec = tween(durationMillis = duration, easing = easing)
                        ) + fadeOut(animationSpec = tween(durationMillis = duration))
                    else ->
                        slideOutHorizontally(
                            targetOffsetX = { it },
                            animationSpec = tween(durationMillis = duration, easing = easing)
                        ) + fadeOut(animationSpec = tween(durationMillis = duration))
                }
            }
        ) {
            authGraph(navController)
            mainGraph(navController)
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
private fun NavGraphBuilder.authGraph(navController: NavHostController) {
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
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("http://10.0.2.2:8080/oauth2/authorization/google")
                    )
                    context.startActivity(intent)
                }
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
                onNext = { navController.navigate(NavRoutes.Login.route) },
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
private fun NavGraphBuilder.mainGraph(navController: NavHostController) {
    navigation(
        startDestination = NavRoutes.Home.route,
        route = AppGraph.MAIN
    ) {

        composable(NavRoutes.Home.route) {
            val vm: HomeViewModel = viewModel(factory = HomeViewModelFactory(LocalContext.current))
            HomeScreen(
                vm,
                appNavController = navController
            )
        }

        composable(NavRoutes.Profile.route) {
            val vm: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(LocalContext.current))
            ProfileScreen(
                vm = vm,
                onNavigateLogin = {
                    navController.navigate(AppGraph.AUTH) {
                        popUpTo(AppGraph.MAIN) {
                            inclusive = true
                        }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable (
            NavRoutes.Track.route,
            arguments = listOf(
                navArgument("urlKey") {type = NavType.StringType},
                navArgument("title") {type = NavType.StringType},
                navArgument("artist") {type = NavType.StringType},
                navArgument("imageKey") {type = NavType.StringType},
            )
        ) {
                backStackEntry  ->
                val context = LocalContext.current
                val url = backStackEntry.arguments?.getString("urlKey") ?: ""
                val title = backStackEntry.arguments?.getString("title") ?: ""
                val artist = backStackEntry.arguments?.getString("artist") ?: ""
                val imageUrl = backStackEntry.arguments?.getString("imageKey") ?: ""

            val vm: MusicPlayerViewModel = viewModel(
                factory = MusicPlayerViewModelFactory(url, title, artist, imageUrl, context)
            )
            MusicPlayer(
                viewModel = vm,
                onBack = { navController.popBackStack() }
            )
        }

        composable (NavRoutes.Search.route) {

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