package com.example.drivesenseapp.actions

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.view.KeyEvent
import android.widget.Toast

class SpotifyAction(private val context: Context) {
    
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    
    // Playlist IDs za različite kontekste
    private val CALM_PLAYLIST_ID = "37i9dQZF1DWZd79rJ6a7lp" // Peaceful Piano
    private val ENERGETIC_PLAYLIST_ID = "37i9dQZF1DX3rxVfibe1L0" // Mood Booster
    private val DEFAULT_PLAYLIST_ID = "37i9dQZF1DXcBWIGoYBM5M" // Today's Top Hits
    
    fun playPlaylist(playlistId: String = DEFAULT_PLAYLIST_ID) {
        try {
            // Strategija sa više pokušaja za pouzdano puštanje
            
            // Pokušaj 1: Spotify URI sa play action
            val spotifyUri = "spotify:playlist:$playlistId"
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(spotifyUri)
                setPackage("com.spotify.music")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            
            try {
                context.startActivity(intent)
                
                // Pošalji broadcast da zapocne playback nakon otvaranja
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    sendSpotifyPlayBroadcast()
                }, 500) // Čekaj 500ms da se Spotify otvori
                
                Toast.makeText(context, "Opening Spotify playlist...", Toast.LENGTH_SHORT).show()
                
            } catch (e: ActivityNotFoundException) {
                // Pokušaj 2: Media player intent
                val mediaIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://open.spotify.com/playlist/$playlistId")
                    setPackage("com.spotify.music")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                
                try {
                    context.startActivity(mediaIntent)
                    
                    // Pošalji play komandu
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        sendSpotifyPlayBroadcast()
                    }, 800)
                    
                    Toast.makeText(context, "Starting Spotify...", Toast.LENGTH_SHORT).show()
                } catch (e2: Exception) {
                    // Pokušaj 3: Otvori u browser-u kao poslednji resort
                    openSpotifyInBrowser(playlistId)
                }
            }
            
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "Spotify not installed", Toast.LENGTH_LONG).show()
            openSpotifyInBrowser(playlistId)
        } catch (e: Exception) {
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Šalje broadcast intent za play komandu Spotify-u
     * Koristi više metoda za maksimalnu kompatibilnost
     */
    private fun sendSpotifyPlayBroadcast() {
        try {
            // Metod 1: Spotify specifični widget broadcast
            val spotifyIntent = Intent("com.spotify.mobile.android.ui.widget.PLAY").apply {
                setPackage("com.spotify.music")
            }
            context.sendBroadcast(spotifyIntent)
            
            // Metod 2: Media button event (univerzalniji pristup)
            val keyEvent = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY)
            val mediaIntent = Intent(Intent.ACTION_MEDIA_BUTTON).apply {
                putExtra(Intent.EXTRA_KEY_EVENT, keyEvent)
                setPackage("com.spotify.music")
            }
            context.sendBroadcast(mediaIntent)
            
            // Metod 3: Audio focus request (ponekad pomaže)
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.requestAudioFocus(
                null,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
            
        } catch (e: Exception) {
            // Ignoriši greške - ovo su best-effort pokušaji
        }
    }
    
    fun playPodcastOrAudiobook() {
        try {
            // Lista popularnih podcast-a za vožnju (različiti žanrovi)
            val podcastShows = listOf(
                "4rOoJ6Egrf8K2IrywzwOMk", // The Joe Rogan Experience (razgovori)
                "5CnDmMUG0S5bSSw612fs8C", // Huberman Lab (nauka)
                "2MAi0BvDc6GTFvKFPXnkCL", // The Daily (vesti NY Times)
                "4AlxqGkkrqe0mfIx3Mi7Uh", // Serial (true crime)
                "5AvwZVawapvyhJUIx8oTnX", // Call Her Daddy (lifestyle)
                "0ofXAdFIQQRsCYj9754UFx"  // TED Talks Daily (edukacija)
            )
            
            // Izaberi random podcast da bude svaki put drugačiji
            val randomPodcast = podcastShows.random()
            
            // Pokušaj 1: Direktan Spotify URI
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("spotify:show:$randomPodcast")
                setPackage("com.spotify.music")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            
            try {
                context.startActivity(intent)
                
                // Pošalji play komandu nakon otvaranja
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    sendSpotifyPlayBroadcast()
                }, 800)
                
                Toast.makeText(context, "Opening podcast on Spotify...", Toast.LENGTH_SHORT).show()
            } catch (e: ActivityNotFoundException) {
                // Pokušaj 2: Web link
                val webIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://open.spotify.com/show/$randomPodcast")
                    setPackage("com.spotify.music")
                }
                context.startActivity(webIntent)
                
                // Pošalji play komandu
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    sendSpotifyPlayBroadcast()
                }, 1000)
                
                Toast.makeText(context, "Opening Spotify...", Toast.LENGTH_SHORT).show()
            }
            
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "Spotify not installed", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Error opening podcast: ${e.message}", Toast.LENGTH_SHORT).show()
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
