package com.example.riviansenseapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.riviansenseapp.actions.*
import com.example.riviansenseapp.ui.theme.RivianSenseAppTheme

@Composable
fun TestActionsScreen(
    onBack: () -> Unit = {},
    onPlaySpotify: () -> Unit = {},
    onPlayPodcast: () -> Unit = {},
    onFadeOutMusic: () -> Unit = {},
    onSetVolume: (Int) -> Unit = {},
    onEnableDND: (PhoneAction.DNDMode) -> Unit = {},
    onDisableDND: () -> Unit = {},
    onAutoReply: () -> Unit = {},
    onNavigateHome: () -> Unit = {},
    onNavigateWork: () -> Unit = {},
    onSuggestBreak: (NavigationAction.StopType) -> Unit = {},
    onStartBreathing: () -> Unit = {},
    onStartStretch: () -> Unit = {},
    onCreateReminder: () -> Unit = {},
    onPrintReminders: () -> Unit = {},
    onClearAllReminders: () -> Unit = {},
    onPlayChime: () -> Unit = {},
    onShowStreak: () -> Unit = {},
    onSetContext: (String, String) -> Unit = { _, _ -> }
) {
    var selectedVolume by remember { mutableStateOf(50) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF020617))
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Test Actions",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        
        // Spotify Actions
        SectionTitle("🎵 Spotify Actions")
        TestButton("Play Playlist", onClick = onPlaySpotify)
        TestButton("Play Podcast", onClick = onPlayPodcast)
        TestButton("Fade Out Music", onClick = onFadeOutMusic)
        
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Text(
                text = "Set Volume: $selectedVolume%",
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Slider(
                value = selectedVolume.toFloat(),
                onValueChange = { selectedVolume = it.toInt() },
                valueRange = 0f..100f,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF1DB954),
                    activeTrackColor = Color(0xFF1DB954)
                )
            )
            TestButton("Apply Volume", onClick = { onSetVolume(selectedVolume) })
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Context Control
        SectionTitle("🎯 Context Control (Mock API)")
        Text(
            text = "Mood:",
            color = Color(0xFF94A3B8),
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SmallButton("Nervous", modifier = Modifier.weight(1f)) {
                onSetContext("NERVOUS", "")
            }
            SmallButton("Tired", modifier = Modifier.weight(1f)) {
                onSetContext("TIRED", "")
            }
            SmallButton("Neutral", modifier = Modifier.weight(1f)) {
                onSetContext("NEUTRAL", "")
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Location:",
            color = Color(0xFF94A3B8),
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SmallButton("Forest", modifier = Modifier.weight(1f)) {
                onSetContext("", "FOREST")
            }
            SmallButton("City", modifier = Modifier.weight(1f)) {
                onSetContext("", "CITY")
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SmallButton("Highway", modifier = Modifier.weight(1f)) {
                onSetContext("", "HIGHWAY")
            }
            SmallButton("Garage", modifier = Modifier.weight(1f)) {
                onSetContext("", "GARAGE")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Phone Actions
        SectionTitle("📱 Phone Actions")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SmallButton("DND Soft", modifier = Modifier.weight(1f)) {
                onEnableDND(PhoneAction.DNDMode.SOFT)
            }
            SmallButton("DND Max", modifier = Modifier.weight(1f)) {
                onEnableDND(PhoneAction.DNDMode.MAX)
            }
        }
        TestButton("Disable DND", onClick = onDisableDND)
        TestButton("Enable Auto-Reply SMS", onClick = onAutoReply)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Navigation Actions
        SectionTitle("🗺️ Navigation")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SmallButton("Navigate Home", modifier = Modifier.weight(1f), onClick = onNavigateHome)
            SmallButton("Navigate Work", modifier = Modifier.weight(1f), onClick = onNavigateWork)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SmallButton("Coffee", modifier = Modifier.weight(1f)) {
                onSuggestBreak(NavigationAction.StopType.COFFEE)
            }
            SmallButton("Rest Stop", modifier = Modifier.weight(1f)) {
                onSuggestBreak(NavigationAction.StopType.REST_STOP)
            }
        }
        SmallButton("Scenic View", modifier = Modifier.fillMaxWidth()) {
            onSuggestBreak(NavigationAction.StopType.SCENIC_VIEWPOINT)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Wellbeing Actions
        SectionTitle("🧘 Wellbeing")
        TestButton("Start Breathing Exercise", onClick = onStartBreathing)
        TestButton("Start Micro Stretch", onClick = onStartStretch)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Logging Actions
        SectionTitle("📊 Logging & Streaks")
        TestButton("Create Reminder: 'Jovan te zvao'", onClick = onCreateReminder)
        TestButton("📋 Print All Reminders (Logcat)", onClick = onPrintReminders)
        TestButton("🗑️ Clear All Reminders", onClick = onClearAllReminders)
        TestButton("Play Chime (Aggressive)", onClick = onPlayChime)
        TestButton("Show Streak Card", onClick = onShowStreak)
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF22D3EE),
        modifier = Modifier.padding(vertical = 12.dp)
    )
}

@Composable
fun TestButton(
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF1E293B)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 14.sp
        )
    }
}

@Composable
fun SmallButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.padding(vertical = 4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF334155)
        ),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(8.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 12.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TestActionsScreenPreview() {
    RivianSenseAppTheme {
        TestActionsScreen()
    }
}
