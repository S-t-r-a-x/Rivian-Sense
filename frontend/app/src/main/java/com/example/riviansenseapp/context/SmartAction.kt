package com.example.riviansenseapp.context

/**
 * Smart Action preporuka bazirana na kontekstu
 */
data class SmartAction(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val priority: Int, // 1 = najviši prioritet
    val action: ActionType,
    val reason: String // Zašto je preporučeno
)

enum class ActionType {
    // Spotify variants
    SPOTIFY_CALM,      // Calm music
    SPOTIFY_ENERGETIC, // Energetic music
    SPOTIFY_PODCAST,   // Podcast
    
    // Phone
    DND_ENABLE,        // Enable DND
    DND_DISABLE,       // Disable DND
    
    // Navigation
    NAV_HOME,          // Navigate home
    NAV_REST_STOP,     // Rest stop suggestion
    NAV_COFFEE,        // Coffee suggestion
    
    // Wellbeing
    BREATHING,         // Breathing exercise
    STRETCH,           // Stretch exercises
    
    // Logging
    LOG_DRIVE,         // End drive
    CREATE_REMINDER    // Create reminder
}
