package com.example.riviansenseapp.ui.screens.cards.wellbeing

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.riviansenseapp.ui.screens.cards.ActionCard

@Composable
fun ShowMicroStretchPromptActionCard(
    onShowMicroStretchPrompt: () -> Unit
) {
    ActionCard(
        icon = Icons.Filled.Face,
        iconTint = Color(0xFFa855f7), // purple accent
        title = "Micro Stretch Break",
        description = "Show a quick stretch prompt when you're parked to release tension from a long or stressful drive.",
        actionText = "Show stretch prompt",
        onAction = onShowMicroStretchPrompt,
        gradientColors = listOf(
            Color(0xFF020617),                      // near-black
            Color(0xFFA855F7).copy(alpha = 0.35f)   // purple glow
        ),
        borderColor = Color(0xFFA855F7).copy(alpha = 0.7f)
    )
}
