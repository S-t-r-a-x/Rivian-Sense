package com.example.riviansenseapp.actions

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

class SpotifyAction(private val context: Context) {
    
    fun playPlaylist(playlistId: String = "37i9dQZF1DXcBWIGoYBM5M") {
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
    
    private fun openSpotifyInBrowser(playlistId: String) {
        try {
            // Konvertuj Spotify URI u web link
            val webUrl = "https://open.spotify.com/playlist/$playlistId"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl))
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Instalirajte Spotify aplikaciju", Toast.LENGTH_SHORT).show()
        }
    }
}
