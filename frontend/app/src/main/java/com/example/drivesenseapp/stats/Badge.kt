package com.example.drivesenseapp.stats

/**
 * Model za badge/achievement
 */
data class Badge(
    val id: String,
    val title: String,
    val description: String,
    val icon: String, // Emoji za sada
    val requirement: BadgeRequirement,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null,
    val tier: BadgeTier = BadgeTier.BRONZE
)

/**
 * Tipovi badge-va
 */
enum class BadgeTier {
    BRONZE,  // Prvi nivo
    SILVER,  // Srednji nivo
    GOLD,    // Najviši nivo
    PLATINUM // Specijalni
}

/**
 * Zahtevi za otključavanje badge-a
 */
sealed class BadgeRequirement {
    data class TotalDrives(val count: Int) : BadgeRequirement()
    data class TotalTime(val seconds: Long) : BadgeRequirement()
    data class MoodTime(val mood: com.example.drivesenseapp.context.Mood, val seconds: Long) : BadgeRequirement()
    data class LocationTime(val location: com.example.drivesenseapp.context.Location, val seconds: Long) : BadgeRequirement()
    data class CalmStreak(val consecutiveDays: Int) : BadgeRequirement() // Uzastopni dani bez nervous mood-a
    data class LongDrive(val singleDriveSeconds: Long) : BadgeRequirement()
}

/**
 * Lista svih dostupnih badge-ova
 */
object BadgeDefinitions {
    val allBadges = listOf(
        // ========== UKUPAN BROJ VOŽNJI ==========
        Badge(
            id = "first_drive",
            title = "First Drive",
            description = "Complete your first drive",
            icon = "🚗",
            requirement = BadgeRequirement.TotalDrives(1),
            tier = BadgeTier.BRONZE
        ),
        Badge(
            id = "novice_driver",
            title = "Novice Driver",
            description = "Complete 10 drives",
            icon = "🛣️",
            requirement = BadgeRequirement.TotalDrives(10),
            tier = BadgeTier.BRONZE
        ),
        Badge(
            id = "experienced_driver",
            title = "Experienced Driver",
            description = "Complete 50 drives",
            icon = "🏁",
            requirement = BadgeRequirement.TotalDrives(50),
            tier = BadgeTier.SILVER
        ),
        Badge(
            id = "veteran_driver",
            title = "Veteran Driver",
            description = "Complete 100 drives",
            icon = "🏆",
            requirement = BadgeRequirement.TotalDrives(100),
            tier = BadgeTier.GOLD
        ),
        
        // ========== UKUPNO VREME VOŽNJE ==========
        Badge(
            id = "one_hour",
            title = "First Hour",
            description = "Spend 1 hour on the road",
            icon = "⏱️",
            requirement = BadgeRequirement.TotalTime(3600), // 1 hour
            tier = BadgeTier.BRONZE
        ),
        Badge(
            id = "ten_hours",
            title = "10 Hours",
            description = "Spend 10 hours on the road",
            icon = "⏰",
            requirement = BadgeRequirement.TotalTime(36000), // 10 hours
            tier = BadgeTier.SILVER
        ),
        Badge(
            id = "fifty_hours",
            title = "Fifty Hours",
            description = "Spend 50 hours on the road",
            icon = "🕐",
            requirement = BadgeRequirement.TotalTime(180000), // 50 hours
            tier = BadgeTier.GOLD
        ),
        Badge(
            id = "hundred_hours",
            title = "100 Hours",
            description = "Spend 100 hours on the road",
            icon = "💯",
            requirement = BadgeRequirement.TotalTime(360000), // 100 hours
            tier = BadgeTier.PLATINUM
        ),
        
        // ========== SMIREN VOZAČ (NEUTRAL MOOD) ==========
        Badge(
            id = "calm_driver",
            title = "Calm Driver",
            description = "Spend 5 hours in neutral mood",
            icon = "😌",
            requirement = BadgeRequirement.MoodTime(
                com.example.drivesenseapp.context.Mood.NEUTRAL,
                18000 // 5 hours
            ),
            tier = BadgeTier.SILVER
        ),
        Badge(
            id = "zen_master",
            title = "Zen Master",
            description = "Spend 20 hours in neutral mood",
            icon = "🧘",
            requirement = BadgeRequirement.MoodTime(
                com.example.drivesenseapp.context.Mood.NEUTRAL,
                72000 // 20 hours
            ),
            tier = BadgeTier.GOLD
        ),
        
        // ========== CITY DRIVING ==========
        Badge(
            id = "city_explorer",
            title = "City Explorer",
            description = "Spend 10 hours in the city",
            icon = "🌆",
            requirement = BadgeRequirement.LocationTime(
                com.example.drivesenseapp.context.Location.CITY,
                36000 // 10 hours
            ),
            tier = BadgeTier.SILVER
        ),
        
        // ========== HIGHWAY DRIVING ==========
        Badge(
            id = "highway_cruiser",
            title = "Highway Cruiser",
            description = "Spend 10 hours on the highway",
            icon = "🛣️",
            requirement = BadgeRequirement.LocationTime(
                com.example.drivesenseapp.context.Location.HIGHWAY,
                36000 // 10 hours
            ),
            tier = BadgeTier.SILVER
        ),
        Badge(
            id = "highway_master",
            title = "Highway Master",
            description = "Spend 50 hours on the highway",
            icon = "🚀",
            requirement = BadgeRequirement.LocationTime(
                com.example.drivesenseapp.context.Location.HIGHWAY,
                180000 // 50 hours
            ),
            tier = BadgeTier.GOLD
        ),
        
        // ========== FOREST DRIVING ==========
        Badge(
            id = "nature_lover",
            title = "Nature Lover",
            description = "Spend 5 hours in the forest",
            icon = "🌲",
            requirement = BadgeRequirement.LocationTime(
                com.example.drivesenseapp.context.Location.FOREST,
                18000 // 5 hours
            ),
            tier = BadgeTier.SILVER
        ),
        
        // ========== STRESS MANAGEMENT ==========
        Badge(
            id = "stress_survivor",
            title = "Stress Survivor",
            description = "Endure 5 hours of nervous mood",
            icon = "😰",
            requirement = BadgeRequirement.MoodTime(
                com.example.drivesenseapp.context.Mood.NERVOUS,
                18000 // 5 hours
            ),
            tier = BadgeTier.BRONZE
        ),
        
        // ========== FATIGUE MANAGEMENT ==========
        Badge(
            id = "night_owl",
            title = "Night Owl",
            description = "Spend 5 hours in tired mood (probably at night)",
            icon = "🦉",
            requirement = BadgeRequirement.MoodTime(
                com.example.drivesenseapp.context.Mood.TIRED,
                18000 // 5 hours
            ),
            tier = BadgeTier.BRONZE
        ),
        
        // ========== LONG DRIVES ==========
        Badge(
            id = "marathon_driver",
            title = "Marathon Driver",
            description = "Complete a drive longer than 3 hours",
            icon = "🏃",
            requirement = BadgeRequirement.LongDrive(10800), // 3 hours
            tier = BadgeTier.GOLD
        )
    )
    
    /**
     * Pronalazi badge po ID-u
     */
    fun findById(id: String): Badge? {
        return allBadges.find { it.id == id }
    }
    
    /**
     * Filtrira badge-ove po tier-u
     */
    fun getByTier(tier: BadgeTier): List<Badge> {
        return allBadges.filter { it.tier == tier }
    }
}
