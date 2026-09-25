package dev.percym.yara.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dev.percym.yara.R
import dev.percym.yara.geofence.GeofenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ShoppingReminderService : Service() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification())
        scope.launch { reRegisterGeofences() }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    @SuppressLint("MissingPermission")
    private suspend fun reRegisterGeofences() {
        val retryDelaysMs = listOf(0L, 30_000L, 60_000L) // immediate, 30s, 60s
        for ((attempt, delayMs) in retryDelaysMs.withIndex()) {
            if (delayMs > 0) delay(delayMs)
            try {
                val cts = CancellationTokenSource()
                val location = LocationServices.getFusedLocationProviderClient(this)
                    .getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
                    .await()
                if (location != null) {
                    GeofenceManager(this).refreshGeofencesFromCache(location.latitude, location.longitude)
                    Log.d(TAG, "Geofences re-registered (attempt ${attempt + 1})")
                    return
                }
            } catch (e: Exception) {
                Log.w(TAG, "Attempt ${attempt + 1} failed: ${e.message}")
            }
        }
        Log.e(TAG, "Failed to re-register geofences after all retries")
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

        fun start(context: Context) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, ShoppingReminderService::class.java)
            )
        }
    }
}
