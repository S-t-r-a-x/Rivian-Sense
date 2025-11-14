package com.example.riviansenseapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.riviansenseapp.ui.theme.RivianSenseAppTheme

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onPlaySpotify: () -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Rivian Sense",
            fontSize = 32.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        Text(
            text = "Smart In-Car Assistant",
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 48.dp)
        )
        
        Button(
            onClick = { onPlaySpotify() },
            modifier = Modifier
                .width(250.dp)
                .height(70.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1DB954) // Spotify zelena boja
            )
        ) {
            Text(
                text = "▶ Pusti Spotify Playlist",
                fontSize = 18.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    RivianSenseAppTheme {
        HomeScreen()
    }
}
