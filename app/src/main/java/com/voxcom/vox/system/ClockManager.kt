package com.voxcom.vox.system

import android.os.Handler
import android.os.Looper
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*

class ClockManager {
    private val handler = Handler(Looper.getMainLooper())

    fun start(tvTime: TextView, tvAmPm: TextView) {

        val runnable = object : Runnable {
            override fun run() {

                val now = Date()

                val timeFormat = SimpleDateFormat("hh:mm", Locale.getDefault())
                val ampmFormat = SimpleDateFormat("a", Locale.getDefault())

                tvTime.text = timeFormat.format(now)
                tvAmPm.text = ampmFormat.format(now)

                handler.postDelayed(this, 1000)
            }
        }

        handler.post(runnable)
    }

    fun stop() {
        handler.removeCallbacksAndMessages(null)
    }
}
