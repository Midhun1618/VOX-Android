package com.voxcom.vox

import android.content.Context
import android.widget.Toast
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

object VoxCommandProcessor {

    fun process(context: Context, text: String) {

        val task = extractTask(text) ?: return

        addTaskToFirebase(context, task)

        Toast.makeText(context, "Task added: $task", Toast.LENGTH_SHORT).show()
    }

    private fun extractTask(text: String): String? {

        val regex = Regex("add( the)? task", RegexOption.IGNORE_CASE)
        val match = regex.find(text) ?: return null

        val task = text.substring(match.range.last + 1).trim()

        return if (task.isNotEmpty()) task else null
    }


    private fun addTaskToFirebase(context: Context, title: String) {

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        val now = Timestamp.now()
        val expiresAt = Timestamp(now.seconds + 86400, 0)

        val task = hashMapOf(
            "title" to title,
            "createdAt" to now,
            "expiresAt" to expiresAt,
            "completed" to false
        )

        db.collection("users")
            .document(uid)
            .collection("tasks")
            .add(task)

        db.collection("users")
            .document(uid)
            .set(mapOf("totalTasks" to FieldValue.increment(1)), SetOptions.merge())
    }
}
