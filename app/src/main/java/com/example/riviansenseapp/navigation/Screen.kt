package com.example.riviansenseapp.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Breathing : Screen("breathing")
    object MicroStretch : Screen("micro_stretch")
    object Settings : Screen("settings")
    object TestActions : Screen("test_actions")
}

