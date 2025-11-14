package com.example.riviansenseapp.ui.screens

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.riviansenseapp.ui.screens.cards.ActionCard
import com.example.riviansenseapp.ui.theme.RivianSenseAppTheme
import java.util.*

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    currentMood: String = "Neutral",
    currentLocation: String = "City",
    smartActions: List<com.example.riviansenseapp.context.SmartAction> = emptyList(),
    onSmartActionClick: (com.example.riviansenseapp.context.ActionType) -> Unit = {},
    onPlaySpotify: () -> Unit = {},
    onStartBreathing: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenTestActions: () -> Unit = {}
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
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header with Settings
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = "Good $timeOfDay",
                    fontSize = 14.sp,
                    color = Color(0xFF94A3B8), // slate-400
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = "Ready to Drive",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            Row {
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
        
        Spacer(modifier = Modifier.height(32.dp))
        
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
                // Location
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                Color(0xFF06B6D4).copy(alpha = 0.1f), // cyan-500 10%
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFF22D3EE), // cyan-400
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column {
                        Text(
                            text = "45.5231° N, 122.6765° W",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = "Forest Park Trail, Portland, OR",
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }
                
                // Scenery
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                Color(0xFF10B981).copy(alpha = 0.1f), // emerald-500 10%
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFF34D399), // emerald-400
                            modifier = Modifier.size(24.dp)
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
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                Color(0xFF8B5CF6).copy(alpha = 0.1f), // purple-500 10%
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Face,
                            contentDescription = null,
                            tint = Color(0xFFA78BFA), // purple-400
                            modifier = Modifier.size(24.dp)
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
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }
        
        // Smart action cards - dynamic based on context
        if (smartActions.isEmpty()) {
            // Fallback when no smart actions
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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
        } else {
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
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Old action card for reference (can be removed later)
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(
                text = "Other Actions",
                fontSize = 14.sp,
                color = Color(0xFF64748B),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            ActionCard(
                icon = Icons.Default.Star,
                iconTint = Color(0xFF22D3EE),
                title = "Manual Actions",
                description = "Access all available actions",
                actionText = "Apply Lighting",
                onAction = { /* TODO */ },
                gradientColors = listOf(
                    Color(0xFF164E63), // cyan-950
                    Color(0xFF0F172A)  // slate-900
                ),
                borderColor = Color(0xFF155E75) // cyan-900
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Quick Stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
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
                Column {
                    Text(
                        text = "Battery",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "87%",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            
            Box(
                modifier = Modifier
                    .weight(1f)
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
                Column {
                    Text(
                        text = "Range",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "245 mi",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
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
