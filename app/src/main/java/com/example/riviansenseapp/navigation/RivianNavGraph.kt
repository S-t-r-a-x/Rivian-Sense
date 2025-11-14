package com.example.riviansenseapp.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.riviansenseapp.ui.screens.*
import com.example.riviansenseapp.viewmodel.MainViewModel

@Composable
fun RivianNavGraph(
    navController: NavHostController,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var features by remember { mutableStateOf(getDefaultFeatures()) }
    
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onTimeout = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Home.route) {
            HomeScreen(
                onPlaySpotify = { viewModel.playSpotify() },
                onStartBreathing = { navController.navigate(Screen.Breathing.route) },
                onOpenSettings = { navController.navigate(Screen.Settings.route) }
            )
        }
        
        composable(Screen.Breathing.route) {
            BreathingScreen(
                onComplete = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                features = features,
                onToggleFeature = { id ->
                    features = features.map { feature ->
                        if (feature.id == id) {
                            feature.copy(enabled = !feature.enabled)
                        } else {
                            feature
                        }
                    }
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
