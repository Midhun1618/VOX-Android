package com.voxcom.vox.data

object ClipboardMemory {

    private var lastCopied: String? = null

    fun save(text: String) {
        lastCopied = text
    }

    fun get(): String? = lastCopied
}
