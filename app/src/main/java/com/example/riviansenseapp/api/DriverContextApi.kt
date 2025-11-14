package com.example.riviansenseapp.api

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.riviansenseapp.context.DriverContext
import com.example.riviansenseapp.context.Location
import com.example.riviansenseapp.context.Mood
import com.google.gson.Gson
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import java.net.URISyntaxException
import android.os.Handler
import android.os.Looper

/**
 * API servis za dohvatanje mood i location konteksta preko WebSocket-a
 */
class DriverContextApi(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("driver_context", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    private var socket: Socket? = null
    private var isConnected = false
    private val heartbeatHandler = Handler(Looper.getMainLooper())
    private var heartbeatRunnable: Runnable? = null
    
    companion object {
        private const val TAG = "DriverContextApi"
        // Za Android Emulator koristi (TESTIRANJE):
        private const val SERVER_URL = "http://192.168.0.73:5000"
        
        // Za pravi device koristi (nakon što emulator radi):
        // private const val SERVER_URL = "http://192.168.0.73:5000"
        
        // DEBUG: Dodaj timeout povećanje za slabu WiFi
        // private val opts = IO.Options().apply {
        //     timeout = 10000
        //     reconnection = true
        // }
    }
    
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
     * Pokreće WebSocket konekciju sa serverom i sluša 'driver_state' poruke
     */
    fun startContextMonitoring(callback: (DriverContext) -> Unit) {
        try {
            // Konfiguriši Socket.IO opcije za HTTP long-polling
            val opts = IO.Options().apply {
                // ⚠️ KRITIČNO: HTTP long-polling NE koristi WebSocket ping/pong
                // Umesto toga, polling request drži konekciju otvorenom duže vreme
                timeout = 120000  // 120 sekundi (2 minuta) - mora biti DUGO za polling
                reconnection = true  // Auto-reconnect nakon disconnect-a
                reconnectionDelay = 1000  // 1 sekunda između pokušaja
                reconnectionAttempts = Int.MAX_VALUE  // Beskrajno pokušavaj
                reconnectionDelayMax = 5000  // Maksimalno 5 sekundi između pokušaja
                
                // Forsiraj SAMO polling transport (zaobiđi WebSocket problem)
                transports = arrayOf("polling")
                
                // NE upgrade-uj na WebSocket (ostani na polling)
                forceNew = false  // Reuse konekcije
                upgrade = false  // Ne pokušavaj upgrade na WebSocket
            }
            
            // Inicijalizuj Socket.IO konekciju sa opcijama
            socket = IO.socket(SERVER_URL, opts)
            
            Log.d(TAG, "🔌 Attempting connection to $SERVER_URL (polling mode)...")
            
            // Event: Uspešna konekcija
            socket?.on(Socket.EVENT_CONNECT) {
                isConnected = true
                Log.d(TAG, "=".repeat(60))
                Log.d(TAG, "✅ CONNECTED to Flask-SocketIO server")
                Log.d(TAG, "   URL: $SERVER_URL")
                Log.d(TAG, "   Transport: polling")
                Log.d(TAG, "   Thread: ${Thread.currentThread().name}")
                Log.d(TAG, "=".repeat(60))
                
                // Pokreni heartbeat (šalji ping na svakih 15 sekundi)
                startHeartbeat()
            }
            
            // Event: Diskonektovanje
            socket?.on(Socket.EVENT_DISCONNECT) { args ->
                isConnected = false
                stopHeartbeat()
                val reason = if (args.isNotEmpty()) args[0].toString() else "unknown"
                Log.w(TAG, "=".repeat(60))
                Log.w(TAG, "❌ DISCONNECTED from server")
                Log.w(TAG, "   Reason: $reason")
                Log.w(TAG, "   Thread: ${Thread.currentThread().name}")
                Log.w(TAG, "=".repeat(60))
            }
            
            // Event: Greška pri konekciji
            socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
                val error = if (args.isNotEmpty()) args[0].toString() else "unknown error"
                Log.e(TAG, "⚠️ Connection error: $error")
            }
            

            
            // Event: Primanje 'driver_state' poruka od servera
            socket?.on("driver_state") { args ->
                try {
                    val data = args[0] as JSONObject
                    val state = data.getString("state").lowercase()
                    
                    // Opcionalno primanje location sa servera
                    val locationString = if (data.has("location")) {
                        data.getString("location").lowercase()
                    } else {
                        null
                    }
                    
                    Log.d(TAG, "📩 Received driver_state: mood=$state, location=$locationString")
                    
                    // Mapiranje state -> Mood
                    val mood = when (state) {
                        "nervous", "stressed", "anxious" -> Mood.NERVOUS
                        "tired", "sleepy", "exhausted" -> Mood.TIRED
                        "neutral", "calm", "normal" -> Mood.NEUTRAL
                        else -> {
                            Log.w(TAG, "Unknown state: $state, defaulting to NEUTRAL")
                            Mood.NEUTRAL
                        }
                    }
                    
                    // Mapiranje location (ili zadrži postojeću)
                    val location = if (locationString != null) {
                        when (locationString) {
                            "city", "urban" -> Location.CITY
                            "highway", "freeway", "motorway" -> Location.HIGHWAY
                            "forest", "nature", "woods" -> Location.FOREST
                            "garage", "parking", "home" -> Location.GARAGE
                            else -> {
                                Log.w(TAG, "Unknown location: $locationString, keeping current")
                                getCurrentContext().location
                            }
                        }
                    } else {
                        // Ako server nije poslao location, zadrži postojeću
                        getCurrentContext().location
                    }
                    
                    val newContext = DriverContext(mood, location)
                    
                    // Sačuvaj u SharedPreferences
                    setMockContext(mood, location)
                    saveContextToHistory(newContext)
                    
                    // Pozovi callback
                    callback(newContext)
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing driver_state: ${e.message}")
                }
            }
            
            // Pokreni konekciju
            socket?.connect()
            Log.d(TAG, "🔌 Connecting to $SERVER_URL...")
            
        } catch (e: URISyntaxException) {
            Log.e(TAG, "Invalid server URL: ${e.message}")
        }
    }
    
    /**
     * Zatvara WebSocket konekciju
     */
    fun stopContextMonitoring() {
        Log.i(TAG, "🛑 Stopping context monitoring...")
        Log.i(TAG, "   Current state: isConnected=$isConnected, socket?.connected()=${socket?.connected()}")
        stopHeartbeat()
        socket?.disconnect()
        socket?.off()
        socket = null
        isConnected = false
        Log.d(TAG, "🔌 WebSocket connection closed")
    }
    
    /**
     * Provera da li je konekcija aktivna
     */
    fun isConnected(): Boolean = isConnected
    
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
     * Pokreni heartbeat timer (šalje ping na svakih 15 sekundi)
     */
    private fun startHeartbeat() {
        stopHeartbeat() // Prvo zaustavi postojeći ako postoji
        
        heartbeatRunnable = object : Runnable {
            override fun run() {
                if (isConnected && socket?.connected() == true) {
                    try {
                        val timestamp = System.currentTimeMillis()
                        socket?.emit("ping", JSONObject().put("timestamp", timestamp))
                        Log.d(TAG, "💓 Heartbeat sent (connected=${socket?.connected()}, isConnected=$isConnected)")
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Heartbeat failed: ${e.message}")
                        Log.e(TAG, "   Socket state: connected=${socket?.connected()}, isConnected=$isConnected")
                    }
                } else {
                    Log.w(TAG, "⚠️ Heartbeat skipped - not connected (socket?.connected()=${socket?.connected()}, isConnected=$isConnected)")
                }
                // Zakaži sledeći heartbeat za 15 sekundi
                heartbeatHandler.postDelayed(this, 15000)
            }
        }
        
        // Pokreni prvi heartbeat nakon 15 sekundi
        heartbeatHandler.postDelayed(heartbeatRunnable!!, 15000)
    }
    
    /**
     * Zaustavi heartbeat timer
     */
    private fun stopHeartbeat() {
        heartbeatRunnable?.let {
            heartbeatHandler.removeCallbacks(it)
        }
        heartbeatRunnable = null
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
