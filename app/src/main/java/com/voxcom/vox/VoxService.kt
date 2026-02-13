package com.voxcom.vox

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.VolumeProvider
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Build
import android.os.IBinder
import android.view.KeyEvent
import androidx.core.app.NotificationCompat

class VoxService : Service() {

    private val CHANNEL_ID = "vox_service_channel"
    private lateinit var mediaSession: MediaSession
    private val pressTimes = mutableListOf<Long>()


    override fun onCreate() {
        super.onCreate()

        setupVolumeInterceptor()


        if (!VoxPrefs.isEnabled(this)) {
            stopSelf()
            return
        }

        val notification = buildNotification()

        // IMPORTANT: call immediately
        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
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
            .setContentText("Waiting for trigger")
            .setSmallIcon(R.mipmap.ic_launcher) // MUST be app icon
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
    }
    private fun detectTriplePress() {
        val now = System.currentTimeMillis()

        pressTimes.add(now)
        pressTimes.removeAll { now - it > 800 }

        if (pressTimes.size >= 3) {
            pressTimes.clear()
            onVoxTriggered()
        }
    }

    private fun onVoxTriggered() {
        playWakeTone()
    }
    private fun playWakeTone() {
        val mp = MediaPlayer.create(this, R.raw.waketone)
        mp.setOnCompletionListener { it.release() }
        mp.start()
    }
    private fun setupVolumeInterceptor() {

        mediaSession = MediaSession(this, "VOX_SESSION")

        val volumeProvider = object : VolumeProvider(
            VolumeProvider.VOLUME_CONTROL_RELATIVE,
            100,
            50
        ) {
            override fun onAdjustVolume(direction: Int) {

                if (direction < 0) { // volume down
                    detectTriplePress()
                }
            }
        }

        mediaSession.setPlaybackToRemote(volumeProvider)
        mediaSession.isActive = true
    }


}
