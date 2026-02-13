package com.voxcom.vox.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import java.util.concurrent.TimeUnit
import com.voxcom.vox.data.model.Task


object TaskRepository {

    private val db = FirebaseFirestore.getInstance()

    private fun uid(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    fun addTask(title: String) {

        val userId = uid() ?: return

        val now = Timestamp.now()
        val expiresAt = Timestamp(
            now.seconds + TimeUnit.HOURS.toSeconds(24),
            0
        )

        val task = hashMapOf(
            "title" to title,
            "createdAt" to now,
            "expiresAt" to expiresAt,
            "completed" to false
        )

        db.collection("users")
            .document(userId)
            .collection("tasks")
            .add(task)

        db.collection("users")
            .document(userId)
            .set(
                mapOf("totalTasks" to FieldValue.increment(1)),
                SetOptions.merge()
            )
    }

    fun markCompleted(taskId: String) {

        val userId = uid() ?: return

        db.collection("users")
            .document(userId)
            .collection("tasks")
            .document(taskId)
            .update("completed", true)
            .addOnSuccessListener {
                db.collection("users")
                    .document(userId)
                    .update("completedTasks", FieldValue.increment(1))
            }
    }

    fun listenTasks(onChange: (List<Task>) -> Unit): ListenerRegistration? {

        val userId = uid() ?: return null

        return db.collection("users")
            .document(userId)
            .collection("tasks")
            .whereEqualTo("completed", false)
            .addSnapshotListener { snapshot, error ->

                if (error != null || snapshot == null) return@addSnapshotListener

                val tasks = snapshot.documents.map { doc ->
                    Task(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        completed = false,
                        expiresAt = doc.getTimestamp("expiresAt")
                    )
                }

                onChange(tasks)
            }
    }

}
