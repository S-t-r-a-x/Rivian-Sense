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
    SPOTIFY_CALM,      // Opuštajuća muzika
    SPOTIFY_ENERGETIC, // Energična muzika
    SPOTIFY_PODCAST,   // Podcast
    
    // Phone
    DND_ENABLE,        // Uključi DND
    DND_DISABLE,       // Isključi DND
    
    // Navigation
    NAV_HOME,          // Navigacija kući
    NAV_REST_STOP,     // Predlog pauze
    NAV_COFFEE,        // Predlog kafe
    
    // Wellbeing
    BREATHING,         // Vežba disanja
    STRETCH,           // Stretch vežbe
    
    // Logging
    LOG_DRIVE,         // Završi vožnju
    CREATE_REMINDER    // Kreiraj reminder
}
