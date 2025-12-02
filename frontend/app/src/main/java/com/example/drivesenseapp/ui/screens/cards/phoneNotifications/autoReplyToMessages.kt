package com.example.drivesenseapp.ui.screens.cards.phoneNotifications

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.drivesenseapp.ui.screens.cards.ActionCard

@Composable
fun AutoReplyToMessagesActionCard(
    templateLabel: String,                           // e.g. "I'm driving, I'll reply soon"
    onAutoReplyToMessages: (template: String) -> Unit
) {
    ActionCard(
        icon = Icons.Filled.Email,
        iconTint = Color(0xFFF97316), // orange accent
        title = "Smart Auto-Reply",
        description = "Use a quick auto-reply while you're driving so people know you'll respond after the trip.",
        actionText = "Use auto-reply",
        onAction = { onAutoReplyToMessages(templateLabel) },
        gradientColors = listOf(
            Color(0xFF020617),                     // near-black
            Color(0xFFF97316).copy(alpha = 0.35f)  // orange glow
        ),
        borderColor = Color(0xFFF97316).copy(alpha = 0.7f)
    )
}
