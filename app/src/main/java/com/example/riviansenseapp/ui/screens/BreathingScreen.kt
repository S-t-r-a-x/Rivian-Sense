package com.example.riviansenseapp.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.riviansenseapp.ui.theme.RivianSenseAppTheme
import kotlinx.coroutines.delay

enum class BreathingPhase(val duration: Int, val text: String, val colors: List<Color>) {
    INHALE(4, "Breathe In", listOf(Color(0xFF06B6D4), Color(0xFF3B82F6))),
    HOLD1(4, "Hold", listOf(Color(0xFF3B82F6), Color(0xFF8B5CF6))),
    EXHALE(4, "Breathe Out", listOf(Color(0xFF8B5CF6), Color(0xFFEC4899))),
    HOLD2(4, "Hold", listOf(Color(0xFFEC4899), Color(0xFF06B6D4)))
}

@Composable
fun BreathingScreen(
    onComplete: () -> Unit = {}
) {
    var phase by remember { mutableStateOf(BreathingPhase.INHALE) }
    var secondsRemaining by remember { mutableStateOf(60) }
    var cycleCount by remember { mutableStateOf(0) }
    
    val targetScale = when (phase) {
        BreathingPhase.INHALE -> 1.5f
        BreathingPhase.EXHALE -> 0.75f
        else -> 1.1f
    }
    
    val animatedScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = tween(
            durationMillis = phase.duration * 1000,
            easing = LinearEasing
        ),
        label = "scale"
    )
    
    // Timer for phase changes
    LaunchedEffect(phase) {
        delay(phase.duration * 1000L)
        phase = when (phase) {
            BreathingPhase.INHALE -> BreathingPhase.HOLD1
            BreathingPhase.HOLD1 -> BreathingPhase.EXHALE
            BreathingPhase.EXHALE -> BreathingPhase.HOLD2
            BreathingPhase.HOLD2 -> {
                cycleCount++
                BreathingPhase.INHALE
            }
        }
    }
    
    // Countdown timer
    LaunchedEffect(Unit) {
        while (secondsRemaining > 0) {
            delay(1000)
            secondsRemaining--
        }
        onComplete()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF020617)), // slate-950
        contentAlignment = Alignment.Center
    ) {
        // Close button
        IconButton(
            onClick = onComplete,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(24.dp)
            )
        }
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Breathing Circle
            Box(
                modifier = Modifier.size(256.dp),
                contentAlignment = Alignment.Center
            ) {
                // Blurred background circle
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(animatedScale)
                        .background(
                            brush = Brush.linearGradient(phase.colors),
                            shape = CircleShape
                        )
                        .blur(64.dp)
                )
                
                // Main circle
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(animatedScale)
                        .background(
                            brush = Brush.linearGradient(phase.colors),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = phase.text,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Cycle ${cycleCount + 1}",
                    fontSize = 14.sp,
                    color = Color(0xFF94A3B8) // slate-400
                )
                Text(
                    text = "${secondsRemaining}s remaining",
                    fontSize = 18.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BreathingScreenPreview() {
    RivianSenseAppTheme {
        BreathingScreen()
    }
}
