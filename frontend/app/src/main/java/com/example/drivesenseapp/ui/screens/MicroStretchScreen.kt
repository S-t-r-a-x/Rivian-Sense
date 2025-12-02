package com.example.drivesenseapp.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.drivesenseapp.ui.theme.DriveSenseAppTheme
import kotlinx.coroutines.delay

@Composable
fun MicroStretchScreen(
    onComplete: () -> Unit = {}
) {
    val exercises = listOf(
        StretchExercise("Neck Roll", "Roll your neck gently in circles", 10),
        StretchExercise("Shoulder Shrug", "Lift shoulders up and release", 10),
        StretchExercise("Wrist Circles", "Rotate wrists in both directions", 10),
        StretchExercise("Back Stretch", "Lean back and stretch your spine", 10),
        StretchExercise("Deep Breath", "Take 3 deep breaths", 10)
    )
    
    var currentExerciseIndex by remember { mutableStateOf(0) }
    var secondsRemaining by remember { mutableStateOf(exercises[0].durationSec) }
    
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    // Countdown timer
    LaunchedEffect(currentExerciseIndex) {
        secondsRemaining = exercises[currentExerciseIndex].durationSec
        while (secondsRemaining > 0) {
            delay(1000)
            secondsRemaining--
        }
        
        if (currentExerciseIndex < exercises.size - 1) {
            currentExerciseIndex++
        } else {
            onComplete()
        }
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
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            // Progress indicator
            Text(
                text = "Exercise ${currentExerciseIndex + 1} of ${exercises.size}",
                fontSize = 14.sp,
                color = Color(0xFF94A3B8),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            LinearProgressIndicator(
                progress = (currentExerciseIndex + 1).toFloat() / exercises.size,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = Color(0xFF22D3EE),
                trackColor = Color(0xFF1E293B)
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Exercise card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(scale),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF164E63).copy(alpha = 0.5f)
                )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Text(
                        text = exercises[currentExerciseIndex].name,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Text(
                        text = exercises[currentExerciseIndex].description,
                        fontSize = 16.sp,
                        color = Color(0xFF94A3B8),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                    
                    // Timer circle
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .background(
                                Color(0xFF22D3EE).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(60.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$secondsRemaining",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF22D3EE)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Skip button
            TextButton(
                onClick = {
                    if (currentExerciseIndex < exercises.size - 1) {
                        currentExerciseIndex++
                    } else {
                        onComplete()
                    }
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(0xFF94A3B8)
                )
            ) {
                Text("Skip →", fontSize = 16.sp)
            }
        }
    }
}

data class StretchExercise(
    val name: String,
    val description: String,
    val durationSec: Int
)

@Preview(showBackground = true)
@Composable
fun MicroStretchScreenPreview() {
    DriveSenseAppTheme {
        MicroStretchScreen()
    }
}
