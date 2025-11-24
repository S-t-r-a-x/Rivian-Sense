package com.example.riviansenseapp.actions

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

class NavigationAction(private val context: Context) {
    
    enum class DestinationType {
        HOME,
        WORK,
        NEXT_EVENT,
        CUSTOM
    }
    
    enum class StopType(val displayName: String) {
        REST_STOP("Rest Stop"),
        COFFEE("Coffee Shop"),
        SCENIC_VIEWPOINT("Scenic Viewpoint"),
        GAS_STATION("Gas Station"),
        RESTAURANT("Restaurant")
    }
    
    fun openNavigationTo(
        destination: String,
        type: DestinationType = DestinationType.CUSTOM
    ) {
        try {
            val address = when (type) {
                DestinationType.HOME -> getStoredAddress("home_address", destination)
                DestinationType.WORK -> getStoredAddress("work_address", destination)
                DestinationType.NEXT_EVENT -> {
                    // TODO: Integrisati sa kalendar API
                    destination
                }
                DestinationType.CUSTOM -> destination
            }
            
            // Otvori Google Maps sa destinacijom
            val gmmIntentUri = Uri.parse("google.navigation:q=${Uri.encode(address)}&mode=d")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                setPackage("com.google.android.apps.maps")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            
            try {
                context.startActivity(mapIntent)
                Toast.makeText(
                    context,
                    "Otvaranje navigacije za: $address",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                // Ako Google Maps nije instaliran, otvori web verziju
                val webIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${Uri.encode(address)}")
                )
                context.startActivity(webIntent)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Greška pri otvaranju navigacije: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    fun saveDestination(type: String, address: String) {
        context.getSharedPreferences("rivian_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("${type}_address", address)
            .apply()
    }
    
    fun suggestBreak(stopType: StopType) {
        try {
            // Traži najbližu lokaciju zadatog tipa
            val query = when (stopType) {
                StopType.REST_STOP -> "rest area near me"
                StopType.COFFEE -> "coffee shop near me"
                StopType.SCENIC_VIEWPOINT -> "scenic viewpoint near me"
                StopType.GAS_STATION -> "gas station near me"
                StopType.RESTAURANT -> "restaurant near me"
            }
            
            val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(query)}")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                setPackage("com.google.android.apps.maps")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            
            try {
                context.startActivity(mapIntent)
                Toast.makeText(
                    context,
                    "Tražim ${stopType.displayName} u blizini...",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                // Fallback na web pretragu
                val webIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(query)}")
                )
                context.startActivity(webIntent)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Greška pri pretrazi: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun getStoredAddress(key: String, defaultValue: String): String {
        return context.getSharedPreferences("rivian_prefs", Context.MODE_PRIVATE)
            .getString(key, defaultValue) ?: defaultValue
    }
    
    fun getHomeAddress(): String? {
        return context.getSharedPreferences("rivian_prefs", Context.MODE_PRIVATE)
            .getString("home_address", null)
    }
    
    fun getWorkAddress(): String? {
        return context.getSharedPreferences("rivian_prefs", Context.MODE_PRIVATE)
            .getString("work_address", null)
    }
}
