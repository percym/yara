package dev.percym.yara.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import dev.percym.yara.R

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "shopping_reminders"
        private const val NOTIFICATION_ID = 1001
        private val VIBRATE_PATTERN = longArrayOf(0, 400, 150, 400)
    }

    init {
        createChannel()
    }

    private fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Shopping Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alerts when you're near a store with items on your list"
            enableVibration(true)
            vibrationPattern = VIBRATE_PATTERN
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    fun showShoppingReminder(storeName: String, products: List<String>) {
        vibrate()

        val summary = when (products.size) {
            1 -> products[0]
            2 -> "${products[0]} and ${products[1]}"
            else -> "${products[0]}, ${products[1]} + ${products.size - 2} more"
        }
        val detail = products.joinToString("\n") { "• $it" }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Near $storeName")
            .setContentText("Don't forget: $summary")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Items to buy:\n$detail"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setVibrate(VIBRATE_PATTERN)
            .build()

        context.getSystemService(NotificationManager::class.java)
            .notify(NOTIFICATION_ID, notification)
    }

    private fun vibrate() {
        val effect = VibrationEffect.createWaveform(VIBRATE_PATTERN, -1)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(VibratorManager::class.java).defaultVibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            (context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(effect)
        }
    }
}
