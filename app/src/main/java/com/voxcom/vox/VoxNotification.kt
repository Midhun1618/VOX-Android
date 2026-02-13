package com.voxcom.vox

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object VoxNotification {

    const val CHANNEL_ID = "vox_service_channel"

    fun createChannel(context: Context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                CHANNEL_ID,
                "VOX Assistant",
                NotificationManager.IMPORTANCE_LOW
            )

            channel.description = "Background assistant status"
            channel.setShowBadge(false)

            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
