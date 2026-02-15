package com.voxcom.vox.data

object ClipboardMemory {

    private var cached: String? = null

    fun set(text: String?) {
        cached = text?.trim()
    }

    fun get(): String? = cached

    fun clear() {
        cached = null
    }

    fun hasData(): Boolean = !cached.isNullOrBlank()
}
