package com.example.riviansenseapp.ui.screens.cards.audio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.riviansenseapp.ui.screens.cards.ActionCard

@Composable
fun PlayPodcastOrAudiobookActionCard(
    onPlayPodcastOrAudiobook: () -> Unit
) {
    ActionCard(
        icon = Icons.Rounded.PlayArrow,
        iconTint = Color(0xFF38BDF8), // sky blue accent
        title = "Traffic Companion",
        description = "When you're stuck in traffic, switch to a podcast or audiobook to make the time feel shorter.",
        actionText = "Play podcast / audiobook",
        onAction = onPlayPodcastOrAudiobook,
        gradientColors = listOf(
            Color(0xFF020617),                     // near-black
            Color(0xFF38BDF8).copy(alpha = 0.35f)  // blue glow
        ),
        borderColor = Color(0xFF38BDF8).copy(alpha = 0.7f)
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF020617)
@Composable
private fun PlayPodcastOrAudiobookActionCardPreview() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF020617),
                            Color(0xFF0F172A)
                        )
                    )
                )
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            PlayPodcastOrAudiobookActionCard(
                onPlayPodcastOrAudiobook = {
                    // preview: no-op
                }
            )
        }
    }
}
