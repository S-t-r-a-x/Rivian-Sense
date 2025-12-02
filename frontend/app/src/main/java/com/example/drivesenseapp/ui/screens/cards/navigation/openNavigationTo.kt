package com.example.drivesenseapp.ui.screens.cards.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.drivesenseapp.ui.screens.cards.ActionCard

@Composable
fun OpenNavigationToActionCard(
    destinationLabel: String,                        // e.g. "Home", "Work", "Next event"
    onOpenNavigationTo: (destination: String) -> Unit
) {
    ActionCard(
        icon = Icons.Filled.LocationOn,
        iconTint = Color(0xFF60A5FA), // blue accent
        title = "Smart Navigation",
        description = "Open navigation to $destinationLabel with one tap, adapted to your current drive and mood.",
        actionText = "Navigate to $destinationLabel",
        onAction = { onOpenNavigationTo(destinationLabel) },
        gradientColors = listOf(
            Color(0xFF020617),                     // near-black
            Color(0xFF60A5FA).copy(alpha = 0.35f)  // blue glow
        ),
        borderColor = Color(0xFF60A5FA).copy(alpha = 0.7f)
    )
}
