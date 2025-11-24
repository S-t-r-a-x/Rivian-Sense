package com.example.riviansenseapp.ui.screens.cards.phoneNotifications

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.riviansenseapp.ui.screens.cards.ActionCard

@Composable
fun EnableDrivingDndActionCard(
    modeLabel: String,                      // e.g. "Soft", "Max"
    onEnableDrivingDnd: (mode: String) -> Unit
) {
    ActionCard(
        icon = Icons.Filled.Warning,
        iconTint = Color(0xFF38BDF8), // blue accent
        title = "Driving DND ($modeLabel)",
        description = "Reduce non-critical notifications while you're driving to keep you focused and less stressed.",
        actionText = "Enable $modeLabel mode",
        onAction = { onEnableDrivingDnd(modeLabel.lowercase()) },
        gradientColors = listOf(
            Color(0xFF020617),                     // near-black
            Color(0xFF38BDF8).copy(alpha = 0.35f)  // blue glow
        ),
        borderColor = Color(0xFF38BDF8).copy(alpha = 0.7f)
    )
}
