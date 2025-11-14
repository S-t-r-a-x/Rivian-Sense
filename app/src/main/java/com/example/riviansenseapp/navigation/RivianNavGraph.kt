package com.example.riviansenseapp.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.riviansenseapp.actions.NavigationAction
import com.example.riviansenseapp.ui.screens.*
import com.example.riviansenseapp.viewmodel.MainViewModel

@Composable
fun RivianNavGraph(
    navController: NavHostController,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
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
                onOpenSettings = { navController.navigate(Screen.Settings.route) },
                onOpenTestActions = { navController.navigate(Screen.TestActions.route) }
            )
        }
        
        composable(Screen.Breathing.route) {
            BreathingScreen(
                onComplete = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.MicroStretch.route) {
            MicroStretchScreen(
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
        
        composable(Screen.TestActions.route) {
            TestActionsScreen(
                onBack = { navController.popBackStack() },
                onPlaySpotify = { viewModel.playSpotify() },
                onPlayPodcast = { viewModel.playPodcastOrAudiobook() },
                onFadeOutMusic = { viewModel.fadeOutMusic() },
                onSetVolume = { volume -> viewModel.setSpotifyVolume(volume) },
                onPlayNatureSound = { type -> viewModel.playNatureSoundscape(type) },
                onStopNatureSound = { viewModel.stopNatureSoundscape() },
                onEnableDND = { mode -> viewModel.enableDrivingDND(mode) },
                onDisableDND = { viewModel.disableDrivingDND() },
                onAutoReply = { viewModel.autoReplyToMessages() },
                onNavigateHome = { 
                    viewModel.openNavigationTo("Home", NavigationAction.DestinationType.HOME)
                },
                onNavigateWork = { 
                    viewModel.openNavigationTo("Work", NavigationAction.DestinationType.WORK)
                },
                onSuggestBreak = { type -> viewModel.suggestBreak(type) },
                onStartBreathing = { navController.navigate(Screen.Breathing.route) },
                onStartStretch = { navController.navigate(Screen.MicroStretch.route) },
                onCreateReminder = { 
                    viewModel.createMissedCallReminder("Jovan", "+381641234567")
                },
                onPrintReminders = {
                    viewModel.printAllReminders()
                },
                onClearAllReminders = {
                    viewModel.clearAllReminders()
                },
                onPlayChime = { viewModel.playSoftChimeForAggressivePattern() },
                onShowStreak = { 
                    val streak = viewModel.showStreakCard()
                    // TODO: Show streak in UI or Toast
                }
            )
        }
    }
}
