package com.example.riviansenseapp.context

import android.content.Context

/**
 * Manager za kontekstualne akcije baziran na mood i location
 */
class ContextualActionManager(private val context: Context) {

    /**
     * Vraća listu preporučenih akcija na osnovu trenutnog konteksta
     */
    fun getSmartActions(driverContext: DriverContext): List<SmartAction> {
        val actions = mutableListOf<SmartAction>()
        
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
        
        return actions.sortedBy { it.priority }
    }
    
    private fun getNervousActions(location: Location): List<SmartAction> {
        val actions = mutableListOf<SmartAction>()
        
        // DND mode (highest priority for nervous)
        actions.add(SmartAction(
            id = "dnd_nervous",
            title = "Uključi Ne Uznemiravaj",
            description = "Blokiraj pozive i notifikacije",
            icon = "🔕",
            priority = 1,
            action = ActionType.DND_ENABLE,
            reason = "Detektovana nervoza - smanjite distrakcije"
        ))
        
        // Breathing exercise
        actions.add(SmartAction(
            id = "breathing_nervous",
            title = "Vežba Disanja",
            description = "4-minutna tehnika smirivanja",
            icon = "🫁",
            priority = 2,
            action = ActionType.BREATHING,
            reason = "Reguliši disanje da se smiriš"
        ))
        
        // Calm music
        actions.add(SmartAction(
            id = "spotify_calm",
            title = "Opuštajuća Muzika",
            description = "Pustite mirnu muziku",
            icon = "🎵",
            priority = 3,
            action = ActionType.SPOTIFY_CALM,
            reason = "Muzika pomaže pri smirivanju"
        ))
        
        // Location-specific actions
        when (location) {
            Location.CITY -> {
                // U gradu, predloži pauzu
                actions.add(SmartAction(
                    id = "coffee_nervous_city",
                    title = "Predlog: Pauza za Kafu",
                    description = "Nađi najbližu kafeteriju",
                    icon = "☕",
                    priority = 4,
                    action = ActionType.NAV_COFFEE,
                    reason = "Gradska gužva - vreme za pauzu"
                ))
            }
            Location.HIGHWAY -> {
                // Na autoputu, predloži odmorište
                actions.add(SmartAction(
                    id = "rest_nervous_highway",
                    title = "Predlog: Odmorište",
                    description = "Nađi najbliže odmorište",
                    icon = "🅿️",
                    priority = 4,
                    action = ActionType.NAV_REST_STOP,
                    reason = "Dugačka vožnja - napravi pauzu"
                ))
            }
            Location.FOREST -> {
                // U šumi, samo stretch
                actions.add(SmartAction(
                    id = "stretch_nervous_forest",
                    title = "Stretch Vežbe",
                    description = "5-minutne vežbe istezanja",
                    icon = "🤸",
                    priority = 4,
                    action = ActionType.STRETCH,
                    reason = "Iskoristi prirodu za relaksaciju"
                ))
            }
            Location.GARAGE -> {
                // U garaži, završi vožnju
                actions.add(SmartAction(
                    id = "log_nervous_garage",
                    title = "Završi Vožnju",
                    description = "Sačuvaj statistiku vožnje",
                    icon = "📊",
                    priority = 4,
                    action = ActionType.LOG_DRIVE,
                    reason = "Stigao si - vreme za odmor"
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
            title = "Energična Muzika",
            description = "Upbeat muzika za budnost",
            icon = "⚡",
            priority = 1,
            action = ActionType.SPOTIFY_ENERGETIC,
            reason = "Detektovan umor - povećaj energiju"
        ))
        
        // Stretch exercises
        actions.add(SmartAction(
            id = "stretch_tired",
            title = "Stretch Vežbe",
            description = "Protegni se za 5 minuta",
            icon = "🤸",
            priority = 2,
            action = ActionType.STRETCH,
            reason = "Aktiviraj mišiće i poboljšaj cirkulaciju"
        ))
        
        // Location-specific actions
        when (location) {
            Location.HIGHWAY -> {
                // Na autoputu, hitno predloži pauzu
                actions.add(SmartAction(
                    id = "rest_tired_highway",
                    title = "⚠️ HITNO: Odmorište",
                    description = "Nađi najbliže odmorište",
                    icon = "🛑",
                    priority = 1, // Override priority
                    action = ActionType.NAV_REST_STOP,
                    reason = "OPASNOST: Umor + autoput = visok rizik"
                ))
            }
            Location.CITY -> {
                actions.add(SmartAction(
                    id = "coffee_tired_city",
                    title = "Predlog: Pauza za Kafu",
                    description = "Kofeinska pauza",
                    icon = "☕",
                    priority = 3,
                    action = ActionType.NAV_COFFEE,
                    reason = "Osvežite se kafom"
                ))
            }
            Location.GARAGE -> {
                actions.add(SmartAction(
                    id = "log_tired_garage",
                    title = "Završi Vožnju",
                    description = "Sačuvaj statistiku i odmori se",
                    icon = "📊",
                    priority = 3,
                    action = ActionType.LOG_DRIVE,
                    reason = "Stigao si - vreme za odmor"
                ))
            }
            Location.FOREST -> {
                actions.add(SmartAction(
                    id = "breathing_tired_forest",
                    title = "Vežba Disanja na Svežem Vazduhu",
                    description = "Duboko disanje",
                    icon = "🫁",
                    priority = 3,
                    action = ActionType.BREATHING,
                    reason = "Svež vazduh + kiseonik = više energije"
                ))
            }
        }
        
        // Podcast suggestion
        actions.add(SmartAction(
            id = "podcast_tired",
            title = "Zanimljiv Podcast",
            description = "Mentalna stimulacija",
            icon = "🎙️",
            priority = 4,
            action = ActionType.SPOTIFY_PODCAST,
            reason = "Interesantan sadržaj drži pažnju"
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
                    title = "Navigacija Kući",
                    description = "Najbrža ruta",
                    icon = "🏠",
                    priority = 1,
                    action = ActionType.NAV_HOME,
                    reason = "Standardna navigacija"
                ))
            }
            Location.HIGHWAY -> {
                actions.add(SmartAction(
                    id = "podcast_neutral_highway",
                    title = "Podcast",
                    description = "Zabavan sadržaj za put",
                    icon = "🎙️",
                    priority = 1,
                    action = ActionType.SPOTIFY_PODCAST,
                    reason = "Duga vožnja - vreme za podcast"
                ))
            }
            Location.GARAGE -> {
                actions.add(SmartAction(
                    id = "log_neutral_garage",
                    title = "Završi Vožnju",
                    description = "Sačuvaj statistiku",
                    icon = "📊",
                    priority = 1,
                    action = ActionType.LOG_DRIVE,
                    reason = "Završi vožnju"
                ))
            }
            Location.FOREST -> {
                actions.add(SmartAction(
                    id = "spotify_calm_forest",
                    title = "Opuštajuća Muzika",
                    description = "Mirna muzika za relaksaciju",
                    icon = "🎵",
                    priority = 1,
                    action = ActionType.SPOTIFY_CALM,
                    reason = "Uživaj u prirodi uz muziku"
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
