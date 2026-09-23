package dev.percym.yara.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dev.percym.yara.service.ShoppingReminderService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            ShoppingReminderService.start(context)
        }
    }
}
