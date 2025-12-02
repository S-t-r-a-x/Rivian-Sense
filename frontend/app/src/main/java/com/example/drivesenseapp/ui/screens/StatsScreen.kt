package com.example.drivesenseapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.drivesenseapp.context.Location
import com.example.drivesenseapp.context.Mood
import com.example.drivesenseapp.stats.*

@Composable
fun StatsScreen(
    statsManager: StatsManager,
    onNavigateBack: () -> Unit
) {
    val stats = remember { statsManager.getStats() }
    val allBadges = remember { statsManager.getAllBadgesWithStatus() }
    
    var selectedTab by remember { mutableStateOf(0) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF020617)) // slate-950
    ) {
        // Header with back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 24.dp, top = 48.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = Color(0xFF94A3B8)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = "Statistics & Badges",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        
        // Custom tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Stats tab
            Button(
                onClick = { selectedTab = 0 },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedTab == 0) Color(0xFF06B6D4) else Color(0xFF1E293B),
                    contentColor = Color.White
                ),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("📊 Stats")
            }
            
            // Badges tab
            Button(
                onClick = { selectedTab = 1 },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedTab == 1) Color(0xFFFFD700) else Color(0xFF1E293B),
                    contentColor = if (selectedTab == 1) Color(0xFF020617) else Color.White
                ),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("🏆 Badges")
            }
        }
        
        // Content
        when (selectedTab) {
            0 -> StatsContent(stats)
            1 -> BadgesContent(allBadges, statsManager)
        }
    }
}

@Composable
fun StatsContent(stats: DriveStats) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Opšta statistika
        item {
            StatsCard(
                title = "General Statistics",
                icon = "📈"
            ) {
                StatRow("Total Drives", "${stats.totalDrives}")
                StatRow("Total Time", stats.formatTime(stats.totalDriveTime))
                StatRow(
                    "Average Drive", 
                    if (stats.totalDrives > 0) 
                        stats.formatTime(stats.totalDriveTime / stats.totalDrives) 
                    else "N/A"
                )
            }
        }
        
        // Mood statistika
        item {
            StatsCard(
                title = "Mood",
                icon = "😊"
            ) {
                MoodStatRow(
                    mood = Mood.NEUTRAL,
                    time = stats.neutralTime,
                    percentage = stats.getMoodPercentage(Mood.NEUTRAL),
                    stats = stats,
                    emoji = "😌"
                )
                MoodStatRow(
                    mood = Mood.NERVOUS,
                    time = stats.nervousTime,
                    percentage = stats.getMoodPercentage(Mood.NERVOUS),
                    stats = stats,
                    emoji = "😰"
                )
                MoodStatRow(
                    mood = Mood.TIRED,
                    time = stats.tiredTime,
                    percentage = stats.getMoodPercentage(Mood.TIRED),
                    stats = stats,
                    emoji = "😴"
                )
            }
        }
        
        // Location statistika
        item {
            StatsCard(
                title = "Locations",
                icon = "🗺️"
            ) {
                LocationStatRow(
                    location = Location.CITY,
                    time = stats.cityTime,
                    percentage = stats.getLocationPercentage(Location.CITY),
                    stats = stats,
                    emoji = "🌆"
                )
                LocationStatRow(
                    location = Location.HIGHWAY,
                    time = stats.highwayTime,
                    percentage = stats.getLocationPercentage(Location.HIGHWAY),
                    stats = stats,
                    emoji = "🛣️"
                )
                LocationStatRow(
                    location = Location.FOREST,
                    time = stats.forestTime,
                    percentage = stats.getLocationPercentage(Location.FOREST),
                    stats = stats,
                    emoji = "🌲"
                )
                LocationStatRow(
                    location = Location.GARAGE,
                    time = stats.garageTime,
                    percentage = stats.getLocationPercentage(Location.GARAGE),
                    stats = stats,
                    emoji = "🏠"
                )
            }
        }
    }
}

@Composable
fun BadgesContent(badges: List<Badge>, statsManager: StatsManager) {
    val unlockedBadges = badges.filter { it.isUnlocked }
    val lockedBadges = badges.filter { !it.isUnlocked }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        item {
            BadgesSummaryCard(
                total = badges.size,
                unlocked = unlockedBadges.size
            )
        }
        
        // Otključani bedževi
        if (unlockedBadges.isNotEmpty()) {
            item {
                Text(
                    text = "🏆 Unlocked (${unlockedBadges.size})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            items(unlockedBadges) { badge ->
                BadgeCard(
                    badge = badge,
                    progress = 100f,
                    isUnlocked = true
                )
            }
        }
        
        // Zaključani bedževi
        if (lockedBadges.isNotEmpty()) {
            item {
                Text(
                    text = "🔒 Locked (${lockedBadges.size})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            items(lockedBadges) { badge ->
                BadgeCard(
                    badge = badge,
                    progress = statsManager.getBadgeProgress(badge),
                    isUnlocked = false
                )
            }
        }
    }
}

@Composable
fun StatsCard(
    title: String,
    icon: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFF0F172A).copy(alpha = 0.6f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(20.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(
                    text = icon,
                    fontSize = 28.sp,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            content()
        }
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 15.sp,
            color = Color(0xFF94A3B8)
        )
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun MoodStatRow(
    mood: Mood,
    time: Long,
    percentage: Float,
    stats: DriveStats,
    emoji: String
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = emoji, fontSize = 24.sp, modifier = Modifier.padding(end = 8.dp))
                Text(
                    text = mood.name,
                    fontSize = 15.sp,
                    color = Color.White
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stats.formatTime(time),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = String.format("%.1f%%", percentage),
                    fontSize = 13.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }
        
        // Progress bar
        LinearProgressIndicator(
            progress = { percentage / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .height(6.dp),
            color = when (mood) {
                Mood.NEUTRAL -> Color(0xFF4CAF50)
                Mood.NERVOUS -> Color(0xFFFF9800)
                Mood.TIRED -> Color(0xFF2196F3)
            },
        )
    }
}

@Composable
fun LocationStatRow(
    location: Location,
    time: Long,
    percentage: Float,
    stats: DriveStats,
    emoji: String
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = emoji, fontSize = 24.sp, modifier = Modifier.padding(end = 8.dp))
                Text(
                    text = location.name,
                    fontSize = 15.sp,
                    color = Color.White
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stats.formatTime(time),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = String.format("%.1f%%", percentage),
                    fontSize = 13.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }
        
        // Progress bar
        LinearProgressIndicator(
            progress = { percentage / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .height(6.dp),
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
fun BadgesSummaryCard(total: Int, unlocked: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFF0F172A).copy(alpha = 0.6f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "🏆",
                    fontSize = 40.sp
                )
                Text(
                    text = "$unlocked",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Unlocked",
                    fontSize = 14.sp,
                    color = Color(0xFF94A3B8)
                )
            }
            
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(60.dp)
                    .background(Color(0xFF475569))
            )
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "🔒",
                    fontSize = 40.sp
                )
                Text(
                    text = "${total - unlocked}",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Remaining",
                    fontSize = 14.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }
}

@Composable
fun BadgeCard(
    badge: Badge,
    progress: Float,
    isUnlocked: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (isUnlocked) 1f else 0.6f)
            .background(
                color = if (isUnlocked) {
                    when (badge.tier) {
                        BadgeTier.BRONZE -> Color(0xFFCD7F32).copy(alpha = 0.15f)
                        BadgeTier.SILVER -> Color(0xFFC0C0C0).copy(alpha = 0.15f)
                        BadgeTier.GOLD -> Color(0xFFFFD700).copy(alpha = 0.15f)
                        BadgeTier.PLATINUM -> Color(0xFFE5E4E2).copy(alpha = 0.15f)
                    }
                } else {
                    Color(0xFF0F172A).copy(alpha = 0.6f)
                },
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badge.icon,
                    fontSize = 32.sp
                )
            }
            
            // Info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = badge.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = badge.description,
                    fontSize = 14.sp,
                    color = Color(0xFF94A3B8)
                )
                
                if (!isUnlocked && progress > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress / 100f },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        text = "${progress.toInt()}%",
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            
            // Tier badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = when (badge.tier) {
                    BadgeTier.BRONZE -> Color(0xFFCD7F32)
                    BadgeTier.SILVER -> Color(0xFFC0C0C0)
                    BadgeTier.GOLD -> Color(0xFFFFD700)
                    BadgeTier.PLATINUM -> Color(0xFFE5E4E2)
                }
            ) {
                Text(
                    text = badge.tier.name.take(1),
                    modifier = Modifier.padding(8.dp),
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}
