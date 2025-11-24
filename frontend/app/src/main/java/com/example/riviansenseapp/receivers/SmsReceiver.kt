package com.example.riviansenseapp.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.SmsManager
import android.telephony.SmsMessage

class SmsReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.provider.Telephony.SMS_RECEIVED") {
            val prefs = context.getSharedPreferences("rivian_prefs", Context.MODE_PRIVATE)
            val autoReplyEnabled = prefs.getBoolean("auto_reply_enabled", false)
            
            if (!autoReplyEnabled) return
            
            val template = prefs.getString("auto_reply_template", 
                "I'm driving, I'll reply when I arrive") ?: return
            
            // Izvuci SMS poruke iz intenta
            val bundle = intent.extras ?: return
            val pdus = bundle.get("pdus") as? Array<*> ?: return
            
            val messages = mutableListOf<SmsMessage>()
            for (pdu in pdus) {
                val message = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val format = bundle.getString("format")
                    SmsMessage.createFromPdu(pdu as ByteArray, format)
                } else {
                    @Suppress("DEPRECATION")
                    SmsMessage.createFromPdu(pdu as ByteArray)
                }
                messages.add(message)
            }
            
            // Za svaku primljenu poruku, pošalji auto-reply
            messages.forEach { message ->
                val senderNumber = message.originatingAddress
                if (senderNumber != null && !wasRepliedRecently(context, senderNumber)) {
                    sendAutoReply(context, senderNumber, template)
                    markAsReplied(context, senderNumber)
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
        val prefs = context.getSharedPreferences("rivian_prefs", Context.MODE_PRIVATE)
        val lastReplyTime = prefs.getLong("last_reply_$phoneNumber", 0)
        val currentTime = System.currentTimeMillis()
        
        // Ne šalji auto-reply ako smo već odgovorili u poslednjih 30 minuta
        return (currentTime - lastReplyTime) < (30 * 60 * 1000)
    }
    
    private fun markAsReplied(context: Context, phoneNumber: String) {
        val prefs = context.getSharedPreferences("rivian_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putLong("last_reply_$phoneNumber", System.currentTimeMillis())
            .apply()
    }
}
