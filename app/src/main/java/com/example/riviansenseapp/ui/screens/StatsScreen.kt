package com.example.riviansenseapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.riviansenseapp.context.Location
import com.example.riviansenseapp.context.Mood
import com.example.riviansenseapp.stats.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    statsManager: StatsManager,
    onNavigateBack: () -> Unit
) {
    val stats = remember { statsManager.getStats() }
    val allBadges = remember { statsManager.getAllBadgesWithStatus() }
    
    var selectedTab by remember { mutableStateOf(0) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistika & Bedževi") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Star, "Nazad")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tabs
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("📊 Statistika") },
                    icon = { Icon(Icons.Default.DateRange, null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("🏆 Bedževi") },
                    icon = { Icon(Icons.Default.Star, null) }
                )
            }
            
            // Content
            when (selectedTab) {
                0 -> StatsContent(stats)
                1 -> BadgesContent(allBadges, statsManager)
            }
        }
    }
}

@Composable
fun StatsContent(stats: DriveStats) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
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
        contentPadding = PaddingValues(16.dp),
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
                    text = "🏆 Otključano (${unlockedBadges.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
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
                    text = "🔒 Zaključano (${lockedBadges.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Text(
                    text = icon,
                    fontSize = 24.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
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
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
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
                Text(text = emoji, fontSize = 20.sp, modifier = Modifier.padding(end = 8.dp))
                Text(text = mood.name, style = MaterialTheme.typography.bodyMedium)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stats.formatTime(time),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = String.format("%.1f%%", percentage),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
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
                Text(text = emoji, fontSize = 20.sp, modifier = Modifier.padding(end = 8.dp))
                Text(text = location.name, style = MaterialTheme.typography.bodyMedium)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stats.formatTime(time),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = String.format("%.1f%%", percentage),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "🏆",
                    fontSize = 32.sp
                )
                Text(
                    text = "$unlocked",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Otključano",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Divider(
                modifier = Modifier
                    .width(2.dp)
                    .height(60.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
            )
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "🔒",
                    fontSize = 32.sp
                )
                Text(
                    text = "${total - unlocked}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Preostalo",
                    style = MaterialTheme.typography.bodySmall
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (isUnlocked) 1f else 0.6f),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) {
                when (badge.tier) {
                    BadgeTier.BRONZE -> Color(0xFFCD7F32).copy(alpha = 0.2f)
                    BadgeTier.SILVER -> Color(0xFFC0C0C0).copy(alpha = 0.2f)
                    BadgeTier.GOLD -> Color(0xFFFFD700).copy(alpha = 0.2f)
                    BadgeTier.PLATINUM -> Color(0xFFE5E4E2).copy(alpha = 0.2f)
                }
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
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
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = badge.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (!isUnlocked && progress > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress / 100f },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        text = "${progress.toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
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
