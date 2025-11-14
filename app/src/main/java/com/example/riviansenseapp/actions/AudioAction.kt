package com.example.riviansenseapp.actions

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.widget.Toast
import com.example.riviansenseapp.R

class AudioAction(private val context: Context) {
    
    private var mediaPlayer: MediaPlayer? = null
    
    enum class NatureSoundType(val fileName: String) {
        FOREST("forest_ambience"),
        RAIN("rain_sounds"),
        OCEAN("ocean_waves"),
        THUNDER("thunder_storm")
    }
    
    fun playNatureSoundscape(type: NatureSoundType) {
        try {
            // Zaustavi trenutnu muziku ako je puštena
            stopNatureSoundscape()
            
            // Za sada koristimo online resurse, kasnije možeš dodati lokalne u res/raw
            val soundUrl = when (type) {
                NatureSoundType.FOREST -> "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
                NatureSoundType.RAIN -> "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3"
                NatureSoundType.OCEAN -> "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3"
                NatureSoundType.THUNDER -> "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3"
            }
            
            mediaPlayer = MediaPlayer().apply {
                setDataSource(soundUrl)
                setAudioStreamType(AudioManager.STREAM_MUSIC)
                isLooping = true
                prepareAsync()
                setOnPreparedListener {
                    it.start()
                    Toast.makeText(context, "Puštam ${type.name.lowercase()} zvukove...", Toast.LENGTH_SHORT).show()
                }
                setOnErrorListener { _, _, _ ->
                    Toast.makeText(context, "Greška pri puštanju zvukova", Toast.LENGTH_SHORT).show()
                    true
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Greška: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    fun stopNatureSoundscape() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
        }
        mediaPlayer = null
    }
    
    fun isPlayingNatureSounds(): Boolean {
        return mediaPlayer?.isPlaying ?: false
    }
}
