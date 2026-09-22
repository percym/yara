package dev.percym.yara.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import dev.percym.yara.notification.NotificationHelper

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val event = GeofencingEvent.fromIntent(intent) ?: return

        if (event.hasError()) {
            Log.e("GeofenceReceiver", "Geofencing error: ${event.errorCode}")
            return
        }

        if (event.geofenceTransition != Geofence.GEOFENCE_TRANSITION_ENTER) return

        val triggeringGeofences = event.triggeringGeofences ?: return
        val geofenceManager = GeofenceManager(context)
        val notificationHelper = NotificationHelper(context)

        val products = triggeringGeofences
            .flatMap { geofenceManager.getProductsForGeofenceId(it.requestId) }
            .distinct()

        if (products.isNotEmpty()) {
            // Use the category display name as the store label for the notification
            val categoryName = triggeringGeofences.first().requestId.substringBefore("__")
            val storeLabel = categoryName.lowercase().replaceFirstChar { it.uppercase() }
            notificationHelper.showShoppingReminder(storeLabel, products)
        }
    }
}
