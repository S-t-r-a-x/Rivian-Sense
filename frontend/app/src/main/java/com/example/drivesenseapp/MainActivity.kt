package com.example.drivesenseapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.drivesenseapp.navigation.DriveNavGraph
import com.example.drivesenseapp.ui.theme.DriveSenseAppTheme
import com.example.drivesenseapp.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicijalizuj akcije sa kontekstom
        // ViewModel automatski pokreće WebSocket listener u initActions()
        viewModel.initActions(this)

        enableEdgeToEdge()
        setContent {
            DriveSenseAppTheme {
                val navController = rememberNavController()

                DriveNavGraph(
                    navController = navController,
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}