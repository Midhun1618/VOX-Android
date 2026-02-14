package com.voxcom.vox.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions

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
                    "totalTask" to 0,
                    "completedTask" to 0,
                    "createdAt" to Timestamp.Companion.now()
                )
                ref.set(data, SetOptions.merge())
            }
        }
    }

    fun getStats(onResult: (total: Long, completed: Long, expired: Int) -> Unit) {

        val uid = uid() ?: return

        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { doc ->

                val total = doc.getLong("totalTask") ?: 0
                val completed = doc.getLong("completedTask") ?: 0

                TaskRepository.getExpiredCount { expired ->
                    onResult(total, completed, expired)
                }
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
    fun listenStats(onResult: (total: Long, completed: Long, expired: Int) -> Unit): ListenerRegistration? {

        val uid = uid() ?: return null

        return db.collection("users").document(uid)
            .addSnapshotListener { doc, _ ->

                if (doc == null || !doc.exists()) return@addSnapshotListener

                val total = doc.getLong("totalTask") ?: 0
                val completed = doc.getLong("completedTask") ?: 0

                TaskRepository.getExpiredCount { expired ->
                    onResult(total, completed, expired)
                }
            }
    }

}