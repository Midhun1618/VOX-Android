package com.voxcom.vox.data.model

data class Reminder(
    val id: String = "",
    val title: String = "",
    val time: Long = 0L,
    val createdBy: String = ""
)
