package com.example.riviansenseapp.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Breathing : Screen("breathing")
    object Settings : Screen("settings")
}
