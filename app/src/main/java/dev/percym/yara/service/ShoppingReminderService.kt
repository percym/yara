package dev.percym.yara.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import dev.percym.yara.R
import dev.percym.yara.geofence.GeofenceManager
import dev.percym.yara.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.time.LocalTime

class ShoppingReminderService : Service() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_BALANCED_POWER_ACCURACY,
        POLL_INTERVAL_MS
    ).setMinUpdateIntervalMillis(MIN_INTERVAL_MS).build()

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { location ->
                scope.launch { checkProximity(location) }
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification())
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest, locationCallback, Looper.getMainLooper()
            )
            Log.d(TAG, "Location updates started — checking every ${POLL_INTERVAL_MS / 1000}s")
        } catch (e: SecurityException) {
            Log.e(TAG, "No location permission", e)
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::fusedLocationClient.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
        scope.cancel()
    }

    private suspend fun checkProximity(location: Location) {
        val hour = LocalTime.now().hour
        if (hour < ALERT_START_HOUR || hour >= ALERT_END_HOUR) return

        val gm = GeofenceManager(this)
        val shops = gm.getShopsWithTrackedProducts()

        if (shops.isEmpty()) {
            gm.refreshGeofencesFromCache(location.latitude, location.longitude)
            return
        }

        val distOut = FloatArray(1)
        val now = System.currentTimeMillis()
        val alertPrefs = getSharedPreferences("yara_alerts", Context.MODE_PRIVATE)

        for (shop in shops) {
            Location.distanceBetween(
                location.latitude, location.longitude,
                shop.lat, shop.lng, distOut
            )
            if (distOut[0] > ALERT_RADIUS_METERS) continue

            val lastAlerted = alertPrefs.getLong(shop.id, 0L)
            if (now - lastAlerted < ALERT_COOLDOWN_MS) continue

            val products = gm.getProductsForCategory(shop.categoryName)
            if (products.isEmpty()) continue

            alertPrefs.edit().putLong(shop.id, now).apply()
            NotificationHelper(this).showShoppingReminder(shop.name, products)
            Log.d(TAG, "Alert fired: ${shop.name} at ${distOut[0].toInt()}m")
        }
    }

    private fun buildNotification(): Notification {
        val nm = getSystemService(NotificationManager::class.java)
        if (nm.getNotificationChannel(CHANNEL_ID) == null) {
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Shopping Monitor", NotificationManager.IMPORTANCE_LOW)
                    .apply { setShowBadge(false) }
            )
        }
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("YaRA")
            .setContentText("Watching for nearby shops")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    companion object {
        private const val TAG = "ShoppingService"
        private const val CHANNEL_ID = "yara_service"
        private const val NOTIFICATION_ID = 2
        private const val POLL_INTERVAL_MS = 15 * 60 * 1000L   // check every 15 min
        private const val MIN_INTERVAL_MS = 10 * 60 * 1000L    // accept at most every 10 min
        private const val ALERT_RADIUS_METERS = 300f
        private const val ALERT_COOLDOWN_MS = 45 * 60 * 1000L  // don't re-alert same shop for 45 min
        private const val ALERT_START_HOUR = 8                  // quiet before 8am
        private const val ALERT_END_HOUR = 22                   // quiet after 10pm

        fun start(context: Context) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, ShoppingReminderService::class.java)
            )
        }
    }
}
