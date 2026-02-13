package com.voxcom.vox.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object UserRepository {

    private val db = FirebaseFirestore.getInstance()

    private fun uid(): String? = FirebaseAuth.getInstance().currentUser?.uid

    fun ensureUserDocument() {
        val uid = uid() ?: return

        val ref = db.collection("users").document(uid)

        ref.get().addOnSuccessListener { doc ->
            if (!doc.exists()) {
                val data = hashMapOf(
                    "email" to FirebaseAuth.getInstance().currentUser?.email,
                    "totalTasks" to 0,
                    "completedTasks" to 0,
                    "createdAt" to Timestamp.Companion.now()
                )
                ref.set(data)
            }
        }
    }

    fun getStats(onResult: (total: Long, completed: Long) -> Unit) {
        val uid = uid() ?: return

        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val total = doc.getLong("totalTasks") ?: 0
                val completed = doc.getLong("completedTasks") ?: 0
                onResult(total, completed)
            }
    }

    fun getProfile(onResult: (username: String?, avatarIndex: Int?, code: String?) -> Unit) {
        val uid = uid() ?: return

        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                onResult(
                    doc.getString("username"),
                    doc.getLong("avatarIndex")?.toInt(),
                    doc.getString("accessCode")
                )
            }
    }
}