package com.example.riviansenseapp.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.riviansenseapp.actions.SpotifyAction

class MainViewModel : ViewModel() {
    
    private var spotifyAction: SpotifyAction? = null
    
    fun initActions(context: Context) {
        spotifyAction = SpotifyAction(context)
    }
    
    fun playSpotify() {
        spotifyAction?.playPlaylist()
    }
    
    fun playSpecificPlaylist(playlistId: String) {
        spotifyAction?.playPlaylist(playlistId)
    }
}
