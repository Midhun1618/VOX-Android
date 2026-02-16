package com.voxcom.vox.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.voxcom.vox.data.ReminderManager
import com.voxcom.vox.data.model.Reminder

object ReminderRepository {

    private val db = FirebaseFirestore.getInstance()
    private val uid get() = FirebaseAuth.getInstance().uid ?: "unknown"

    private fun ref() =
        db.collection("users")
            .document(uid)
            .collection("reminders")

    fun add(title: String, time: Long) {

        val reminder = Reminder(
            title = title,
            time = time,
            createdBy = "phone"
        )

        ref().add(reminder)
            .addOnSuccessListener {
                Log.d("VOX_REMINDER", "Reminder uploaded")
            }
            .addOnFailureListener {
                Log.e("VOX_REMINDER", "Upload failed", it)
            }
    }

    fun listen(): ListenerRegistration? {

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return null

        return db.collection("users")
            .document(uid)
            .collection("reminders")
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    Log.e("VOX_REMINDER", "Listen failed", error)
                    return@addSnapshotListener
                }

                if (snapshot == null) return@addSnapshotListener

                val reminders = snapshot.documents.mapNotNull { doc ->

                    val time = doc.getLong("time") ?: return@mapNotNull null

                    Reminder(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        time = time
                    )
                }


                Log.d("VOX_REMINDER", "Firestore -> ${reminders.size} reminders")

                ReminderManager.update(reminders)
            }
    }


}
