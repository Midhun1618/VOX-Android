package com.voxcom.vox.data.model

import com.google.firebase.Timestamp

data class Task(
    val id: String,
    val title: String,
    val completed: Boolean,
    val expiresAt: Timestamp?
) {
    fun state(now: Long = System.currentTimeMillis()): TaskState {

        if (completed) return TaskState.COMPLETED

        val expiry = expiresAt?.toDate()?.time ?: return TaskState.ACTIVE

        return if (now >= expiry)
            TaskState.MISSED
        else
            TaskState.ACTIVE
    }
}
