package com.voxcom.vox

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class VoxService : Service() {

    companion object {
        const val ACTION_WAKE = "VOX_WAKE"
        const val ACTION_START = "VOX_START"
    }

    private val CHANNEL_ID = "vox_service_channel"

    override fun onCreate() {
        super.onCreate()

        if (!VoxPrefs.isEnabled(this)) {
            stopSelf()
            return
        }

        startForeground(1, buildNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (intent?.action == "VOX_WAKE") {
            startListeningMode()
        }

        return START_STICKY
    }


    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(): Notification {

        val openIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("VOX Assistant Active")
            .setContentText("Listening for trigger")
            .setSmallIcon(R.drawable.ic_stat_vox)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
    }

    private fun playWakeTone() {
        val mp = MediaPlayer.create(this, R.raw.waketone)
        mp.setOnCompletionListener { it.release() }
        mp.start()
    }
    private fun startListeningMode() {
        playWakeTone()

        android.util.Log.d("VOX", "Assistant Activated")
    }

}
