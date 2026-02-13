package com.voxcom.vox.voice

import android.content.Context

class VoxAssistantManager(private val context: Context) {

    private val recognizer = VoxSpeechRecognizer(context)

    fun startListening() {
        recognizer.startListening { text ->
            VoxCommandProcessor.process(context, text)
        }
    }
}
