package com.example.riviansenseapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.riviansenseapp.ui.screens.*
import com.example.riviansenseapp.ui.theme.RivianSenseAppTheme
import com.example.riviansenseapp.viewmodel.MainViewModel

enum class Screen {
    SPLASH, HOME, BREATHING, SETTINGS
}

class MainActivity : ComponentActivity() {
    
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inicijalizuj akcije sa kontekstom
        viewModel.initActions(this)
        
        enableEdgeToEdge()
        setContent {
            RivianSenseAppTheme {
                var currentScreen by remember { mutableStateOf(Screen.SPLASH) }
                var features by remember { mutableStateOf(getDefaultFeatures()) }
                
                when (currentScreen) {
                    Screen.SPLASH -> {
                        SplashScreen(
                            onTimeout = { currentScreen = Screen.HOME }
                        )
                    }
                    
                    Screen.HOME -> {
                        HomeScreen(
                            modifier = Modifier.fillMaxSize(),
                            onPlaySpotify = { viewModel.playSpotify() },
                            onStartBreathing = { currentScreen = Screen.BREATHING },
                            onOpenSettings = { currentScreen = Screen.SETTINGS }
                        )
                    }
                    
                    Screen.BREATHING -> {
                        BreathingScreen(
                            onComplete = { currentScreen = Screen.HOME }
                        )
                    }
                    
                    Screen.SETTINGS -> {
                        SettingsScreen(
                            modifier = Modifier.fillMaxSize(),
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
                            onBack = { currentScreen = Screen.HOME }
                        )
                    }
                }
            }
        }
    }
}