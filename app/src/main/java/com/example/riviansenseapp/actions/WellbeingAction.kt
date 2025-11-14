package com.example.riviansenseapp.actions

import android.content.Context
import android.widget.Toast

class WellbeingAction(private val context: Context) {
    
    // Ova metoda će biti pozivana iz ViewModel-a da otvori BreathingScreen
    fun startBreathingExercise(durationSec: Int = 60): BreathingExerciseData {
        return BreathingExerciseData(durationSec)
    }
    
    // Ova metoda će biti pozivana da otvori MicroStretchScreen
    fun showMicroStretchPrompt(): Boolean {
        // Vraća true da signalizira da treba otvoriti stretch screen
        return true
    }
    
    // Data class za breathing exercise parametre
    data class BreathingExerciseData(
        val durationSec: Int,
        val cycleSeconds: Int = 16 // 4s inhale + 4s hold + 4s exhale + 4s hold
    )
}
