package com.example.riviansenseapp.ui.screens.cards.audio

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Place
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.riviansenseapp.ui.screens.cards.ActionCard

@Composable
fun PlayNatureSoundscapeActionCard(
    soundscapeLabel: String,                    // e.g. "Forest", "Rain", "Ocean"
    onPlayNatureSoundscape: (String) -> Unit    // receives the label so you can map to a type
) {
    ActionCard(
        icon = Icons.Rounded.Place,
        iconTint = Color(0xFF22C55E), // green-ish for nature
        title = "Nature Soundscape",
        description = "Play a calm ${soundscapeLabel.lowercase()} soundscape to match the current drive and mood.",
        actionText = "Play $soundscapeLabel sounds",
        onAction = { onPlayNatureSoundscape(soundscapeLabel) },
        gradientColors = listOf(
            Color(0xFF020617),                     // near-black
            Color(0xFF16A34A).copy(alpha = 0.35f)  // green glow
        ),
        borderColor = Color(0xFF16A34A).copy(alpha = 0.7f)
    )
}
