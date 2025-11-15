package com.example.riviansenseapp.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.riviansenseapp.ui.screens.cards.ActionCard
import com.example.riviansenseapp.ui.theme.RivianSenseAppTheme
import java.util.*

/**
 * Pomocna funkcija za proveru da li je feature omogućen u Settings-u
 * Reaktivna - prati promene u SharedPreferences
 */
@Composable
fun isFeatureEnabled(featureName: String, refreshTrigger: Int = 0): Boolean {
    val context = LocalContext.current
    
    // remember sa ključem refreshTrigger - recompose se kada se promeni
    val isEnabled = remember(featureName, refreshTrigger) {
        val prefs = context.getSharedPreferences("rivian_prefs", android.content.Context.MODE_PRIVATE)
        val enabledFeatures = prefs.getStringSet("enabled_features", null)
        
        Log.d("HomeScreen", "🔍 Checking feature: $featureName (refresh=$refreshTrigger)")
        Log.d("HomeScreen", "📋 Enabled features from prefs: $enabledFeatures")
        
        // Ako nisu postavljene preference, sve je omogućeno (default)
        if (enabledFeatures == null) {
            Log.d("HomeScreen", "⚠️ No preferences - allowing $featureName (default)")
            true
        } else {
            val result = enabledFeatures.contains(featureName)
            Log.d("HomeScreen", "${if (result) "✅" else "❌"} Feature $featureName is ${if (result) "ENABLED" else "DISABLED"}")
            result
        }
    }
    
    return isEnabled
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    currentMood: String = "Neutral",
    currentLocation: String = "City",
    smartActions: List<com.example.riviansenseapp.context.SmartAction> = emptyList(),
    refreshTrigger: Int = 0,
    onSmartActionClick: (com.example.riviansenseapp.context.ActionType) -> Unit = {},
    onPlaySpotify: () -> Unit = {},
    onStartBreathing: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenTestActions: () -> Unit = {},
    onOpenStats: () -> Unit = {}
) {
    val timeOfDay = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 0..11 -> "Morning"
        in 12..17 -> "Afternoon"
        else -> "Evening"
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF020617)) // slate-950
            .padding(start = 24.dp, end = 24.dp, top = 48.dp, bottom = 14.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header with Settings
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ready to Drive",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Row {
                // Stats button
                IconButton(
                    onClick = onOpenStats,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = Color(0xFFFFD700) // gold for stats
                    )
                ) {
                    Icon(
                        painter = painterResource(id = com.example.riviansenseapp.R.drawable.badge_foreground),
                        contentDescription = "Statistics & Badges",
                        modifier = Modifier.size(55.dp)
                    )
                }
                
                // Test button (temporary)
                IconButton(
                    onClick = onOpenTestActions,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = Color(0xFFEF4444) // red for testing
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Test Actions",
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                IconButton(
                    onClick = onOpenSettings,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = Color(0xFF94A3B8)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Location & Scenery Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF164E63), // cyan-950
                            Color(0xFF0F172A)  // slate-900
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .border(
                    width = 1.dp,
                    color = Color(0xFF155E75), // cyan-900
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(20.dp)
        ) {
            Column {
                // Scenery
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    val sceneEmoji = when(currentLocation.lowercase()) {
                        "highway" -> "🛣️"
                        "city" -> "🏙️"
                        "forest" -> "🌳"
                        "parking" -> "🅿️"
                        else -> "📍" // default location
                    }
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                Color(0xFF10B981).copy(alpha = 0.1f), // emerald-500 10%
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = sceneEmoji,
                            fontSize = 28.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column {
                        Text(
                            text = "Current Location",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = currentLocation,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }
                
                // Mood
                Row(
                    verticalAlignment = Alignment.Top
                ) {
                    val moodEmoji = when(currentMood.lowercase()) {
                        "nervous" -> "😰"
                        "tired" -> "😴"
                        else -> "😌" // neutral or unknown
                    }
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                Color(0xFF8B5CF6).copy(alpha = 0.1f), // purple-500 10%
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = moodEmoji,
                            fontSize = 28.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column {
                        Text(
                            text = "Detected Mood",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = currentMood,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Smart Actions Section
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = Color(0xFF22D3EE),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Suggested Actions",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }
        
        // Smart action cards - dynamic based on context
        if (smartActions.isEmpty()) {
            Log.d("HomeScreen", "\n🎯 Smart actions are empty - showing FALLBACK actions")
            // Fallback when no smart actions - ✅ FILTRIRAMO NA OSNOVU SETTINGS-A
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Music action - samo ako je omogućeno
                if (isFeatureEnabled("PLAY_CALM_MUSIC", refreshTrigger) || isFeatureEnabled("PLAY_ENERGETIC_MUSIC", refreshTrigger)) {
                    ActionCard(
                        icon = Icons.Default.PlayArrow,
                        iconTint = Color(0xFF34D399),
                        title = "Play Music",
                        description = "Start your playlist",
                        actionText = "Play on Spotify",
                        onAction = onPlaySpotify,
                        gradientColors = listOf(
                            Color(0xFF064E3B),
                            Color(0xFF0F172A)
                        ),
                        borderColor = Color(0xFF14532D)
                    )
                }

                // Breathing exercise - samo ako je omogućeno
                if (isFeatureEnabled("START_BREATHING_EXERCISE", refreshTrigger)) {
                    ActionCard(
                        icon = Icons.Default.FavoriteBorder,
                        iconTint = Color(0xFF60A5FA),
                        title = "Breathing Exercise",
                        description = "60-second box breathing",
                        actionText = "Start Exercise",
                        onAction = onStartBreathing,
                        gradientColors = listOf(
                            Color(0xFF172554),
                            Color(0xFF0F172A)
                        ),
                        borderColor = Color(0xFF1E3A8A)
                    )
                }
            }
        } else {
            Log.d("HomeScreen", "\n✅ Showing ${smartActions.size} SMART ACTIONS:")
            smartActions.forEach { action ->
                Log.d("HomeScreen", "  ✓ ${action.title} (${action.action})")
            }
            // Smart actions from context
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                smartActions.forEach { smartAction ->
                    com.example.riviansenseapp.ui.screens.cards.SmartActionCard(
                        smartAction = smartAction,
                        onClick = { onSmartActionClick(smartAction.action) }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    RivianSenseAppTheme {
        HomeScreen()
    }
}
