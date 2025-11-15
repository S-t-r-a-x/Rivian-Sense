package com.example.riviansenseapp.navigation

import androidx.compose.runtime.*
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.riviansenseapp.actions.NavigationAction
import com.example.riviansenseapp.context.ActionType
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
    
    // State za trigger refresh nakon promene Settings-a
    var refreshTrigger by remember { mutableIntStateOf(0) }
    
    // Observe context and smart actions
    val currentContext by viewModel.currentContext.collectAsState()
    val smartActions by viewModel.smartActions.collectAsState()
    
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
                currentMood = when(currentContext.mood) {
                    com.example.riviansenseapp.context.Mood.NERVOUS -> "Nervous"
                    com.example.riviansenseapp.context.Mood.TIRED -> "Tired"
                    com.example.riviansenseapp.context.Mood.NEUTRAL -> "Neutral"
                },
                currentLocation = when(currentContext.location) {
                    com.example.riviansenseapp.context.Location.FOREST -> "Forest"
                    com.example.riviansenseapp.context.Location.CITY -> "City"
                    com.example.riviansenseapp.context.Location.HIGHWAY -> "Highway"
                    com.example.riviansenseapp.context.Location.GARAGE -> "Garage"
                },
                smartActions = smartActions,
                refreshTrigger = refreshTrigger,
                onSmartActionClick = { actionType ->
                    when(actionType) {
                        ActionType.BREATHING -> navController.navigate(Screen.Breathing.route)
                        ActionType.STRETCH -> navController.navigate(Screen.MicroStretch.route)
                        else -> viewModel.executeSmartAction(actionType)
                    }
                },
                onPlaySpotify = { viewModel.playSpotify() },
                onStartBreathing = { navController.navigate(Screen.Breathing.route) },
                onOpenSettings = { navController.navigate(Screen.Settings.route) },
                onOpenTestActions = { navController.navigate(Screen.TestActions.route) },
                onOpenStats = { navController.navigate(Screen.Stats.route) }
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
                onBack = {
                    // Inkrementiraj refreshTrigger da se HomeScreen recompose-uje
                    refreshTrigger++
                    // Osveži smart actions u ViewModelu
                    viewModel.refreshSmartActions()
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
                },
                onSetContext = { mood, location ->
                    if (mood.isNotEmpty()) {
                        viewModel.setMockContext(
                            com.example.riviansenseapp.context.Mood.valueOf(mood),
                            currentContext.location
                        )
                    }
                    if (location.isNotEmpty()) {
                        viewModel.setMockContext(
                            currentContext.mood,
                            com.example.riviansenseapp.context.Location.valueOf(location)
                        )
                    }
                }
            )
        }
        
        composable(Screen.Stats.route) {
            val statsManager = viewModel.getStatsManager()
            if (statsManager != null) {
                StatsScreen(
                    statsManager = statsManager,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
