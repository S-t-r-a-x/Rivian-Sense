package com.example.riviansenseapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.riviansenseapp.ui.theme.RivianSenseAppTheme

data class Feature(
    val id: String,
    val name: String,
    val description: String,
    val action: String,
    val enabled: Boolean
)

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    features: List<Feature> = getDefaultFeatures(),
    onToggleFeature: (String) -> Unit = {},
    onBack: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF020617)) // slate-950
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Back button
        TextButton(
            onClick = onBack,
            colors = ButtonDefaults.textButtonColors(
                contentColor = Color(0xFF94A3B8)
            ),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Back")
        }
        
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                tint = Color(0xFF22D3EE), // cyan-400
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Settings",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        
        Text(
            text = "Customize your Rivian Sense experience",
            fontSize = 14.sp,
            color = Color(0xFF94A3B8),
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // Features List
        Text(
            text = "Available Features",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            features.forEach { feature ->
                FeatureCard(
                    feature = feature,
                    onToggle = { onToggleFeature(feature.id) }
                )
            }
        }
        
        // App Info
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF164E63).copy(alpha = 0.3f), // cyan-950
                            Color(0xFF0F172A).copy(alpha = 0.3f)  // slate-900
                        )
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                .border(
                    1.dp,
                    Color(0xFF155E75).copy(alpha = 0.2f),
                    RoundedCornerShape(12.dp)
                )
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "App Version",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "Rivian Sense v1.0.0",
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun FeatureCard(
    feature: Feature,
    onToggle: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color(0xFF0F172A).copy(alpha = 0.5f),
                RoundedCornerShape(12.dp)
            )
            .border(
                1.dp,
                Color(0xFF1E293B),
                RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Checkbox(
                checked = feature.enabled,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFF06B6D4), // cyan-500
                    uncheckedColor = Color(0xFF475569)
                ),
                modifier = Modifier.padding(top = 4.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = feature.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                
                Text(
                    text = feature.description,
                    fontSize = 14.sp,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
//                    Icon(
//                        imageVector = Icons.Default.Code,
//                        contentDescription = null,
//                        tint = Color(0xFF22D3EE),
//                        modifier = Modifier.size(12.dp)
//                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = feature.action,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF22D3EE),
                        modifier = Modifier
                            .background(
                                Color(0xFF164E63).copy(alpha = 0.3f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

fun getDefaultFeatures() = listOf(
    Feature(
        id = "breathing",
        name = "Breathing Exercise",
        description = "Full-screen box breathing before driving",
        action = "startBreathingExercise(60)",
        enabled = true
    ),
    Feature(
        id = "lighting",
        name = "Cabin Lighting",
        description = "Adjust cabin lighting based on mood",
        action = "setCabinLighting(\"soft_blue\")",
        enabled = true
    ),
    Feature(
        id = "playlist",
        name = "Chill Playlist",
        description = "Auto-suggest Spotify playlists",
        action = "playChillPlaylist()",
        enabled = true
    ),
    Feature(
        id = "climate",
        name = "Smart Climate",
        description = "Auto-adjust temperature based on setting",
        action = "adjustClimate()",
        enabled = false
    ),
    Feature(
        id = "scenic",
        name = "Scenic Routes",
        description = "Suggest scenic routes in nature",
        action = "suggestScenicRoute()",
        enabled = true
    )
)

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    RivianSenseAppTheme {
        SettingsScreen()
    }
}
