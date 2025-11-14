package com.example.riviansenseapp

import android.os.Bundle
import android.util.Log // --- NEW IMPORT ---
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.riviansenseapp.navigation.RivianNavGraph
import com.example.riviansenseapp.ui.theme.RivianSenseAppTheme
import com.example.riviansenseapp.viewmodel.MainViewModel

// --- NEW IMPORTS ---
import org.json.JSONObject
import java.net.URISyntaxException
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    // --- NEW: Define Socket and Server URL ---
    private lateinit var mSocket: Socket

    companion object {
        // Use 10.0.2.2 for the Android Emulator
        private const val SERVER_URL = "http://192.168.0.242:5000"

        // Use your laptop's Wi-Fi IP for a physical phone
        // private const val SERVER_URL = "http://192.168.1.12:5000"
    }

    // --- NEW: Define the Socket event listener ---
    private val onNewState = Emitter.Listener { args ->
        // We are on a background thread, so use runOnUiThread
        runOnUiThread {
            val data = args[0] as JSONObject
            val state: String
            try {
                state = data.getString("state")
            } catch (e: Exception) {
                Log.e("SocketIO", "Error parsing JSON", e)
                return@runOnUiThread // Exit this block
            }

            Log.d("SocketIO", "Received state: $state")

            // *** KEY INTEGRATION POINT ***
            // Instead of logic here, we call a function on your existing ViewModel
            // This keeps your logic centralized in the ViewModel.
            viewModel.handleDriverState(state)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicijalizuj akcije sa kontekstom
        viewModel.initActions(this)

        // --- NEW: Initialize and connect the socket ---
        try {
            mSocket = IO.socket(SERVER_URL)
        } catch (e: URISyntaxException) {
            Log.e("MainActivity", "Socket URI syntax error", e)
            throw RuntimeException(e)
        }

        // Register the listener
        mSocket.on("driver_state", onNewState)

        // Connect to the server
        mSocket.connect()
        // --- END OF NEW CODE ---

        enableEdgeToEdge()
        setContent {
            RivianSenseAppTheme {
                val navController = rememberNavController()

                RivianNavGraph(
                    navController = navController,
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    // --- NEW: Add onDestroy to disconnect the socket ---
    override fun onDestroy() {
        super.onDestroy()
        // Disconnect the socket when the app is closed
        mSocket.disconnect()
        mSocket.off("driver_state", onNewState)
    }
}