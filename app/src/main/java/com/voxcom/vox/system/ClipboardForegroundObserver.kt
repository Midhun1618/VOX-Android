package com.voxcom.vox.system

import android.content.ClipboardManager
import android.content.Context
import com.voxcom.vox.data.ClipboardMemory

class ClipboardForegroundObserver(private val context: Context) {

    private val clipboard =
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    private var lastText: String? = null

    fun start() {
        clipboard.addPrimaryClipChangedListener {

            val text = clipboard.primaryClip
                ?.getItemAt(0)
                ?.coerceToText(context)
                ?.toString()
                ?: return@addPrimaryClipChangedListener

            if (text == lastText) return@addPrimaryClipChangedListener
            lastText = text

            ClipboardMemory.save(text)

            ClipboardNotification.show(context)

        }
    }

}
