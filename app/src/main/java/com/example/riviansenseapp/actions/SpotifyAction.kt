package com.example.riviansenseapp.actions

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.widget.Toast

class SpotifyAction(private val context: Context) {
    
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    
    // Playlist IDs za različite kontekste
    private val CALM_PLAYLIST_ID = "37i9dQZF1DWZd79rJ6a7lp" // Peaceful Piano
    private val ENERGETIC_PLAYLIST_ID = "37i9dQZF1DX3rxVfibe1L0" // Mood Booster
    private val DEFAULT_PLAYLIST_ID = "37i9dQZF1DXcBWIGoYBM5M" // Today's Top Hits
    
    fun playPlaylist(playlistId: String = DEFAULT_PLAYLIST_ID) {
        try {
            // Pokušaj 1: Koristimo play akciju direktno u URI-ju
            val playUri = "spotify:playlist:$playlistId:play"
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(playUri)
                setPackage("com.spotify.music")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            
            try {
                context.startActivity(intent)
                Toast.makeText(context, "Pokrećem Spotify playlist...", Toast.LENGTH_SHORT).show()
            } catch (e: ActivityNotFoundException) {
                // Pokušaj 2: Alternativni URI format
                val alternativeIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://open.spotify.com/playlist/$playlistId?play=true")
                    setPackage("com.spotify.music")
                }
                context.startActivity(alternativeIntent)
                Toast.makeText(context, "Pokrećem Spotify...", Toast.LENGTH_SHORT).show()
            }
            
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "Spotify nije instaliran", Toast.LENGTH_LONG).show()
            openSpotifyInBrowser(playlistId)
        } catch (e: Exception) {
            Toast.makeText(context, "Greška: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    fun playPodcastOrAudiobook() {
        try {
            // Otvori Spotify na podcasts sekciju
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("spotify:show:") // Generic podcast intent
                setPackage("com.spotify.music")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Greška pri otvaranju podcasta", Toast.LENGTH_SHORT).show()
        }
    }
    
    fun fadeOutMusic(durationMs: Long) {
        try {
            val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            
            // Postepeno smanjuj volumen
            android.os.Handler(android.os.Looper.getMainLooper()).post(object : Runnable {
                var step = 0
                val steps = 10
                val stepDuration = durationMs / steps
                
                override fun run() {
                    if (step < steps) {
                        val newVolume = currentVolume - (currentVolume * step / steps)
                        audioManager.setStreamVolume(
                            AudioManager.STREAM_MUSIC,
                            newVolume,
                            0
                        )
                        step++
                        android.os.Handler(android.os.Looper.getMainLooper())
                            .postDelayed(this, stepDuration)
                    }
                }
            })
        } catch (e: Exception) {
            Toast.makeText(context, "Greška pri fade out: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    fun setSpotifyVolume(volumePercent: Int) {
        try {
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val newVolume = (maxVolume * volumePercent / 100).coerceIn(0, maxVolume)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0)
        } catch (e: Exception) {
            Toast.makeText(context, "Greška pri podešavanju volumena", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun openSpotifyInBrowser(playlistId: String) {
        try {
            val webUrl = "https://open.spotify.com/playlist/$playlistId"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl))
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Instalirajte Spotify aplikaciju", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Pusti opuštajuću muziku (za nervozne vozače)
     */
    fun playCalmMusic() {
        playPlaylist(CALM_PLAYLIST_ID)
    }
    
    /**
     * Pusti energičnu muziku (za umorne vozače)
     */
    fun playEnergeticMusic() {
        playPlaylist(ENERGETIC_PLAYLIST_ID)
    }
}
