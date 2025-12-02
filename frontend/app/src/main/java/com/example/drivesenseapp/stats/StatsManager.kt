package com.example.drivesenseapp.stats

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.drivesenseapp.context.DriverContext
import com.example.drivesenseapp.context.Location
import com.example.drivesenseapp.context.Mood
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Manager za praćenje statistike vožnje i badge-ova
 */
class StatsManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("drive_stats", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    // Tracking trenutne sesije
    private var currentSessionStart: Long? = null
    private var currentMood: Mood? = null
    private var currentLocation: Location? = null
    private var lastUpdateTime: Long = System.currentTimeMillis()
    
    companion object {
        private const val TAG = "StatsManager"
        private const val KEY_STATS = "stats_data"
        private const val KEY_UNLOCKED_BADGES = "unlocked_badges"
        private const val KEY_CURRENT_DRIVE_START = "current_drive_start"
    }
    
    /**
     * Učitava trenutnu statistiku iz SharedPreferences
     */
    fun getStats(): DriveStats {
        val json = prefs.getString(KEY_STATS, null) ?: return DriveStats()
        return try {
            gson.fromJson(json, DriveStats::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading stats: ${e.message}")
            DriveStats()
        }
    }
    
    /**
     * Čuva statistiku u SharedPreferences
     */
    private fun saveStats(stats: DriveStats) {
        val json = gson.toJson(stats)
        prefs.edit().putString(KEY_STATS, json).apply()
    }
    
    /**
     * Započinje tracking sesije vožnje
     */
    fun startDriveSession(context: DriverContext) {
        currentSessionStart = System.currentTimeMillis()
        currentMood = context.mood
        currentLocation = context.location
        lastUpdateTime = System.currentTimeMillis()
        
        // Sačuvaj timestamp početka vožnje
        prefs.edit().putLong(KEY_CURRENT_DRIVE_START, currentSessionStart!!).apply()
        
        Log.d(TAG, "📊 Drive session started: mood=${context.mood}, location=${context.location}")
    }
    
    /**
     * Završava tracking sesije vožnje
     */
    fun endDriveSession(): Long {
        val startTime = currentSessionStart ?: prefs.getLong(KEY_CURRENT_DRIVE_START, 0L)
        if (startTime == 0L) {
            Log.w(TAG, "No active drive session to end")
            return 0L
        }
        
        // ✅ FIX: Prvo dodaj vreme trenutnog mood-a/lokacije pre završetka
        val now = System.currentTimeMillis()
        val finalElapsedSeconds = (now - lastUpdateTime) / 1000
        
        if (finalElapsedSeconds > 0 && currentMood != null && currentLocation != null) {
            Log.d(TAG, "📊 Adding final time before ending: ${finalElapsedSeconds}s for mood=$currentMood, location=$currentLocation")
            incrementTime(currentMood!!, currentLocation!!, finalElapsedSeconds)
        }
        
        val endTime = System.currentTimeMillis()
        val durationSeconds = (endTime - startTime) / 1000
        
        // Update brojač vožnji
        val stats = getStats()
        val updatedStats = stats.copy(
            totalDrives = stats.totalDrives + 1,
            lastUpdated = System.currentTimeMillis()
        )
        saveStats(updatedStats)
        
        // Proveri badge-ove
        checkForNewBadges(updatedStats)
        
        // Reset session
        currentSessionStart = null
        currentMood = null
        currentLocation = null
        prefs.edit().remove(KEY_CURRENT_DRIVE_START).apply()
        
        Log.d(TAG, "📊 Drive session ended: total duration=${updatedStats.formatTime(durationSeconds)}")
        Log.d(TAG, "📊 Total drives: ${updatedStats.totalDrives}, Total time: ${updatedStats.formatTime(updatedStats.totalDriveTime)}")
        return durationSeconds
    }
    
    /**
     * Update-uje statistiku kada se kontekst promeni (mood ili location)
     */
    fun updateContext(newContext: DriverContext) {
        val now = System.currentTimeMillis()
        val elapsedSeconds = (now - lastUpdateTime) / 1000
        
        if (elapsedSeconds < 1) return // Ignoriši previše česte update-e
        
        // Dodaj vreme prethodnom mood-u i lokaciji
        if (currentMood != null && currentLocation != null) {
            incrementTime(currentMood!!, currentLocation!!, elapsedSeconds)
        }
        
        // Update trenutni kontekst
        currentMood = newContext.mood
        currentLocation = newContext.location
        lastUpdateTime = now
        
        Log.d(TAG, "📊 Context updated: mood=${newContext.mood}, location=${newContext.location}, elapsed=${elapsedSeconds}s")
    }
    
    /**
     * Dodaje vreme u statistiku za određeni mood i lokaciju
     */
    private fun incrementTime(mood: Mood, location: Location, seconds: Long) {
        Log.d(TAG, "⏱️ Adding ${seconds}s to mood=$mood, location=$location")
        val stats = getStats()
        
        val updatedStats = stats.copy(
            // Update mood time
            nervousTime = if (mood == Mood.NERVOUS) stats.nervousTime + seconds else stats.nervousTime,
            tiredTime = if (mood == Mood.TIRED) stats.tiredTime + seconds else stats.tiredTime,
            neutralTime = if (mood == Mood.NEUTRAL) stats.neutralTime + seconds else stats.neutralTime,
            
            // Update location time
            cityTime = if (location == Location.CITY) stats.cityTime + seconds else stats.cityTime,
            highwayTime = if (location == Location.HIGHWAY) stats.highwayTime + seconds else stats.highwayTime,
            forestTime = if (location == Location.FOREST) stats.forestTime + seconds else stats.forestTime,
            garageTime = if (location == Location.GARAGE) stats.garageTime + seconds else stats.garageTime,
            
            // Combination counters se ne update-uju ovde (samo se koriste za analitiku)
            
            totalDriveTime = stats.totalDriveTime + seconds,
            lastUpdated = System.currentTimeMillis()
        )
        
        saveStats(updatedStats)
        
        Log.d(TAG, "💾 Stats saved - Total time: ${updatedStats.formatTime(updatedStats.totalDriveTime)}")
        Log.d(TAG, "   Mood breakdown: N=${updatedStats.formatTime(updatedStats.nervousTime)}, T=${updatedStats.formatTime(updatedStats.tiredTime)}, Neu=${updatedStats.formatTime(updatedStats.neutralTime)}")
        Log.d(TAG, "   Location breakdown: C=${updatedStats.formatTime(updatedStats.cityTime)}, H=${updatedStats.formatTime(updatedStats.highwayTime)}, F=${updatedStats.formatTime(updatedStats.forestTime)}, G=${updatedStats.formatTime(updatedStats.garageTime)}")
        
        // Proveri da li su otključani novi badge-ovi
        checkForNewBadges(updatedStats)
    }
    
    /**
     * Vraća listu otključanih badge-ova
     */
    fun getUnlockedBadges(): List<Badge> {
        val json = prefs.getString(KEY_UNLOCKED_BADGES, null) ?: return emptyList()
        val type = object : TypeToken<List<String>>() {}.type
        val unlockedIds: List<String> = gson.fromJson(json, type)
        
        return BadgeDefinitions.allBadges.filter { it.id in unlockedIds }.map { badge ->
            badge.copy(
                isUnlocked = true,
                unlockedAt = prefs.getLong("badge_${badge.id}_unlocked_at", 0L)
            )
        }
    }
    
    /**
     * Vraća sve badge-ove sa statusom otključanosti
     */
    fun getAllBadgesWithStatus(): List<Badge> {
        val unlockedIds = getUnlockedBadges().map { it.id }.toSet()
        
        return BadgeDefinitions.allBadges.map { badge ->
            if (badge.id in unlockedIds) {
                badge.copy(
                    isUnlocked = true,
                    unlockedAt = prefs.getLong("badge_${badge.id}_unlocked_at", 0L)
                )
            } else {
                badge
            }
        }
    }
    
    /**
     * Proverava da li su ispunjeni uslovi za nove badge-ove
     */
    private fun checkForNewBadges(stats: DriveStats) {
        val currentlyUnlocked = getUnlockedBadges().map { it.id }.toSet()
        val newlyUnlocked = mutableListOf<Badge>()
        
        for (badge in BadgeDefinitions.allBadges) {
            // Preskoči već otključane
            if (badge.id in currentlyUnlocked) continue
            
            // Proveri da li je ispunjen uslov
            val isUnlocked = when (val req = badge.requirement) {
                is BadgeRequirement.TotalDrives -> stats.totalDrives >= req.count
                is BadgeRequirement.TotalTime -> stats.totalDriveTime >= req.seconds
                is BadgeRequirement.MoodTime -> stats.getTimeForMood(req.mood) >= req.seconds
                is BadgeRequirement.LocationTime -> stats.getTimeForLocation(req.location) >= req.seconds
                is BadgeRequirement.LongDrive -> false // Ovo se proverava u endDriveSession
                is BadgeRequirement.CalmStreak -> false // TODO: Implementiraj streak tracking
            }
            
            if (isUnlocked) {
                newlyUnlocked.add(badge)
                unlockBadge(badge.id)
                Log.d(TAG, "🏆 NEW BADGE UNLOCKED: ${badge.title}")
            }
        }
    }
    
    /**
     * Otključava badge
     */
    private fun unlockBadge(badgeId: String) {
        val currentUnlocked = getUnlockedBadges().map { it.id }.toMutableList()
        if (badgeId !in currentUnlocked) {
            currentUnlocked.add(badgeId)
            
            // Sačuvaj listu otključanih
            val json = gson.toJson(currentUnlocked)
            prefs.edit()
                .putString(KEY_UNLOCKED_BADGES, json)
                .putLong("badge_${badgeId}_unlocked_at", System.currentTimeMillis())
                .apply()
        }
    }
    
    /**
     * Resetuje sve statistike (za testiranje)
     */
    fun resetAllStats() {
        prefs.edit().clear().apply()
        Log.d(TAG, "📊 All stats reset")
    }
    
    /**
     * Ispisuje sve statistike u Logcat (za testiranje)
     */
    fun printStats() {
        val stats = getStats()
        val unlockedBadges = getUnlockedBadges()
        
        Log.d(TAG, "\n" + "=".repeat(60))
        Log.d(TAG, "📊 === DRIVE STATISTICS ===")
        Log.d(TAG, "=".repeat(60))
        Log.d(TAG, "🚗 Total Drives: ${stats.totalDrives}")
        Log.d(TAG, "⏱️  Total Drive Time: ${stats.formatTime(stats.totalDriveTime)}")
        Log.d(TAG, "")
        Log.d(TAG, "😰 MOOD BREAKDOWN:")
        Log.d(TAG, "  Nervous: ${stats.formatTime(stats.nervousTime)} (${stats.getMoodPercentage(Mood.NERVOUS)}%)")
        Log.d(TAG, "  Tired: ${stats.formatTime(stats.tiredTime)} (${stats.getMoodPercentage(Mood.TIRED)}%)")
        Log.d(TAG, "  Neutral: ${stats.formatTime(stats.neutralTime)} (${stats.getMoodPercentage(Mood.NEUTRAL)}%)")
        Log.d(TAG, "")
        Log.d(TAG, "🗺️  LOCATION BREAKDOWN:")
        Log.d(TAG, "  City: ${stats.formatTime(stats.cityTime)}")
        Log.d(TAG, "  Highway: ${stats.formatTime(stats.highwayTime)}")
        Log.d(TAG, "  Forest: ${stats.formatTime(stats.forestTime)}")
        Log.d(TAG, "  Garage: ${stats.formatTime(stats.garageTime)}")
        Log.d(TAG, "")
        Log.d(TAG, "🏆 UNLOCKED BADGES: ${unlockedBadges.size}")
        unlockedBadges.forEach { badge ->
            Log.d(TAG, "  ${badge.icon} ${badge.title} (${badge.tier})")
        }
        Log.d(TAG, "=".repeat(60) + "\n")
    }
    
    /**
     * Vraća procenat progresa za određeni badge
     */
    fun getBadgeProgress(badge: Badge): Float {
        val stats = getStats()
        
        return when (val req = badge.requirement) {
            is BadgeRequirement.TotalDrives -> {
                (stats.totalDrives.toFloat() / req.count) * 100f
            }
            is BadgeRequirement.TotalTime -> {
                (stats.totalDriveTime.toFloat() / req.seconds) * 100f
            }
            is BadgeRequirement.MoodTime -> {
                (stats.getTimeForMood(req.mood).toFloat() / req.seconds) * 100f
            }
            is BadgeRequirement.LocationTime -> {
                (stats.getTimeForLocation(req.location).toFloat() / req.seconds) * 100f
            }
            is BadgeRequirement.LongDrive -> 0f // Ne može se pratiti
            is BadgeRequirement.CalmStreak -> 0f // TODO
        }.coerceIn(0f, 100f)
    }
}
