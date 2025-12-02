package com.example.drivesenseapp.context

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

/**
 * Manager za kontekstualne akcije baziran na mood i location
 */
class ContextualActionManager(private val context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("drive_prefs", Context.MODE_PRIVATE)
    
    /**
     * Mapira ActionType na AppFeature iz Settings-a
     */
    private fun getFeatureForAction(actionType: ActionType): String? {
        return when (actionType) {
            ActionType.SPOTIFY_CALM -> "PLAY_CALM_MUSIC"
            ActionType.SPOTIFY_ENERGETIC -> "PLAY_ENERGETIC_MUSIC"
            ActionType.SPOTIFY_PODCAST -> "PLAY_PODCAST_OR_AUDIOBOOK"
            ActionType.DND_ENABLE, ActionType.DND_DISABLE -> "ENABLE_DND"
            ActionType.NAV_HOME, ActionType.NAV_REST_STOP, ActionType.NAV_COFFEE -> "SMART_NAVIGATION"
            ActionType.BREATHING -> "START_BREATHING_EXERCISE"
            ActionType.STRETCH -> "SHOW_MICRO_STRETCH_PROMPT"
            ActionType.LOG_DRIVE -> "LOG_DRIVE_SUMMARY"
            ActionType.CREATE_REMINDER -> "CREATE_POST_DRIVE_REMINDER"
        }
    }
    
    /**
     * Proverava da li je data akcija omogućena u Settings-u
     */
    private fun isActionEnabled(actionType: ActionType): Boolean {
        val featureName = getFeatureForAction(actionType) ?: return true
        val enabledFeatures = prefs.getStringSet("enabled_features", null)
        
        Log.d("ActionFilter", "🔍 Checking action: $actionType -> feature: $featureName")
        Log.d("ActionFilter", "📋 Enabled features: $enabledFeatures")
        
        // Ako nisu postavljene preference, sve je omogućeno (default)
        if (enabledFeatures == null) {
            Log.d("ActionFilter", "⚠️ No preferences saved - allowing all (default)")
            return true
        }
        
        val isEnabled = enabledFeatures.contains(featureName)
        Log.d("ActionFilter", "${if (isEnabled) "✅" else "❌"} Action $actionType is ${if (isEnabled) "ENABLED" else "DISABLED"}")
        
        return isEnabled
    }

    /**
     * Vraća listu preporučenih akcija na osnovu trenutnog konteksta
     * Filtrira akcije na osnovu Settings preference
     */
    fun getSmartActions(driverContext: DriverContext): List<SmartAction> {
        val actions = mutableListOf<SmartAction>()
        
        // 🛑 PRIORITY: Ako je vozac stao, dodaj stop akcije na vrh liste
        if (driverContext.stop) {
            Log.d("ActionFilter", "🛑 STOP detected - adding stop-specific actions")
            actions.addAll(getStopActions(driverContext.mood, driverContext.location))
        }
        
        // Dodaj akcije na osnovu mood-a
        when (driverContext.mood) {
            Mood.NERVOUS -> {
                actions.addAll(getNervousActions(driverContext.location))
            }
            Mood.TIRED -> {
                actions.addAll(getTiredActions(driverContext.location))
            }
            Mood.NEUTRAL -> {
                actions.addAll(getNeutralActions(driverContext.location))
            }
        }
        
        // ✅ FILTER: Ukloni akcije koje nisu omogućene u Settings-u
        Log.d("ActionFilter", "\n🎯 === FILTERING SMART ACTIONS ===")
        Log.d("ActionFilter", "📊 Total actions before filter: ${actions.size}")
        actions.forEach { Log.d("ActionFilter", "  - ${it.title} (${it.action})") }
        
        val filteredActions = actions.filter { isActionEnabled(it.action) }
        
        Log.d("ActionFilter", "\n✅ Actions after filter: ${filteredActions.size}")
        filteredActions.forEach { Log.d("ActionFilter", "  ✓ ${it.title} (${it.action})") }
        Log.d("ActionFilter", "==========================\n")
        
        return filteredActions.sortedBy { it.priority }
    }
    
    /**
     * Akcije koje se prikazuju kada je vozac stao (stop = true)
     * Ove akcije imaju najvisi prioritet
     * NAPOMENA: Reminders se prikazuju automatski kao notifikacije, ne kao akcija
     */
    private fun getStopActions(mood: Mood, location: Location): List<SmartAction> {
        val actions = mutableListOf<SmartAction>()
        
        // STRETCH EXERCISES (kada si stao)
        actions.add(SmartAction(
            id = "stretch_stop",
            title = "Stretch Exercises",
            description = "Stretch while you wait",
            icon = "🤸",
            priority = 0,
            action = ActionType.STRETCH,
            reason = "Use the break to stretch"
        ))
        
        return actions
    }
    
    private fun getNervousActions(location: Location): List<SmartAction> {
        val actions = mutableListOf<SmartAction>()
        
        // DND mode (highest priority for nervous)
        actions.add(SmartAction(
            id = "dnd_nervous",
            title = "Enable Do Not Disturb",
            description = "Block calls and notifications",
            icon = "🔕",
            priority = 1,
            action = ActionType.DND_ENABLE,
            reason = "Nervousness detected - reduce distractions"
        ))
        
        // Breathing exercise
        actions.add(SmartAction(
            id = "breathing_nervous",
            title = "Breathing Exercise",
            description = "4-minute calming technique",
            icon = "🫁",
            priority = 2,
            action = ActionType.BREATHING,
            reason = "Regulate breathing to calm down"
        ))
        
        // Calm music
        actions.add(SmartAction(
            id = "spotify_calm",
            title = "Calm Music",
            description = "Play peaceful music",
            icon = "🎵",
            priority = 3,
            action = ActionType.SPOTIFY_CALM,
            reason = "Music helps with calming"
        ))
        
        // Location-specific actions
        when (location) {
            Location.CITY -> {
                // U gradu, predloži pauzu
                actions.add(SmartAction(
                    id = "coffee_nervous_city",
                    title = "Suggestion: Coffee Break",
                    description = "Find nearest coffee shop",
                    icon = "☕",
                    priority = 4,
                    action = ActionType.NAV_COFFEE,
                    reason = "City traffic - time for a break"
                ))
            }
            Location.HIGHWAY -> {
                // Na autoputu, predloži odmorište
                actions.add(SmartAction(
                    id = "rest_nervous_highway",
                    title = "Suggestion: Rest Stop",
                    description = "Find nearest rest area",
                    icon = "🅿️",
                    priority = 4,
                    action = ActionType.NAV_REST_STOP,
                    reason = "Long drive - take a break"
                ))
            }
            Location.FOREST -> {
                // U šumi, samo stretch
                actions.add(SmartAction(
                    id = "stretch_nervous_forest",
                    title = "Stretch Exercises",
                    description = "5-minute stretching",
                    icon = "🤸",
                    priority = 4,
                    action = ActionType.STRETCH,
                    reason = "Use nature for relaxation"
                ))
            }
            Location.GARAGE -> {
                // U garaži, završi vožnju
                actions.add(SmartAction(
                    id = "log_nervous_garage",
                    title = "End Drive",
                    description = "Save drive statistics",
                    icon = "📊",
                    priority = 4,
                    action = ActionType.LOG_DRIVE,
                    reason = "You've arrived - time to rest"
                ))
            }
        }
        
        return actions
    }
    
    private fun getTiredActions(location: Location): List<SmartAction> {
        val actions = mutableListOf<SmartAction>()
        
        // Energetic music (highest priority for tired)
        actions.add(SmartAction(
            id = "spotify_energetic",
            title = "Energetic Music",
            description = "Upbeat music for alertness",
            icon = "⚡",
            priority = 1,
            action = ActionType.SPOTIFY_ENERGETIC,
            reason = "Fatigue detected - boost energy"
        ))
        
        // Stretch exercises
        actions.add(SmartAction(
            id = "stretch_tired",
            title = "Stretch Exercises",
            description = "Stretch for 5 minutes",
            icon = "🤸",
            priority = 2,
            action = ActionType.STRETCH,
            reason = "Activate muscles and improve circulation"
        ))
        
        // Location-specific actions
        when (location) {
            Location.HIGHWAY -> {
                // Na autoputu, hitno predloži pauzu
                actions.add(SmartAction(
                    id = "rest_tired_highway",
                    title = "⚠️ URGENT: Rest Stop",
                    description = "Find nearest rest area",
                    icon = "🛑",
                    priority = 1, // Override priority
                    action = ActionType.NAV_REST_STOP,
                    reason = "DANGER: Fatigue + highway = high risk"
                ))
            }
            Location.CITY -> {
                actions.add(SmartAction(
                    id = "coffee_tired_city",
                    title = "Suggestion: Coffee Break",
                    description = "Caffeine break",
                    icon = "☕",
                    priority = 3,
                    action = ActionType.NAV_COFFEE,
                    reason = "Refresh with coffee"
                ))
            }
            Location.GARAGE -> {
                actions.add(SmartAction(
                    id = "log_tired_garage",
                    title = "End Drive",
                    description = "Save statistics and rest",
                    icon = "📊",
                    priority = 3,
                    action = ActionType.LOG_DRIVE,
                    reason = "You've arrived - time to rest"
                ))
            }
            Location.FOREST -> {
                actions.add(SmartAction(
                    id = "breathing_tired_forest",
                    title = "Fresh Air Breathing Exercise",
                    description = "Deep breathing",
                    icon = "🫁",
                    priority = 3,
                    action = ActionType.BREATHING,
                    reason = "Fresh air + oxygen = more energy"
                ))
            }
        }
        
        // Podcast suggestion
        actions.add(SmartAction(
            id = "podcast_tired",
            title = "Interesting Podcast",
            description = "Mental stimulation",
            icon = "🎙️",
            priority = 4,
            action = ActionType.SPOTIFY_PODCAST,
            reason = "Interesting content keeps attention"
        ))
        
        return actions
    }
    
    private fun getNeutralActions(location: Location): List<SmartAction> {
        val actions = mutableListOf<SmartAction>()
        
        // Basic actions for neutral mood
        when (location) {
            Location.CITY -> {
                actions.add(SmartAction(
                    id = "nav_home_neutral",
                    title = "Navigate Home",
                    description = "Fastest route",
                    icon = "🏠",
                    priority = 1,
                    action = ActionType.NAV_HOME,
                    reason = "Standard navigation"
                ))
            }
            Location.HIGHWAY -> {
                actions.add(SmartAction(
                    id = "podcast_neutral_highway",
                    title = "Podcast",
                    description = "Fun content for the road",
                    icon = "🎙️",
                    priority = 1,
                    action = ActionType.SPOTIFY_PODCAST,
                    reason = "Long drive - time for podcast"
                ))
            }
            Location.GARAGE -> {
                actions.add(SmartAction(
                    id = "log_neutral_garage",
                    title = "End Drive",
                    description = "Save statistics",
                    icon = "📊",
                    priority = 1,
                    action = ActionType.LOG_DRIVE,
                    reason = "End the drive"
                ))
            }
            Location.FOREST -> {
                actions.add(SmartAction(
                    id = "spotify_calm_forest",
                    title = "Calm Music",
                    description = "Peaceful music for relaxation",
                    icon = "🎵",
                    priority = 1,
                    action = ActionType.SPOTIFY_CALM,
                    reason = "Enjoy nature with music"
                ))
            }
        }
        
        return actions
    }
    
    /**
     * Provera da li DND treba da bude aktivan
     */
    fun shouldDNDBeActive(driverContext: DriverContext): Boolean {
        return driverContext.mood == Mood.NERVOUS
    }
}
