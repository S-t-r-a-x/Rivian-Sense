package com.example.riviansenseapp.ui.screens.cards.phoneNotifications

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.riviansenseapp.ui.screens.cards.ActionCard

@Composable
fun DisableDrivingDndActionCard(
    onDisableDrivingDnd: () -> Unit
) {
    ActionCard(
        icon = Icons.Rounded.CheckCircle,
        iconTint = Color(0xFF4ADE80), // green accent
        title = "Driving DND",
        description = "Turn off driving mode and restore your normal notification behavior.",
        actionText = "Disable Driving DND",
        onAction = onDisableDrivingDnd,
        gradientColors = listOf(
            Color(0xFF020617),                     // near-black
            Color(0xFF4ADE80).copy(alpha = 0.35f)  // green glow
        ),
        borderColor = Color(0xFF4ADE80).copy(alpha = 0.7f)
    )
}
