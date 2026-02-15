package com.voxcom.vox.system

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.voxcom.vox.R

object ClipboardNotification {

    private const val ID = 222

    fun show(context: Context) {

        val intent = Intent(context, ClipboardActionReceiver::class.java)
        intent.action = "COPY_TO_VOX"

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            101,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, VoxNotification.CLIPBOARD_CHANNEL)
            .setSmallIcon(R.drawable.ic_stat_vox)
            .setContentTitle("Text copied")
            .setContentText("Send copied text to VOX?")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(0, "Copy to VOX", pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(ID, notification)
    }
}
