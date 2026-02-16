package com.voxcom.vox.data

import android.util.Log
import com.voxcom.vox.App
import com.voxcom.vox.data.model.Reminder
import com.voxcom.vox.system.ReminderScheduler

object ReminderManager {

    private val reminders = mutableListOf<Reminder>()
    private val observers = mutableListOf<() -> Unit>()

    fun update(new: List<Reminder>) {

        reminders.clear()
        reminders.addAll(new)

        // schedule alarms every sync
        val ctx = App.instance ?: return
        reminders.forEach { reminder ->
            if (reminder.time != null && reminder.time!! > System.currentTimeMillis()) {
                Log.d("VOX_REMINDER", "Scheduling: ${reminder.title}")
                ReminderScheduler.schedule(ctx, reminder)
            }
        }

        notifyObservers()
    }

    fun observe(observer: () -> Unit) {
        observers.add(observer)
        observer()
    }

    fun all(): List<Reminder> = reminders.toList()

    private fun notifyObservers() {
        observers.forEach { it.invoke() }
    }
}
