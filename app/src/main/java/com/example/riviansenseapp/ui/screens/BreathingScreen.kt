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
    EXHALE(4, "Breathe Out", listOf(Color(0xFF8B5CF6), Color(0xFFEC4899)))
}

@Composable
fun BreathingScreen(
    onComplete: () -> Unit = {}
) {
    var phase by remember { mutableStateOf(BreathingPhase.INHALE) }
    var secondsRemaining by remember { mutableStateOf(60) }
    var cycleCount by remember { mutableStateOf(0) }
    
    // Animirani scale koji prati trenutnu fazu
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    val animatedScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 12000 // 3 faze × 4 sekunde
                0.6f at 0 with CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)    // Start mala veličina
                1.8f at 4000 with CubicBezierEasing(0.4f, 0.0f, 0.6f, 1.0f) // INHALE: smooth povećavanje
                1.8f at 8000 with LinearEasing                               // HOLD1: miruje
                0.6f at 12000 with CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f) // EXHALE: smooth smanjivanje
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "scale"
    )
    
    // Timer za praćenje faza (za prikaz teksta)
    LaunchedEffect(Unit) {
        while (true) {
            phase = BreathingPhase.INHALE
            delay(4000)
            phase = BreathingPhase.HOLD1
            delay(4000)
            phase = BreathingPhase.EXHALE
            delay(4000)
            cycleCount++
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
                modifier = Modifier.size(300.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer glow ring
                Box(
                    modifier = Modifier
                        .size(300.dp)
                        .scale(animatedScale)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    phase.colors[0].copy(alpha = 0.4f),
                                    phase.colors[1].copy(alpha = 0.1f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )
                
                // Middle glow
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .scale(animatedScale)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    phase.colors[0].copy(alpha = 0.6f),
                                    phase.colors[1].copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )
                
                // Main circle with gradient
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .scale(animatedScale)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    phase.colors[0],
                                    phase.colors[1]
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = phase.text,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.SemiBold,
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
