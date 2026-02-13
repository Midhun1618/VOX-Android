package com.voxcom.vox.data.model

import com.google.firebase.Timestamp

data class Task(
    val id: String = "",
    val title: String = "",
    val completed: Boolean = false,
    val expiresAt: Timestamp? = null
)