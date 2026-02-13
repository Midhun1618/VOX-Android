package com.voxcom.vox.system

import android.content.Context
import android.media.MediaPlayer

object SoundPlayer {

    fun wake(context: Context, resId: Int) {
        val mp = MediaPlayer.create(context, resId)
        mp.setOnCompletionListener { it.release() }
        mp.start()
    }
}
