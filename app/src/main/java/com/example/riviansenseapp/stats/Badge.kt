package com.example.riviansenseapp.stats

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
    data class MoodTime(val mood: com.example.riviansenseapp.context.Mood, val seconds: Long) : BadgeRequirement()
    data class LocationTime(val location: com.example.riviansenseapp.context.Location, val seconds: Long) : BadgeRequirement()
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
            title = "Prva Vožnja",
            description = "Završi svoju prvu vožnju",
            icon = "🚗",
            requirement = BadgeRequirement.TotalDrives(1),
            tier = BadgeTier.BRONZE
        ),
        Badge(
            id = "novice_driver",
            title = "Početnik",
            description = "Završi 10 vožnji",
            icon = "🛣️",
            requirement = BadgeRequirement.TotalDrives(10),
            tier = BadgeTier.BRONZE
        ),
        Badge(
            id = "experienced_driver",
            title = "Iskusan Vozač",
            description = "Završi 50 vožnji",
            icon = "🏁",
            requirement = BadgeRequirement.TotalDrives(50),
            tier = BadgeTier.SILVER
        ),
        Badge(
            id = "veteran_driver",
            title = "Veteran",
            description = "Završi 100 vožnji",
            icon = "🏆",
            requirement = BadgeRequirement.TotalDrives(100),
            tier = BadgeTier.GOLD
        ),
        
        // ========== UKUPNO VREME VOŽNJE ==========
        Badge(
            id = "one_hour",
            title = "Prvi Sat",
            description = "Provedi 1 sat na putu",
            icon = "⏱️",
            requirement = BadgeRequirement.TotalTime(3600), // 1 sat
            tier = BadgeTier.BRONZE
        ),
        Badge(
            id = "ten_hours",
            title = "10 Sati",
            description = "Provedi 10 sati na putu",
            icon = "⏰",
            requirement = BadgeRequirement.TotalTime(36000), // 10 sati
            tier = BadgeTier.SILVER
        ),
        Badge(
            id = "fifty_hours",
            title = "Pola Stotine",
            description = "Provedi 50 sati na putu",
            icon = "🕐",
            requirement = BadgeRequirement.TotalTime(180000), // 50 sati
            tier = BadgeTier.GOLD
        ),
        Badge(
            id = "hundred_hours",
            title = "100 Sati",
            description = "Provedi 100 sati na putu",
            icon = "💯",
            requirement = BadgeRequirement.TotalTime(360000), // 100 sati
            tier = BadgeTier.PLATINUM
        ),
        
        // ========== SMIREN VOZAČ (NEUTRAL MOOD) ==========
        Badge(
            id = "calm_driver",
            title = "Smiren Vozač",
            description = "Provedi 5 sati u neutralnom stanju",
            icon = "😌",
            requirement = BadgeRequirement.MoodTime(
                com.example.riviansenseapp.context.Mood.NEUTRAL,
                18000 // 5 sati
            ),
            tier = BadgeTier.SILVER
        ),
        Badge(
            id = "zen_master",
            title = "Zen Majstor",
            description = "Provedi 20 sati u neutralnom stanju",
            icon = "🧘",
            requirement = BadgeRequirement.MoodTime(
                com.example.riviansenseapp.context.Mood.NEUTRAL,
                72000 // 20 sati
            ),
            tier = BadgeTier.GOLD
        ),
        
        // ========== CITY DRIVING ==========
        Badge(
            id = "city_explorer",
            title = "Gradski Istraživač",
            description = "Provedi 10 sati u gradu",
            icon = "🌆",
            requirement = BadgeRequirement.LocationTime(
                com.example.riviansenseapp.context.Location.CITY,
                36000 // 10 sati
            ),
            tier = BadgeTier.SILVER
        ),
        
        // ========== HIGHWAY DRIVING ==========
        Badge(
            id = "highway_cruiser",
            title = "Autoputski Cruiser",
            description = "Provedi 10 sati na autoputu",
            icon = "🛣️",
            requirement = BadgeRequirement.LocationTime(
                com.example.riviansenseapp.context.Location.HIGHWAY,
                36000 // 10 sati
            ),
            tier = BadgeTier.SILVER
        ),
        Badge(
            id = "highway_master",
            title = "Kralj Autoputa",
            description = "Provedi 50 sati na autoputu",
            icon = "🚀",
            requirement = BadgeRequirement.LocationTime(
                com.example.riviansenseapp.context.Location.HIGHWAY,
                180000 // 50 sati
            ),
            tier = BadgeTier.GOLD
        ),
        
        // ========== FOREST DRIVING ==========
        Badge(
            id = "nature_lover",
            title = "Ljubitelj Prirode",
            description = "Provedi 5 sati u šumi",
            icon = "🌲",
            requirement = BadgeRequirement.LocationTime(
                com.example.riviansenseapp.context.Location.FOREST,
                18000 // 5 sati
            ),
            tier = BadgeTier.SILVER
        ),
        
        // ========== STRESS MANAGEMENT ==========
        Badge(
            id = "stress_survivor",
            title = "Preživeo Stres",
            description = "Prebrodi 5 sati nervoznog stanja",
            icon = "😰",
            requirement = BadgeRequirement.MoodTime(
                com.example.riviansenseapp.context.Mood.NERVOUS,
                18000 // 5 sati
            ),
            tier = BadgeTier.BRONZE
        ),
        
        // ========== FATIGUE MANAGEMENT ==========
        Badge(
            id = "night_owl",
            title = "Noćna Ptica",
            description = "Provedi 5 sati umornog stanja (verovatno noću)",
            icon = "🦉",
            requirement = BadgeRequirement.MoodTime(
                com.example.riviansenseapp.context.Mood.TIRED,
                18000 // 5 sati
            ),
            tier = BadgeTier.BRONZE
        ),
        
        // ========== LONG DRIVES ==========
        Badge(
            id = "marathon_driver",
            title = "Maratonac",
            description = "Završi vožnju dužu od 3 sata",
            icon = "🏃",
            requirement = BadgeRequirement.LongDrive(10800), // 3 sata
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
