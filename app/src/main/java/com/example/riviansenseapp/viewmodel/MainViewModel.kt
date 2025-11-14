package com.example.riviansenseapp.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.riviansenseapp.actions.*

class MainViewModel : ViewModel() {
    
    private var spotifyAction: SpotifyAction? = null
    private var audioAction: AudioAction? = null
    private var phoneAction: PhoneAction? = null
    private var navigationAction: NavigationAction? = null
    private var wellbeingAction: WellbeingAction? = null
    private var loggingAction: LoggingAction? = null
    
    fun initActions(context: Context) {
        spotifyAction = SpotifyAction(context)
        audioAction = AudioAction(context)
        phoneAction = PhoneAction(context)
        navigationAction = NavigationAction(context)
        wellbeingAction = WellbeingAction(context)
        loggingAction = LoggingAction(context)
    }
    
    // Spotify Actions
    fun playSpotify() {
        spotifyAction?.playPlaylist()
    }
    
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
    
    // Audio Actions
    fun playNatureSoundscape(type: AudioAction.NatureSoundType) {
        audioAction?.playNatureSoundscape(type)
    }
    
    fun stopNatureSoundscape() {
        audioAction?.stopNatureSoundscape()
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
    fun createPostDriveReminder(text: String) {
        loggingAction?.createPostDriveReminder(text)
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
    
    override fun onCleared() {
        super.onCleared()
        audioAction?.stopNatureSoundscape()
    }
}
