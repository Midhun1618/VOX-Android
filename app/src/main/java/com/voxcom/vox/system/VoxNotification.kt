package com.voxcom.vox.system

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object VoxNotification {

    const val SERVICE_CHANNEL = "vox_service_channel"
    const val CLIPBOARD_CHANNEL = "vox_clipboard_channel"

    fun createChannel(context: Context) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = context.getSystemService(NotificationManager::class.java)

        // Foreground assistant channel (silent)
        val serviceChannel = NotificationChannel(
            SERVICE_CHANNEL,
            "VOX Assistant",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Background assistant status"
            setShowBadge(false)
        }

        // Clipboard alert channel (popup notification)
        val clipboardChannel = NotificationChannel(
            CLIPBOARD_CHANNEL,
            "Clipboard Transfer",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Send copied text to VOX"
        }

        manager.createNotificationChannel(serviceChannel)
        manager.createNotificationChannel(clipboardChannel)
    }
}
