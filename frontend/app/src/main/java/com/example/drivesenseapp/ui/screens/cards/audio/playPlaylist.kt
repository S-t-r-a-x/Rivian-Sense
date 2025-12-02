package com.example.drivesenseapp.ui.screens.cards.audio

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.drivesenseapp.ui.screens.cards.ActionCard

@Composable
fun PlayPlaylistActionCard(
    onPlayPlaylist: () -> Unit
) {
    ActionCard(
        icon = Icons.Default.PlayArrow,
        iconTint = Color(0xFF22C55E), // green accent
        title = "Smart Playlist",
        description = "Automatically picks a playlist that matches your current drive and mood.",
        actionText = "Play playlist",
        onAction = onPlayPlaylist,
        gradientColors = listOf(
            Color(0xFF020617),                    // near-black
            Color(0xFF22C55E).copy(alpha = 0.35f) // green glow
        ),
        borderColor = Color(0xFF22C55E).copy(alpha = 0.7f)
    )
}
