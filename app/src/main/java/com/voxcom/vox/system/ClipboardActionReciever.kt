package com.voxcom.vox.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.voxcom.vox.data.ClipboardMemory
import com.voxcom.vox.data.repository.ClipboardRepository

class ClipboardActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {

        if (intent?.action != "COPY_TO_VOX") return

        val text = ClipboardMemory.get() ?: return

        ClipboardRepository.push(text, "phone")

        Toast.makeText(context, "Sent to VOX: $text", Toast.LENGTH_SHORT).show()
    }
}
