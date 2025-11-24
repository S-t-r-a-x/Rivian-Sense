package com.example.riviansenseapp.ui.screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.riviansenseapp.ui.theme.RivianSenseAppTheme

// Enum for all toggleable features (this is what we store in SharedPreferences)
enum class AppFeature {
    // AUDIO & MEDIA
    PLAY_CALM_MUSIC,
    PLAY_ENERGETIC_MUSIC,
    PLAY_PODCAST_OR_AUDIOBOOK,
    FADE_OUT_MUSIC,

    // PHONE / NOTIFICATIONS
    ENABLE_DND,
    AUTO_REPLY,

    // NAVIGATION / BREAKS
    SMART_NAVIGATION,
    SUGGEST_BREAK,

    // WELLBEING / INTERACTION
    START_BREATHING_EXERCISE,
    SHOW_MICRO_STRETCH_PROMPT,

    // LOGGING, REMINDERS & STREAKS
    CREATE_POST_DRIVE_REMINDER,
    LOG_DRIVE_SUMMARY,
    AGGRESSIVE_CHIME,
    CALM_DRIVE_STREAK,
    SHOW_STREAK_CARD
}

// UI model for a single feature row
data class Feature(
    val id: AppFeature,
    val name: String,
    val description: String,
    val action: String,
    val enabled: Boolean
)

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current

    // Load features with enabled state from SharedPreferences
    var features by remember {
        val loaded = loadFeaturesWithPrefs(context)
        Log.d("SettingsScreen", "📥 Loaded ${loaded.size} features from SharedPreferences")
        loaded.forEachIndexed { index, feature ->
            Log.d("SettingsScreen", "  ${index + 1}. ${feature.name} [${feature.id.name}] = ${if (feature.enabled) "✅ ENABLED" else "❌ DISABLED"}")
        }
        mutableStateOf(loaded)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF020617)) // slate-950
            .verticalScroll(rememberScrollState())
    ) {
        // Back button with better spacing
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 24.dp, top = 48.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    Log.d("SettingsScreen", "\n🔙 BACK button clicked - saving preferences...")
                    saveEnabledFeatures(context, features)
                    Log.d("SettingsScreen", "💾 Preferences saved, calling onBack()")
                    onBack()
                },
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
            
            // Header inline with back button
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                tint = Color(0xFF22D3EE),
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Settings",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        
        // Description
        Text(
            text = "Customize your Rivian Sense experience",
            fontSize = 14.sp,
            color = Color(0xFF94A3B8),
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 24.dp)
        )

        // Features List
        Text(
            text = "Available Features",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 16.dp)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 24.dp)
        ) {

            features.forEach { feature ->
                FeatureCard(
                    feature = feature,
                    onToggle = { checked ->
                        Log.d("SettingsScreen", "🔄 Toggle: ${feature.name} [${feature.id.name}] -> ${if (checked) "✅ ENABLED" else "❌ DISABLED"}")
                        features = features.map {
                            if (it.id == feature.id) it.copy(enabled = checked) else it
                        }
                    }
                )
            }
        }

        // App Info
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp)
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
    onToggle: (Boolean) -> Unit
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
                onCheckedChange = { checked -> onToggle(checked) },
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
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }
}

/**
 * Base set of features (full list of actions your app supports).
 * The "enabled" flag here is the default; user prefs override it.
 */
fun getDefaultFeatures() = listOf(
    // AUDIO & MEDIA
    Feature(
        id = AppFeature.PLAY_CALM_MUSIC,
        name = "Calm Music",
        description = "Play a relaxing Spotify playlist when you’re stressed or tense.",
        action = "playCalmMusic()",
        enabled = true
    ),
    Feature(
        id = AppFeature.PLAY_ENERGETIC_MUSIC,
        name = "Energetic Music",
        description = "Play an energetic Spotify playlist when you’re tired or bored.",
        action = "playEnergeticMusic()",
        enabled = true
    ),
    Feature(
        id = AppFeature.PLAY_PODCAST_OR_AUDIOBOOK,
        name = "Podcast & Audiobook",
        description = "Open Spotify podcasts/audiobooks when you’re stuck in traffic.",
        action = "playPodcastOrAudiobook()",
        enabled = true
    ),
    Feature(
        id = AppFeature.FADE_OUT_MUSIC,
        name = "Calm Fade-Out",
        description = "Gently reduce music volume over a few seconds to calm the cabin.",
        action = "fadeOutMusic(durationMs)",
        enabled = true
    ),

    // PHONE / NOTIFICATIONS
    Feature(
        id = AppFeature.ENABLE_DND,
        name = "Driving DND Mode",
        description = "Reduce or silence notifications while driving to keep you focused.",
        action = "enableDrivingDND(mode) / disableDrivingDND()",
        enabled = true
    ),
    Feature(
        id = AppFeature.AUTO_REPLY,
        name = "Auto-Reply to Messages",
        description = "Automatically reply to SMS and missed calls while you’re driving.",
        action = "autoReplyToMessages(template) / disableAutoReply()",
        enabled = false
    ),

    // NAVIGATION / BREAKS
    Feature(
        id = AppFeature.SMART_NAVIGATION,
        name = "Smart Navigation",
        description = "Open navigation to Home, Work, next event, or a custom address.",
        action = "openNavigationTo(destination)",
        enabled = true
    ),
    Feature(
        id = AppFeature.SUGGEST_BREAK,
        name = "Break Suggestions",
        description = "Suggest nearby rest stops, coffee places or scenic viewpoints.",
        action = "suggestBreak(stopType)",
        enabled = true
    ),

    // WELLBEING / INTERACTION
    Feature(
        id = AppFeature.START_BREATHING_EXERCISE,
        name = "Breathing Exercise",
        description = "Trigger a full-screen guided breathing session before or during a drive.",
        action = "startBreathingExercise(durationSec)",
        enabled = true
    ),
    Feature(
        id = AppFeature.SHOW_MICRO_STRETCH_PROMPT,
        name = "Micro Stretch Prompt",
        description = "Show a quick stretch prompt when you’re parked after a long or stressful drive.",
        action = "showMicroStretchPrompt()",
        enabled = true
    ),

    // LOGGING, REMINDERS & STREAKS
    Feature(
        id = AppFeature.CREATE_POST_DRIVE_REMINDER,
        name = "Post-Drive Reminder",
        description = "Let the app create reminders like “Reply to John when I arrive.”",
        action = "createPostDriveReminder(text)",
        enabled = true
    ),
    Feature(
        id = AppFeature.LOG_DRIVE_SUMMARY,
        name = "Drive Summary Logging",
        description = "Log each drive with mood, scenery and actions for later insights.",
        action = "logDriveSummary(moodStats)",
        enabled = true
    ),
    Feature(
        id = AppFeature.AGGRESSIVE_CHIME,
        name = "Aggressive Driving Chime",
        description = "Play a subtle chime if aggressive patterns are detected for a while.",
        action = "playSoftChimeForAggressivePattern()",
        enabled = true
    ),
    Feature(
        id = AppFeature.CALM_DRIVE_STREAK,
        name = "Calm Drive Streak Tracking",
        description = "Track how many drives in a row were mostly calm or neutral.",
        action = "incrementCalmDriveStreak() / resetCalmDriveStreak()",
        enabled = true
    ),
    Feature(
        id = AppFeature.SHOW_STREAK_CARD,
        name = "Calm Drive Streak Card",
        description = "Show a card with your calm drive streak and a fun badge.",
        action = "showStreakCard()",
        enabled = true
    )
)

private const val PREFS_NAME = "rivian_prefs"
private const val KEY_ENABLED_FEATURES = "enabled_features"

/**
 * Load features with enabled state overridden by SharedPreferences.
 * We store the enum names (AppFeature.name) in prefs.
 */
private fun loadFeaturesWithPrefs(context: Context): List<Feature> {
    val base = getDefaultFeatures()
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val savedIds = prefs.getStringSet(KEY_ENABLED_FEATURES, null)

    return if (savedIds == null) {
        base
    } else {
        base.map { feature ->
            feature.copy(enabled = savedIds.contains(feature.id.name))
        }
    }
}

/**
 * Save the set of enabled feature IDs (AppFeature.name) to SharedPreferences.
 */
private fun saveEnabledFeatures(context: Context, features: List<Feature>) {
    val enabledIds: Set<String> = features
        .filter { it.enabled }
        .map { it.id.name }
        .toSet()

    Log.d("SettingsScreen", "\n💾 === SAVING ENABLED FEATURES ===")
    Log.d("SettingsScreen", "📍 SharedPreferences: $PREFS_NAME")
    Log.d("SettingsScreen", "🔑 Key: $KEY_ENABLED_FEATURES")
    Log.d("SettingsScreen", "📊 Total features: ${features.size}")
    Log.d("SettingsScreen", "✅ Enabled features: ${enabledIds.size}")
    enabledIds.forEach { Log.d("SettingsScreen", "  ✓ $it") }
    Log.d("SettingsScreen", "================================\n")

    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putStringSet(KEY_ENABLED_FEATURES, enabledIds)
        .apply()
    
    Log.d("SettingsScreen", "✅ SharedPreferences.apply() completed")
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    RivianSenseAppTheme {
        SettingsScreen()
    }
}
