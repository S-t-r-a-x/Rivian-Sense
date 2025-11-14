package com.example.riviansenseapp.actions

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.telephony.SmsManager
import android.widget.Toast
import androidx.core.app.ActivityCompat

class PhoneAction(private val context: Context) {
    
    private val notificationManager = 
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    enum class DNDMode {
        SOFT,   // Priority notifications only
        MAX     // Complete silence
    }
    
    fun enableDrivingDND(mode: DNDMode = DNDMode.SOFT) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!notificationManager.isNotificationPolicyAccessGranted) {
                    // Zatraži dozvolu za DND
                    val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(intent)
                    Toast.makeText(context, "Molim vas omogućite DND pristup", Toast.LENGTH_LONG).show()
                    return
                }
                
                val interruptionFilter = when (mode) {
                    DNDMode.SOFT -> NotificationManager.INTERRUPTION_FILTER_PRIORITY
                    DNDMode.MAX -> NotificationManager.INTERRUPTION_FILTER_ALARMS
                }
                
                notificationManager.setInterruptionFilter(interruptionFilter)
                
                // Sačuvaj u SharedPreferences da je DND aktivan
                context.getSharedPreferences("rivian_prefs", Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean("dnd_enabled", true)
                    .putString("dnd_mode", mode.name)
                    .apply()
                
                Toast.makeText(
                    context, 
                    "Driving mode (${mode.name}) aktiviran", 
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Greška pri aktivaciji DND: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    fun disableDrivingDND() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (notificationManager.isNotificationPolicyAccessGranted) {
                    notificationManager.setInterruptionFilter(
                        NotificationManager.INTERRUPTION_FILTER_ALL
                    )
                }
                
                // Ukloni iz SharedPreferences
                context.getSharedPreferences("rivian_prefs", Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean("dnd_enabled", false)
                    .apply()
                
                Toast.makeText(context, "Driving mode deaktiviran", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Greška pri deaktivaciji DND: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    fun isDNDEnabled(): Boolean {
        return context.getSharedPreferences("rivian_prefs", Context.MODE_PRIVATE)
            .getBoolean("dnd_enabled", false)
    }
    
    fun autoReplyToMessages(template: String = "I'm driving, I'll call you back when I arrive") {
        try {
            // Proveri dozvole za SMS i pozive
            val hasSendPermission = ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.SEND_SMS
            ) == PackageManager.PERMISSION_GRANTED
            
            val hasReceivePermission = ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.RECEIVE_SMS
            ) == PackageManager.PERMISSION_GRANTED
            
            val hasPhoneStatePermission = ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED
            
            if (!hasSendPermission || !hasReceivePermission || !hasPhoneStatePermission) {
                Toast.makeText(
                    context,
                    "Potrebne dozvole: SEND_SMS, RECEIVE_SMS, READ_PHONE_STATE",
                    Toast.LENGTH_LONG
                ).show()
                return
            }
            
            // Sačuvaj template u SharedPreferences
            context.getSharedPreferences("rivian_prefs", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("auto_reply_enabled", true)
                .putString("auto_reply_template", template)
                .apply()
            
            Toast.makeText(
                context,
                "Auto-reply aktiviran! 📱\n• Odgovara na SMS\n• Odgovara na propuštene pozive",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Greška pri postavljanju auto-reply: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    fun disableAutoReply() {
        context.getSharedPreferences("rivian_prefs", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("auto_reply_enabled", false)
            .apply()
        
        Toast.makeText(context, "Auto-reply isključen", Toast.LENGTH_SHORT).show()
    }
    
    fun isAutoReplyEnabled(): Boolean {
        return context.getSharedPreferences("rivian_prefs", Context.MODE_PRIVATE)
            .getBoolean("auto_reply_enabled", false)
    }
    
    // Helper metoda za slanje SMS (koristi se u BroadcastReceiver-u)
    fun sendSMS(phoneNumber: String, message: String) {
        try {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.SEND_SMS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    context.getSystemService(SmsManager::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    SmsManager.getDefault()
                }
                
                smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Greška pri slanju SMS: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
