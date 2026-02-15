package com.voxcom.vox.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.ClipboardManager
import android.util.Log
import android.widget.Toast
import com.voxcom.vox.data.ClipboardMemory
import com.voxcom.vox.data.repository.ClipboardRepository

class ClipboardActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        Log.d("VOX_CLIP", "Receiver triggered")

        val text = ClipboardMemory.get()

        Log.d("VOX_CLIP", "Cached text = $text")

        if (text == null) {
            Log.d("VOX_CLIP", "Nothing cached -> abort")
            return
        }

        ClipboardRepository.push(text, "phone")
        Log.d("VOX_CLIP", "Pushed to firebase")
    }
}
