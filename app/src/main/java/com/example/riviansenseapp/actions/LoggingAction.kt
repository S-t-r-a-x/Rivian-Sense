package com.example.riviansenseapp.actions

import android.content.Context
import android.media.MediaPlayer
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class LoggingAction(private val context: Context) {
    
    private val prefs = context.getSharedPreferences("rivian_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    enum class Mood {
        RELAXED, STRESSED, SURPRISED, AGGRESSIVE, UNCERTAIN, HURRIED, NEUTRAL
    }
    
    enum class Scenery {
        GARAGE, PARKING, FOREST, HIGHWAY, TRAFFIC, CITY, RURAL
    }
    
    data class DriveSummary(
        val id: String = UUID.randomUUID().toString(),
        val startTime: Long,
        val endTime: Long,
        val dominantMood: Mood,
        val dominantScenery: Scenery,
        val keyActions: List<String> = emptyList(),
        val moodStats: Map<Mood, Int> = emptyMap(),
        val wasCalm: Boolean = false
    )
    
    fun createPostDriveReminder(text: String): Boolean {
        return try {
            val reminders = getReminders().toMutableList()
            val reminder = Reminder(
                id = UUID.randomUUID().toString(),
                text = text,
                createdAt = System.currentTimeMillis(),
                isCompleted = false
            )
            reminders.add(reminder)
            
            prefs.edit()
                .putString("reminders", gson.toJson(reminders))
                .apply()
            
            Toast.makeText(
                context,
                "✅ Reminder kreiran: \"$text\"",
                Toast.LENGTH_SHORT
            ).show()
            true
        } catch (e: Exception) {
            Toast.makeText(context, "Greška pri kreiranju remindera: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        }
    }
    
    fun createMissedCallReminder(callerName: String, phoneNumber: String): Boolean {
        val text = "Propušten poziv: $callerName ($phoneNumber)"
        return createPostDriveReminder(text)
    }
    
    fun logDriveSummary(
        dominantMood: Mood,
        dominantScenery: Scenery,
        keyActions: List<String> = emptyList(),
        moodStats: Map<Mood, Int> = emptyMap()
    ) {
        try {
            val currentTime = System.currentTimeMillis()
            val startTime = prefs.getLong("current_drive_start", currentTime)
            
            val wasCalm = dominantMood in listOf(Mood.RELAXED, Mood.NEUTRAL) &&
                    (moodStats[Mood.AGGRESSIVE] ?: 0) < 5
            
            val summary = DriveSummary(
                startTime = startTime,
                endTime = currentTime,
                dominantMood = dominantMood,
                dominantScenery = dominantScenery,
                keyActions = keyActions,
                moodStats = moodStats,
                wasCalm = wasCalm
            )
            
            val summaries = getDriveSummaries().toMutableList()
            summaries.add(summary)
            
            prefs.edit()
                .putString("drive_summaries", gson.toJson(summaries))
                .remove("current_drive_start")
                .apply()
            
            if (wasCalm) {
                incrementCalmDriveStreak()
            } else {
                resetCalmDriveStreak()
            }
            
        } catch (e: Exception) {
            Toast.makeText(context, "Greška pri logovanju vožnje: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    fun startDrive() {
        prefs.edit()
            .putLong("current_drive_start", System.currentTimeMillis())
            .apply()
    }
    
    fun playSoftChimeForAggressivePattern() {
        try {
            // Pusti soft chime korišćenjem RingtoneManager-a
            try {
                val notification = android.media.RingtoneManager.getDefaultUri(
                    android.media.RingtoneManager.TYPE_NOTIFICATION
                )
                val ringtone = android.media.RingtoneManager.getRingtone(context, notification)
                if (ringtone != null) {
                    ringtone.play()
                    android.util.Log.d("RivianChime", "🔔 Chime pokrenut (RingtoneManager)")
                } else {
                    throw Exception("Ringtone je null")
                }
            } catch (e: Exception) {
                android.util.Log.d("RivianChime", "⚠️ RingtoneManager failed: ${e.message}, pokušavam ToneGenerator")
                // Alternativa: koristi ToneGenerator
                try {
                    val toneGen = android.media.ToneGenerator(
                        android.media.AudioManager.STREAM_NOTIFICATION,
                        80 // Volumen (0-100)
                    )
                    toneGen.startTone(android.media.ToneGenerator.TONE_PROP_BEEP, 400)
                    android.util.Log.d("RivianChime", "🔔 Chime pokrenut (ToneGenerator)")
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        toneGen.release()
                    }, 500)
                } catch (ex: Exception) {
                    android.util.Log.e("RivianChime", "❌ ToneGenerator failed: ${ex.message}")
                    Toast.makeText(context, "Greška pri chime zvuku: ${ex.message}", Toast.LENGTH_SHORT).show()
                }
            }
            
            Toast.makeText(
                context,
                "🔔 Chime test - proveri Logcat sa tagom 'RivianChime'",
                Toast.LENGTH_SHORT
            ).show()
            
        } catch (e: Exception) {
            android.util.Log.e("RivianChime", "❌ Glavna greška: ${e.message}")
            Toast.makeText(context, "Greška pri chime zvuku: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    fun incrementCalmDriveStreak() {
        val currentStreak = getCalmDriveStreak()
        prefs.edit()
            .putInt("calm_drive_streak", currentStreak + 1)
            .putLong("last_calm_drive", System.currentTimeMillis())
            .apply()
    }
    
    fun resetCalmDriveStreak() {
        prefs.edit()
            .putInt("calm_drive_streak", 0)
            .apply()
    }
    
    fun getCalmDriveStreak(): Int {
        return prefs.getInt("calm_drive_streak", 0)
    }
    
    fun showStreakCard(): StreakData {
        val streak = getCalmDriveStreak()
        val message = when {
            streak == 0 -> "Započni svoj streak mirnih vožnji!"
            streak < 3 -> "$streak mirna vožnja - nastavi!"
            streak < 5 -> "$streak mirne vožnje zaredom – odlično!"
            streak < 10 -> "$streak mirnih vožnji zaredom – neverovatno! 🌟"
            else -> "$streak mirnih vožnji – majstore! 🏆"
        }
        
        return StreakData(
            streakCount = streak,
            message = message,
            badge = getBadgeForStreak(streak)
        )
    }
    
    private fun getBadgeForStreak(streak: Int): String {
        return when {
            streak < 3 -> "🌱"
            streak < 5 -> "⭐"
            streak < 10 -> "🌟"
            streak < 20 -> "💫"
            else -> "🏆"
        }
    }
    
    fun getDriveSummaries(): List<DriveSummary> {
        return try {
            val json = prefs.getString("drive_summaries", "[]") ?: "[]"
            val type = object : TypeToken<List<DriveSummary>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun getReminders(): List<Reminder> {
        return try {
            val json = prefs.getString("reminders", "[]") ?: "[]"
            val type = object : TypeToken<List<Reminder>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun clearReminder(id: String) {
        val reminders = getReminders().toMutableList()
        reminders.removeAll { it.id == id }
        prefs.edit()
            .putString("reminders", gson.toJson(reminders))
            .apply()
    }
    
    fun completeReminder(id: String) {
        val reminders = getReminders().toMutableList()
        val updatedReminders = reminders.map { reminder ->
            if (reminder.id == id) {
                reminder.copy(isCompleted = true)
            } else {
                reminder
            }
        }
        prefs.edit()
            .putString("reminders", gson.toJson(updatedReminders))
            .apply()
        
        Toast.makeText(context, "✅ Reminder označen kao završen", Toast.LENGTH_SHORT).show()
    }
    
    fun getAllReminders(): List<Reminder> {
        return getReminders()
    }
    
    fun getActiveReminders(): List<Reminder> {
        return getReminders().filter { !it.isCompleted }
    }
    
    fun getCompletedReminders(): List<Reminder> {
        return getReminders().filter { it.isCompleted }
    }
    
    fun printAllReminders() {
        val reminders = getReminders()
        if (reminders.isEmpty()) {
            android.util.Log.d("RivianReminders", "📋 REMINDERI: Nema sačuvanih remindera.")
            Toast.makeText(context, "Nema sačuvanih remindera.", Toast.LENGTH_SHORT).show()
            return
        }
        
        android.util.Log.d("RivianReminders", "")
        android.util.Log.d("RivianReminders", "═".repeat(60))
        android.util.Log.d("RivianReminders", "📋 SVI REMINDERI (${reminders.size}):")
        android.util.Log.d("RivianReminders", "═".repeat(60))
        
        reminders.forEachIndexed { index, reminder ->
            val status = if (reminder.isCompleted) "✅ ZAVRŠEN" else "⏳ AKTIVAN"
            val date = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault())
                .format(java.util.Date(reminder.createdAt))
            
            android.util.Log.d("RivianReminders", "")
            android.util.Log.d("RivianReminders", "${index + 1}. $status")
            android.util.Log.d("RivianReminders", "   Text: ${reminder.text}")
            android.util.Log.d("RivianReminders", "   Kreiran: $date")
            android.util.Log.d("RivianReminders", "   ID: ${reminder.id}")
        }
        
        android.util.Log.d("RivianReminders", "═".repeat(60))
        
        Toast.makeText(context, "📋 Ispisano ${reminders.size} remindera u Logcat", Toast.LENGTH_SHORT).show()
    }
    
    fun clearAllReminders() {
        val count = getReminders().size
        prefs.edit()
            .remove("reminders")
            .apply()
        
        android.util.Log.d("RivianReminders", "🗑️ Svi reminderi ($count) su obrisani")
        Toast.makeText(context, "🗑️ Obrisano $count remindera", Toast.LENGTH_SHORT).show()
    }
    
    data class Reminder(
        val id: String,
        val text: String,
        val createdAt: Long,
        val isCompleted: Boolean = false
    )
    
    data class StreakData(
        val streakCount: Int,
        val message: String,
        val badge: String
    )
}
