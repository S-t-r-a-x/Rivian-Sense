package com.example.drivesenseapp.ui.screens.cards.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.drivesenseapp.ui.screens.cards.ActionCard

@Composable
fun SuggestBreakActionCard(
    stopTypeLabel: String,                        // e.g. "Coffee", "Rest stop", "Scenic viewpoint"
    onSuggestBreak: (stopType: String) -> Unit
) {
    // Pick a short description based on label (optional, can be static too)
    val description = when (stopTypeLabel.lowercase()) {
        "coffee" -> "Suggest a short coffee stop to reset your focus and mood."
        "rest stop" -> "Suggest a quick rest stop to stretch and relax before continuing."
        "scenic viewpoint" -> "Suggest a scenic viewpoint to turn this drive into a small moment of joy."
        else -> "Suggest a short break tailored to your current drive and mood."
    }

    ActionCard(
        icon = Icons.Filled.LocationOn,
        iconTint = Color(0xFFfbbf24), // warm yellow accent
        title = "Suggest a Break",
        description = description,
        actionText = "Find $stopTypeLabel stop",
        onAction = { onSuggestBreak(stopTypeLabel) },
        gradientColors = listOf(
            Color(0xFF020617),                      // near-black
            Color(0xFFFBBF24).copy(alpha = 0.35f)   // yellow glow
        ),
        borderColor = Color(0xFFFBBF24).copy(alpha = 0.7f)
    )
}
