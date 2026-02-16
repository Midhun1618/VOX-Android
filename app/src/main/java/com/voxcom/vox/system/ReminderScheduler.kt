package com.voxcom.vox.system

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.voxcom.vox.data.model.Reminder

object ReminderScheduler {

    fun schedule(context: Context, reminder: Reminder) {

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("title", reminder.title)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        try {
            val triggerTime = reminder.time ?: return

            alarm.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } catch (e: SecurityException) {
            android.util.Log.e("VOX_REMINDER", "Exact alarm permission denied")
        }
    }
}
