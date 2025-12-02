package com.example.drivesenseapp.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.SmsManager
import android.telephony.TelephonyManager

class MissedCallReceiver : BroadcastReceiver() {
    
    companion object {
        private var lastCallState = TelephonyManager.CALL_STATE_IDLE
        private var isIncomingCall = false
        private var incomingNumber: String? = null
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val prefs = context.getSharedPreferences("drive_prefs", Context.MODE_PRIVATE)
            val autoReplyEnabled = prefs.getBoolean("auto_reply_enabled", false)
            
            if (!autoReplyEnabled) return
            
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            val phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
            
            when (state) {
                TelephonyManager.EXTRA_STATE_RINGING -> {
                    // Dolazni poziv zvoni
                    isIncomingCall = true
                    incomingNumber = phoneNumber
                    lastCallState = TelephonyManager.CALL_STATE_RINGING
                }
                
                TelephonyManager.EXTRA_STATE_IDLE -> {
                    // Poziv je završen
                    if (lastCallState == TelephonyManager.CALL_STATE_RINGING && isIncomingCall) {
                        // Propušten poziv - nije odgovoren
                        incomingNumber?.let { number ->
                            if (!wasRepliedRecently(context, number)) {
                                val template = prefs.getString(
                                    "auto_reply_template",
                                    "I'm driving, I'll call you back when I arrive"
                                ) ?: return
                                
                                sendAutoReply(context, number, template)
                                markAsReplied(context, number)
                            }
                        }
                    }
                    
                    // Reset state
                    isIncomingCall = false
                    incomingNumber = null
                    lastCallState = TelephonyManager.CALL_STATE_IDLE
                }
                
                TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                    // Poziv je odgovoren
                    isIncomingCall = false // Označi da je poziv odgovoren
                    lastCallState = TelephonyManager.CALL_STATE_OFFHOOK
                }
            }
        }
    }
    
    private fun sendAutoReply(context: Context, phoneNumber: String, message: String) {
        try {
            val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }
            
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
        } catch (e: Exception) {
            // Silent fail
        }
    }
    
    private fun wasRepliedRecently(context: Context, phoneNumber: String): Boolean {
        val prefs = context.getSharedPreferences("drive_prefs", Context.MODE_PRIVATE)
        val lastReplyTime = prefs.getLong("last_call_reply_$phoneNumber", 0)
        val currentTime = System.currentTimeMillis()
        
        // Ne šalji auto-reply ako smo već odgovorili u poslednjih 30 minuta
        return (currentTime - lastReplyTime) < (30 * 60 * 1000)
    }
    
    private fun markAsReplied(context: Context, phoneNumber: String) {
        val prefs = context.getSharedPreferences("drive_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putLong("last_call_reply_$phoneNumber", System.currentTimeMillis())
            .apply()
    }
}
