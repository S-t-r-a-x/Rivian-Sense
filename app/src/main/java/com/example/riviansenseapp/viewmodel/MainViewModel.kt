package com.example.riviansenseapp.viewmodel

import android.content.Context
import android.util.Log // --- NEW IMPORT ---
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.riviansenseapp.actions.*
import kotlinx.coroutines.flow.MutableStateFlow // --- NEW IMPORT ---
import kotlinx.coroutines.flow.asStateFlow // --- NEW IMPORT ---
import com.example.riviansenseapp.api.DriverContextApi
import com.example.riviansenseapp.context.*
import com.example.riviansenseapp.stats.StatsManager
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

class MainViewModel : ViewModel() {

    // --- NEW: StateFlow for Debug UI ---
    private val _driverState = MutableStateFlow("disconnected")
    val driverState = _driverState.asStateFlow() // Expose this to the UI in MainActivity

    // --- Existing Action Properties ---
    private var spotifyAction: SpotifyAction? = null
    private var phoneAction: PhoneAction? = null
    private var navigationAction: NavigationAction? = null
    private var wellbeingAction: WellbeingAction? = null
    private var loggingAction: LoggingAction? = null

    private var contextApi: DriverContextApi? = null
    private var contextualActionManager: ContextualActionManager? = null
    private var statsManager: StatsManager? = null
    private var periodicStatsUpdateJob: Job? = null

    // StateFlow za trenutni kontekst
    private val _currentContext = MutableStateFlow(DriverContext(Mood.NEUTRAL, Location.CITY))
    val currentContext: StateFlow<DriverContext> = _currentContext

    // StateFlow za smart actions
    private val _smartActions = MutableStateFlow<List<SmartAction>>(emptyList())
    val smartActions: StateFlow<List<SmartAction>> = _smartActions

    fun initActions(context: Context) {
        spotifyAction = SpotifyAction(context)
        phoneAction = PhoneAction(context)
        navigationAction = NavigationAction(context)
        wellbeingAction = WellbeingAction(context)
        loggingAction = LoggingAction(context)

        contextApi = DriverContextApi(context)
        contextualActionManager = ContextualActionManager(context)
        statsManager = StatsManager(context)
        
        // Učitaj početni kontekst
        refreshContext()
        
        // Počni tracking statistike
        statsManager?.startDriveSession(_currentContext.value)
        
        // Pokreni periodični update statistike (na svakih 10 sekundi)
        startPeriodicStatsUpdate()
        
        // Pokreni WebSocket listener za real-time updates
        startWebSocketListener()
    }
    
    /**
     * Pokreće WebSocket listener za real-time kontekst updates
     */
    private fun startWebSocketListener() {
        contextApi?.startContextMonitoring { newContext ->
            viewModelScope.launch {
                _currentContext.value = newContext
                
                // Update statistiku
                statsManager?.updateContext(newContext)
                
                android.util.Log.d("MainViewModel", "🔄 Context updated: ${newContext.mood} @ ${newContext.location}")
                
                // Auto-manage DND based on mood
                contextualActionManager?.let { manager ->
                    if (manager.shouldDNDBeActive(newContext)) {
                        enableDrivingDND(PhoneAction.DNDMode.SOFT)
                    } else {
                        disableDrivingDND()
                    }
                }
                
                // Update smart actions
                updateSmartActions()
            }
        }
    }
    
    /**
     * Pokreni periodični update statistike na svakih 10 sekundi
     * Ovo omogućava da se vreme prati i kada se kontekst NE menja
     */
    private fun startPeriodicStatsUpdate() {
        periodicStatsUpdateJob?.cancel()
        periodicStatsUpdateJob = viewModelScope.launch {
            while (isActive) {
                delay(10000) // 10 sekundi
                
                // Pozovi updateContext sa trenutnim kontekstom da bi se izračunalo vreme
                statsManager?.updateContext(_currentContext.value)
                
                Log.d("MainViewModel", "🔄 Periodic stats update (10s)")
            }
        }
    }
    
    /**
     * Zatvara WebSocket konekciju
     */
    fun stopWebSocketListener() {
        contextApi?.stopContextMonitoring()
    }    /**
     * Osvežava kontekst sa API-ja i ažurira smart actions
     */
    fun refreshContext() {
        viewModelScope.launch {
            val context = contextApi?.getCurrentContext() ?: DriverContext(Mood.NEUTRAL, Location.CITY)
            _currentContext.value = context

            // Auto-manage DND based on mood
            contextualActionManager?.let { manager ->
                if (manager.shouldDNDBeActive(context)) {
                    enableDrivingDND(PhoneAction.DNDMode.SOFT)
                } else {
                    disableDrivingDND()
                }
            }

            // Update smart actions
            updateSmartActions()
        }
    }

    /**
     * Ažurira smart actions na osnovu trenutnog konteksta
     */
    private fun updateSmartActions() {
        contextualActionManager?.let { manager ->
            val actions = manager.getSmartActions(_currentContext.value)
            _smartActions.value = actions
        }
    }
    
    /**
     * Javna funkcija za osvežavanje smart actions
     * Poziva se kada se vrati sa Settings ekrana
     */
    fun refreshSmartActions() {
        Log.d("MainViewModel", "🔄 Refreshing smart actions after Settings change...")
        updateSmartActions()
    }

    /**
     * Mock funkcija za postavljanje konteksta (za testiranje)
     */
    fun setMockContext(mood: Mood, location: Location) {
        contextApi?.setMockContext(mood, location)
        refreshContext()
    }

    // Spotify Actions
    fun playSpotify() {
        spotifyAction?.playPlaylist()
    }

    // ... (rest of your existing functions) ...

    fun playSpecificPlaylist(playlistId: String) {
        spotifyAction?.playPlaylist(playlistId)
    }

    fun playPodcastOrAudiobook() {
        spotifyAction?.playPodcastOrAudiobook()
    }

    fun fadeOutMusic(durationMs: Long = 3000) {
        spotifyAction?.fadeOutMusic(durationMs)
    }

    fun setSpotifyVolume(volumePercent: Int) {
        spotifyAction?.setSpotifyVolume(volumePercent)
    }
    
    fun playCalmMusic() {
        spotifyAction?.playCalmMusic()
    }
    
    fun playEnergeticMusic() {
        spotifyAction?.playEnergeticMusic()
    }

    // Phone Actions
    fun enableDrivingDND(mode: PhoneAction.DNDMode = PhoneAction.DNDMode.SOFT) {
        phoneAction?.enableDrivingDND(mode)
    }

    fun disableDrivingDND() {
        phoneAction?.disableDrivingDND()
    }

    fun autoReplyToMessages(template: String = "I'm driving, I'll reply when I arrive") {
        phoneAction?.autoReplyToMessages(template)
    }

    fun disableAutoReply() {
        phoneAction?.disableAutoReply()
    }

    // Navigation Actions
    fun openNavigationTo(
        destination: String,
        type: NavigationAction.DestinationType = NavigationAction.DestinationType.CUSTOM
    ) {
        navigationAction?.openNavigationTo(destination, type)
    }

    fun suggestBreak(stopType: NavigationAction.StopType) {
        navigationAction?.suggestBreak(stopType)
    }

    fun saveDestination(type: String, address: String) {
        navigationAction?.saveDestination(type, address)
    }

    // Wellbeing Actions
    fun startBreathingExercise(durationSec: Int = 60): WellbeingAction.BreathingExerciseData? {
        return wellbeingAction?.startBreathingExercise(durationSec)
    }

    fun showMicroStretchPrompt(): Boolean {
        return wellbeingAction?.showMicroStretchPrompt() ?: false
    }

    // Logging Actions
    fun createPostDriveReminder(text: String): Boolean {
        return loggingAction?.createPostDriveReminder(text) ?: false
    }

    fun createMissedCallReminder(callerName: String, phoneNumber: String): Boolean {
        return loggingAction?.createMissedCallReminder(callerName, phoneNumber) ?: false
    }

    fun completeReminder(id: String) {
        loggingAction?.completeReminder(id)
    }

    fun getAllReminders(): List<LoggingAction.Reminder> {
        return loggingAction?.getAllReminders() ?: emptyList()
    }

    fun getActiveReminders(): List<LoggingAction.Reminder> {
        return loggingAction?.getActiveReminders() ?: emptyList()
    }

    fun printAllReminders() {
        loggingAction?.printAllReminders()
    }

    fun clearAllReminders() {
        loggingAction?.clearAllReminders()
    }

    fun logDriveSummary(
        dominantMood: LoggingAction.Mood,
        dominantScenery: LoggingAction.Scenery,
        keyActions: List<String> = emptyList(),
        moodStats: Map<LoggingAction.Mood, Int> = emptyMap()
    ) {
        loggingAction?.logDriveSummary(dominantMood, dominantScenery, keyActions, moodStats)
    }

    fun startDrive() {
        loggingAction?.startDrive()
    }

    fun playSoftChimeForAggressivePattern() {
        loggingAction?.playSoftChimeForAggressivePattern()
    }

    fun incrementCalmDriveStreak() {
        loggingAction?.incrementCalmDriveStreak()
    }

    fun getCalmDriveStreak(): Int {
        return loggingAction?.getCalmDriveStreak() ?: 0
    }

    fun showStreakCard(): LoggingAction.StreakData? {
        return loggingAction?.showStreakCard()
    }

    fun getDriveSummaries(): List<LoggingAction.DriveSummary> {
        return loggingAction?.getDriveSummaries() ?: emptyList()
    }

    fun getReminders(): List<LoggingAction.Reminder> {
        return loggingAction?.getReminders() ?: emptyList()
    }

    /**
     * Izvršava smart action na osnovu ActionType-a
     */
    fun executeSmartAction(actionType: ActionType) {
        when (actionType) {
            ActionType.SPOTIFY_CALM -> playCalmMusic()
            ActionType.SPOTIFY_ENERGETIC -> playEnergeticMusic()
            ActionType.SPOTIFY_PODCAST -> playPodcastOrAudiobook()
            ActionType.DND_ENABLE -> enableDrivingDND()
            ActionType.DND_DISABLE -> disableDrivingDND()
            ActionType.NAV_HOME -> openNavigationTo("", NavigationAction.DestinationType.HOME)
            ActionType.NAV_REST_STOP -> suggestBreak(NavigationAction.StopType.REST_STOP)
            ActionType.NAV_COFFEE -> suggestBreak(NavigationAction.StopType.COFFEE)
            ActionType.BREATHING -> {} // Will navigate to breathing screen
            ActionType.STRETCH -> {} // Will navigate to stretch screen
            ActionType.LOG_DRIVE -> logDriveSummary(
                LoggingAction.Mood.STRESSED,
                LoggingAction.Scenery.CITY
            )
            ActionType.CREATE_REMINDER -> {} // Will open reminder dialog
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        
        // Zaustavi periodični update
        periodicStatsUpdateJob?.cancel()
        
        // Završi drive session i sačuvaj statistiku
        val driveDuration = statsManager?.endDriveSession() ?: 0L
        Log.d("MainViewModel", "🏁 Drive session ended: ${driveDuration}s")
        
        stopWebSocketListener()
    }
    
    /**
     * Vraća StatsManager za pristup statistici i badge-ovima
     */
    fun getStatsManager(): StatsManager? = statsManager
}
