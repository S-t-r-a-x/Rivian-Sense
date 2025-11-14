package com.example.riviansenseapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.riviansenseapp.ui.screens.HomeScreen
import com.example.riviansenseapp.ui.theme.RivianSenseAppTheme
import com.example.riviansenseapp.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inicijalizuj akcije sa kontekstom
        viewModel.initActions(this)
        
        enableEdgeToEdge()
        setContent {
            RivianSenseAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    HomeScreen(
                        modifier = Modifier.padding(innerPadding),
                        onPlaySpotify = { viewModel.playSpotify() }
                    )
                }
            }
        }
    }
}