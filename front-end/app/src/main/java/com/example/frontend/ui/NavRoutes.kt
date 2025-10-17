package com.example.frontend.ui

sealed class NavRoutes(val route: String) {
    object StartScreen : NavRoutes("start_screen")
    object SignUpStep1 : NavRoutes("signup_step1")
    object SignUpStep2 : NavRoutes("signup_step2")
    object SignUpStep3 : NavRoutes("signup_step3")
    object Login : NavRoutes("login")
    object Home : NavRoutes("home")

    object Profile : NavRoutes("profile")

    object Search : NavRoutes("search")

    object Track : NavRoutes("track/{urlKey}/{title}/{artist}/{imageKey}")
}
