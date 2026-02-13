package com.voxcom.vox

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.content.Intent

class VoxAccessibilityService : AccessibilityService() {

    private val pressTimes = mutableListOf<Long>()

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    override fun onKeyEvent(event: KeyEvent): Boolean {


        Log.d("VOX_KEYS", "Key pressed: ${event.keyCode}")

        if (event.action == KeyEvent.ACTION_DOWN &&
            event.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {

            detectTriplePress()
            return true
        }

        return false
    }

    private fun detectTriplePress() {
        val now = System.currentTimeMillis()

        pressTimes.add(now)
        pressTimes.removeAll { now - it > 800 }

        if (pressTimes.size >= 3) {
            pressTimes.clear()
            sendWakeSignal()
        }
    }
    private fun sendWakeSignal() {

        val intent = Intent(this, VoxService::class.java)
        intent.action = VoxService.ACTION_WAKE

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
            startForegroundService(intent)
        else
            startService(intent)
    }
}
