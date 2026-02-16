package com.voxcom.vox.data

import com.voxcom.vox.data.model.Reminder

object ReminderManager {

    private val reminders = mutableListOf<Reminder>()
    private val observers = mutableListOf<() -> Unit>()

    fun update(new: List<Reminder>) {
        reminders.clear()
        reminders.addAll(new)
        notifyObservers()
    }

    fun observe(observer: () -> Unit) {
        observers.add(observer)
        observer() // immediately send current data
    }

    fun all(): List<Reminder> = reminders.toList()

    private fun notifyObservers() {
        observers.forEach { it.invoke() }
    }
}
