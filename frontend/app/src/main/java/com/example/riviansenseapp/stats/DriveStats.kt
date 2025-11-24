package com.example.riviansenseapp.stats

import com.example.riviansenseapp.context.Location
import com.example.riviansenseapp.context.Mood

/**
 * Model za statistiku vožnje
 */
data class DriveStats(
    // Ukupno vreme u svakom mood-u (u sekundama)
    val nervousTime: Long = 0,
    val tiredTime: Long = 0,
    val neutralTime: Long = 0,
    
    // Ukupno vreme u svakoj lokaciji (u sekundama)
    val cityTime: Long = 0,
    val highwayTime: Long = 0,
    val forestTime: Long = 0,
    val garageTime: Long = 0,
    
    // Brojači za kombinacije (mood + location)
    val nervousCityCount: Int = 0,
    val nervousHighwayCount: Int = 0,
    val nervousForestCount: Int = 0,
    val tiredCityCount: Int = 0,
    val tiredHighwayCount: Int = 0,
    val tiredForestCount: Int = 0,
    
    // Ukupan broj vožnji
    val totalDrives: Int = 0,
    
    // Ukupno vreme vožnje (u sekundama)
    val totalDriveTime: Long = 0,
    
    // Timestamp poslednjeg update-a
    val lastUpdated: Long = System.currentTimeMillis()
) {
    /**
     * Vraća ukupno vreme za određeni mood
     */
    fun getTimeForMood(mood: Mood): Long {
        return when (mood) {
            Mood.NERVOUS -> nervousTime
            Mood.TIRED -> tiredTime
            Mood.NEUTRAL -> neutralTime
        }
    }
    
    /**
     * Vraća ukupno vreme za određenu lokaciju
     */
    fun getTimeForLocation(location: Location): Long {
        return when (location) {
            Location.CITY -> cityTime
            Location.HIGHWAY -> highwayTime
            Location.FOREST -> forestTime
            Location.GARAGE -> garageTime
        }
    }
    
    /**
     * Vraća procenat vremena u određenom mood-u
     */
    fun getMoodPercentage(mood: Mood): Float {
        if (totalDriveTime == 0L) return 0f
        return (getTimeForMood(mood).toFloat() / totalDriveTime) * 100f
    }
    
    /**
     * Vraća procenat vremena u određenoj lokaciji
     */
    fun getLocationPercentage(location: Location): Float {
        if (totalDriveTime == 0L) return 0f
        return (getTimeForLocation(location).toFloat() / totalDriveTime) * 100f
    }
    
    /**
     * Formatiše vreme u čitljiv format (HH:MM:SS)
     */
    fun formatTime(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, secs)
    }
}
