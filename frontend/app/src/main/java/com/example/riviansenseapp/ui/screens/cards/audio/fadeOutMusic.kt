package com.example.riviansenseapp.ui.screens.cards.audio

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.riviansenseapp.ui.screens.cards.ActionCard

@Composable
fun FadeOutMusicActionCard(
    durationSeconds: Int,
    onFadeOutMusic: (durationMs: Int) -> Unit
) {
    val durationMs = durationSeconds * 1000
    val label = "${durationSeconds}s"

    ActionCard(
        icon = Icons.Rounded.PlayArrow,
        iconTint = Color(0xFFFB923C), // warm orange accent
        title = "Fade Out Music",
        description = "Gently lower the music over $label to make the cabin feel calmer.",
        actionText = "Fade out over $label",
        onAction = { onFadeOutMusic(durationMs) },
        gradientColors = listOf(
            Color(0xFF020617),                     // near-black
            Color(0xFFFB923C).copy(alpha = 0.35f)  // orange glow
        ),
        borderColor = Color(0xFFFB923C).copy(alpha = 0.7f)
    )
}
