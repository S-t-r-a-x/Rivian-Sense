package com.example.riviansenseapp.context

/**
 * Model za kontekst vozača
 */
data class DriverContext(
    val mood: Mood,
    val location: Location,
    val timestamp: Long = System.currentTimeMillis()
)

enum class Mood {
    NERVOUS,  // Nervozan
    TIRED,    // Umoran
    NEUTRAL   // Neutralno (default)
}

enum class Location {
    FOREST,   // Šuma
    CITY,     // Grad
    HIGHWAY,  // Autoput
    GARAGE    // Garaža
}
