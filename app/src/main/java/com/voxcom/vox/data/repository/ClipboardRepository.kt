package com.voxcom.vox.data.repository

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

object ClipboardRepository {

    private val db = FirebaseFirestore.getInstance()


    private fun uid(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }


    private fun ref() = uid()?.let {
        db.collection("users")
            .document(it)
            .collection("clipboard")
            .document("state")
    }

    fun push(content: String, device: String) {

        val user = FirebaseAuth.getInstance().currentUser
        Log.d("CLIPBOARD", "user = $user")

        val reference = ref() ?: return

        val data = hashMapOf(
            "content" to content,
            "device" to device,
            "timestamp" to System.currentTimeMillis()
        )
        reference.set(data)
    }

    fun listen(onChange: (content: String, device: String, time: Long) -> Unit): ListenerRegistration? {

        val reference = ref() ?: return null

        return reference.addSnapshotListener { doc, error ->

            Log.d("VOX_CLIP", "LISTENER TRIGGERED")

            if (error != null) {
                Log.d("VOX_CLIP", "ERROR: ${error.message}")
                return@addSnapshotListener
            }

            if (doc == null || !doc.exists()) {
                Log.d("VOX_CLIP", "NO DOCUMENT")
                return@addSnapshotListener
            }

            val content = doc.getString("content")
            val device = doc.getString("device")
            val time = doc.getLong("timestamp")


            Log.d("VOX_CLIP", "DATA -> $content | $device | $time")

            if (content == null || device == null || time == null) return@addSnapshotListener

            onChange(content, device, time)
        }
    }


}
