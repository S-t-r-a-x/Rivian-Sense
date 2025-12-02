package com.example.drivesenseapp.ui.screens.cards.wellbeing

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.drivesenseapp.ui.screens.cards.ActionCard

@Composable
fun StartBreathingExerciseActionCard(
    durationSeconds: Int,
    onStartBreathingExercise: (durationSec: Int) -> Unit
) {
    val label = "${durationSeconds}s"

    ActionCard(
        icon = Icons.Filled.Person,
        iconTint = Color(0xFF38BDF8), // calm blue accent
        title = "Breathing Exercise",
        description = "Take a guided $label breathing break to reset before or during a stressful drive.",
        actionText = "Start $label exercise",
        onAction = { onStartBreathingExercise(durationSeconds) },
        gradientColors = listOf(
            Color(0xFF020617),                     // near-black
            Color(0xFF38BDF8).copy(alpha = 0.35f)  // blue glow
        ),
        borderColor = Color(0xFF38BDF8).copy(alpha = 0.7f)
    )
}
