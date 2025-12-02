package com.example.drivesenseapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.drivesenseapp.ui.screens.*
import com.example.drivesenseapp.viewmodel.MainViewModel

@Composable
fun DriveNavGraph(
    navController: NavHostController,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
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
                onOpenSettings = { navController.navigate(Screen.Settings.route) },
                onOpenTestActions = { navController.navigate(Screen.TestActions.route) },
                onOpenStats = { navController.navigate(Screen.Stats.route) }
            )
        }

        composable(Screen.Breathing.route) {
            BreathingScreen(onComplete = { navController.popBackStack() })
        }

        composable(Screen.MicroStretch.route) {
            MicroStretchScreen(onComplete = { navController.popBackStack() })
        }

        composable(Screen.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.TestActions.route) {
            TestActionsScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Stats.route) {
            val statsManager = viewModel.getStatsManager()
            if (statsManager != null) {
                StatsScreen(statsManager = statsManager, onNavigateBack = { navController.popBackStack() })
            }
        }
    }
}