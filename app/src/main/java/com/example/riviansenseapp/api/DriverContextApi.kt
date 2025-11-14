package com.example.riviansenseapp.api

import android.content.Context
import android.content.SharedPreferences
import com.example.riviansenseapp.context.DriverContext
import com.example.riviansenseapp.context.Location
import com.example.riviansenseapp.context.Mood
import com.google.gson.Gson

/**
 * Mock API servis za dohvatanje mood i location konteksta
 * Kasnije će biti zamenjeno pravim API pozivom
 */
class DriverContextApi(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("driver_context", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    /**
     * Simulira API poziv za dobijanje trenutnog konteksta
     * U produkciji će ovo biti pravi HTTP request
     */
    fun getCurrentContext(): DriverContext {
        // Za sada čitamo iz SharedPreferences
        // Kasnije će ovo biti retrofit/ktor API call
        val moodString = prefs.getString("current_mood", "NEUTRAL") ?: "NEUTRAL"
        val locationString = prefs.getString("current_location", "CITY") ?: "CITY"
        
        return DriverContext(
            mood = Mood.valueOf(moodString),
            location = Location.valueOf(locationString),
            timestamp = System.currentTimeMillis()
        )
    }
    
    /**
     * Mock funkcija za setovanje konteksta (za testiranje)
     * U produkciji ovo neće biti potrebno jer će API vršiti analizu
     */
    fun setMockContext(mood: Mood, location: Location) {
        prefs.edit()
            .putString("current_mood", mood.name)
            .putString("current_location", location.name)
            .putLong("context_timestamp", System.currentTimeMillis())
            .apply()
    }
    
    /**
     * Simulira kontinuirano praćenje konteksta
     * U produkciji će ovo biti WebSocket ili polling mehanizam
     */
    fun startContextMonitoring(callback: (DriverContext) -> Unit) {
        // TODO: Implementirati polling ili WebSocket listener
        // Za sada samo vraća trenutni kontekst
        callback(getCurrentContext())
    }
    
    /**
     * Vraća istoriju konteksta (za analizu)
     */
    fun getContextHistory(): List<DriverContext> {
        val historyJson = prefs.getString("context_history", "[]") ?: "[]"
        return try {
            gson.fromJson(historyJson, Array<DriverContext>::class.java).toList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Čuva kontekst u istoriju
     */
    fun saveContextToHistory(context: DriverContext) {
        val history = getContextHistory().toMutableList()
        history.add(context)
        
        // Čuvaj samo poslednjih 50 unosa
        if (history.size > 50) {
            history.removeAt(0)
        }
        
        prefs.edit()
            .putString("context_history", gson.toJson(history))
            .apply()
    }
}
